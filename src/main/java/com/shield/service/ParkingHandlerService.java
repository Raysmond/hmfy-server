package com.shield.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.domain.*;
import com.shield.domain.enumeration.ParkMsgType;
import com.shield.domain.enumeration.ParkingConnectMethod;
import com.shield.domain.enumeration.RecordType;
import com.shield.repository.ParkMsgRepository;
import com.shield.service.dto.*;
import com.shield.service.tcp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class ParkingHandlerService {
    @Autowired
    private ParkMsgService parkMsgService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("tcpOut")
    private MessageChannel tcpOut;

    @Autowired
    @Qualifier("redisLongTemplate")
    private RedisTemplate<String, Long> redisLongTemplate;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ParkMsgRepository parkMsgRepository;

    @Autowired
    private AbstractServerConnectionFactory cf;

    @Autowired
    private CarWhiteListService carWhiteListService;

    private static final Gson GSON = new Gson();

    private static Map<String, String> parkId2ConnectionId = Maps.newHashMap();
    private static Map<String, String> connectionId2ParkId = Maps.newHashMap();

    @Autowired
    private ShipPlanService shipPlanService;

    public static final String AUTO_REGISTERED_PLAN_IDS = "set_auto_registered_ship_plan_ids";
    public static final String AUTO_DELETED_PLAN_IDS = "set_auto_deleted_ship_plan_ids";

    public static final String AUTO_REGISTER_PLAN_ID_QUEUE = "auto_register_ship_plan_ids_queue";
    public static final String AUTO_DELETE_PLAN_ID_QUEUE = "auto_delete_ship_plan_ids_queue";

    public static final String REDIS_KEY_UPLOAD_CAR_WHITELIST = "upload_car_whitelist_appointment_ids";
    public static final String REDIS_KEY_DELETE_CAR_WHITELIST = "delete_car_whitelist_appointment_ids";

    public static final String REDIS_KEY_TRUCK_NUMBER_CARD_ID = "truck_number_2_card_id";

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 注册/删除 固定车辆白名单 检查队列 根据客户端返回消息进行确认
    public static final String REDIS_KEY_CHECK_REGISTER_WHITELIST_TRUCK_NUMBER_QUEUE = "set_check_register_msg_id_queue";

    public String handle(Message<String> msg) {
        log.info("Receive TCP msg: {}", msg.getPayload());
        try {
            ServiceResponse response = handleService(msg);
            String responseMsg = objectMapper.writeValueAsString(response);
            log.info("Handle msg success, response: {}", responseMsg);
            return responseMsg;
        } catch (Exception e) {
            log.error("failed to handle msg: {}, exception: ", msg.getPayload(), e);
            return "{\"service\": \"\", \"result_code\":1, \"message\": \"处理失败\"}";
        }
    }


    private ServiceResponse handleService(Message<String> message) throws IOException {
        String msg = message.getPayload();
        JsonObject data = GSON.fromJson(msg, JsonObject.class);
        if (!data.has("service")) {
            return new ServiceResponse("", 1, "无法识别service");
        }
        try {
            ParkMsgDTO parkMsgDTO = new ParkMsgDTO();
            parkMsgDTO.setBody(msg);
            if (data.has("parkid")) {
                parkMsgDTO.setParkid(data.get("parkid").getAsString());
            } else {
                String connectionId = (String) message.getHeaders().getOrDefault(IpHeaders.CONNECTION_ID, null);
                parkMsgDTO.setParkid(connectionId2ParkId.getOrDefault(connectionId, "NONE"));
            }
            parkMsgDTO.setService(data.get("service").getAsString());
            parkMsgDTO.setCreateTime(ZonedDateTime.now());
            parkMsgDTO.setType(ParkMsgType.IN);
            parkMsgDTO.setSendTime(ZonedDateTime.now());
            parkMsgDTO.setSendTimes(1);
            if (!parkMsgDTO.getService().equals("heartbeat")) {
                parkMsgService.save(parkMsgDTO);
            }
        } catch (Exception e) {
            log.error("failed to save ParkMsg, msg: {}", msg, e);
        }

        switch (data.get("service").getAsString()) {
            case "checkKey":
                AuthRequest authRequest = objectMapper.readValue(msg, AuthRequest.class);
                // todo
                return new ServiceResponse(authRequest.getService(), 0, "认证成功");
            case "heartbeat":
                HeartBeatMsg heartBeatMsg = objectMapper.readValue(msg, HeartBeatMsg.class);
                String connectionId = (String) message.getHeaders().getOrDefault(IpHeaders.CONNECTION_ID, null);
                parkId2ConnectionId.put(heartBeatMsg.getParkid(), connectionId);
                connectionId2ParkId.put(connectionId, heartBeatMsg.getParkid());
                log.info("TCP socket ip_connectionId: {}, parkid: {}", connectionId, heartBeatMsg.getParkid());
                return new ServiceResponse(heartBeatMsg.getService(), 0, "在线");
            case "uploadcarin":
                UploadCarInMsg inMsg = objectMapper.readValue(msg, UploadCarInMsg.class);
                updateCarInOutTime(inMsg.getParkid(), inMsg.getCar_number(), RecordType.IN, inMsg.getIn_time(), null);
                return new UploadCarInOutResponse(inMsg.getService(), 0, "上传成功", inMsg.getOrder_id());
            case "uploadcarout":
                UploadCarOutMsg outMsg = objectMapper.readValue(msg, UploadCarOutMsg.class);
                updateCarInOutTime(outMsg.getParkid(), outMsg.getCar_number(), RecordType.OUT, outMsg.getIn_time(), outMsg.getOut_time());
                return new UploadCarInOutResponse(outMsg.getService(), 0, "上传成功", outMsg.getOrder_id());
            case "whitelist_sync":
                // 固定车注册与修改 返回
                WhiteListMsgResponse whiteListMsgResponse = objectMapper.readValue(msg, WhiteListMsgResponse.class);
                handleWhiteListResponse(whiteListMsgResponse);
                return new ServiceResponse(whiteListMsgResponse.getService(), 0, "成功");
            case "uploadopenstrobe":
                // 手动开闸记录上传
                String recordid = data.get("recordid").getAsString();
                return new UploadOpenStrobeResponse(data.get("service").getAsString(), 0, "上传成功", recordid);
            default:
                return new ServiceResponse(data.get("service").getAsString(), 0, "无法处理此消息");
        }
    }


    private ParkMsgDTO saveTcpSendMessage(String msg, String parkId, String service, String truckNumber) {
        ParkMsgDTO parkMsgDTO = new ParkMsgDTO();
        parkMsgDTO.setBody(msg);
        parkMsgDTO.setParkid(parkId);
        parkMsgDTO.setService(service);
        parkMsgDTO.setCreateTime(ZonedDateTime.now());
        parkMsgDTO.setType(ParkMsgType.OUT);
        parkMsgDTO.setTruckNumber(truckNumber);
        parkMsgDTO.setSendTimes(1);
        parkMsgDTO.setSendTime(ZonedDateTime.now());
        parkMsgDTO = parkMsgService.save(parkMsgDTO);
        return parkMsgDTO;
    }


    private void handleWhiteListResponse(WhiteListMsgResponse whiteListMsgResponse) {
        if (whiteListMsgResponse.getMessage().equals("注册固定车成功")) {
            putTruckNumber2CardId(whiteListMsgResponse.getCar_number(), whiteListMsgResponse.getCard_id());
        }
        if (whiteListMsgResponse.getMessage().equals("注册固定车成功")
            || whiteListMsgResponse.getMessage().equals("删除固定车成功")
            || whiteListMsgResponse.getMessage().equals("注册固定车失败,车牌号重复")
            || whiteListMsgResponse.getMessage().equals("删除固定车失败,card_id 不存在")) {
            Page<ParkMsg> parkMsgs = parkMsgRepository.findByServiceAndTruckNumber(whiteListMsgResponse.getService(),
                whiteListMsgResponse.getCar_number(), ParkMsgType.OUT,
                PageRequest.of(0, 1, Sort.Direction.DESC, "id"));
            if (!parkMsgs.getContent().isEmpty()) {
                ParkMsg parkMsg = parkMsgs.getContent().get(0);
                redisLongTemplate.opsForSet().remove(REDIS_KEY_CHECK_REGISTER_WHITELIST_TRUCK_NUMBER_QUEUE, parkMsg.getId());
            }
        }
    }

    private void putTruckNumber2CardId(String truckNumber, String cardId) {
        log.info("set card_id = {}, car_number: {}", cardId, truckNumber);
        String k = DigestUtils.md5Hex(truckNumber).toUpperCase();
        redisTemplate.opsForHash().put(REDIS_KEY_TRUCK_NUMBER_CARD_ID, k, cardId);
    }

    private String getCardIdByTruckNumber(String truckNumber) {
        String k = DigestUtils.md5Hex(truckNumber).toUpperCase();
        if (redisTemplate.opsForHash().hasKey(REDIS_KEY_TRUCK_NUMBER_CARD_ID, k)) {
            return (String) redisTemplate.opsForHash().get(REDIS_KEY_TRUCK_NUMBER_CARD_ID, k);
        } else {
            return null;
        }
    }

    //@PostConstruct
    private void loadWhiteListSyncServiceMsg() {
        Page<ParkMsgDTO> page = parkMsgService.findAllByService(PageRequest.of(0, 10000, Sort.Direction.ASC, "id"), "whitelist_sync");
        for (ParkMsgDTO parkMsg : page.getContent()) {
            try {
                JsonObject data = GSON.fromJson(parkMsg.getBody(), JsonObject.class);
                if (data.get("result_code").getAsInt() == 0 && data.get("message").getAsString().equals("注册固定车成功")) {
                    putTruckNumber2CardId(data.get("car_number").getAsString(), data.get("card_id").getAsString());
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 自动注册 固定车白名单 (TCP)
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void autoRegisterCarWhiteList() throws JsonProcessingException, InterruptedException {
        Set<Long> planIds = redisLongTemplate.opsForSet().members(AUTO_REGISTER_PLAN_ID_QUEUE);
        if (planIds == null || planIds.isEmpty()) {
            return;
        }

        log.info("[TCP] Find {} ShipPlan ids need to register car whitelist", planIds.size());

        Set<String> openConnectionIds = Sets.newHashSet(cf.getOpenConnectionIds());
        if (openConnectionIds.isEmpty()) {
            return;
        }

        Map<String, RegionDTO> regions = Maps.newHashMap();
        for (RegionDTO region : regionService.findAll(PageRequest.of(0, 1000))) {
            regions.put(region.getName(), region);
        }

        for (Long planId : planIds) {
            // 自动注册白名单开启
            ShipPlanDTO planDTO = shipPlanService.findOne(planId).get();
            if (!planDTO.getAuditStatus().equals(1)) {
                redisLongTemplate.opsForSet().remove(AUTO_REGISTER_PLAN_ID_QUEUE, planDTO.getId());
                continue;
            }
            if (redisLongTemplate.opsForSet().isMember(AUTO_REGISTERED_PLAN_IDS, planDTO.getId())) {
                continue;
            }
            if (!regions.containsKey(planDTO.getDeliverPosition())) {
                redisLongTemplate.opsForSet().remove(AUTO_REGISTER_PLAN_ID_QUEUE, planDTO.getId());
                continue;
            }
            RegionDTO region = regions.get(planDTO.getDeliverPosition());
            if (!region.isOpen()  || !region.isAutoAppointment() || region.getParkingConnectMethod() == null || !region.getParkingConnectMethod().equals(ParkingConnectMethod.TCP)) {
                redisLongTemplate.opsForSet().remove(AUTO_REGISTER_PLAN_ID_QUEUE, planDTO.getId());
                continue;
            }

            log.info("[TCP] ShipPlan [id={}], apply_id: {}, truckNumber: {}, need to auto register whitelist",
                planDTO.getId(), planDTO.getApplyId(), planDTO.getTruckNumber());

            UploadCarWhiteListMsg carWhiteListMsg = new UploadCarWhiteListMsg();
            String now = ZonedDateTime.now().format(DATE_TIME_FORMAT);
            carWhiteListMsg.setParkid(region.getParkId());
            carWhiteListMsg.setCar_number(planDTO.getTruckNumber());
            // carWhiteListMsg.setCard_id(planDTO.getTruckNumber());
            carWhiteListMsg.setOperate_type(1);
            carWhiteListMsg.setStartdate(now);
            ZonedDateTime validDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1);
            if (validDate.isBefore(ZonedDateTime.now().plusHours(6))) {
                log.info("car whitelist validdate {} < startdate {} + 6 hours, extend to 6 hours {}.",
                    validDate.format(DATE_TIME_FORMAT), now, ZonedDateTime.now().plusHours(6).format(DATE_TIME_FORMAT));
                validDate = ZonedDateTime.now().plusHours(6);
            }
            carWhiteListMsg.setValiddate(validDate.format(DATE_TIME_FORMAT));
            carWhiteListMsg.setCreate_time(now);
            carWhiteListMsg.setModify_time(now);
            String msg = objectMapper.writeValueAsString(carWhiteListMsg);
            if (sendMessageToClient(msg, region.getParkId())) {
                redisLongTemplate.opsForSet().add(AUTO_REGISTERED_PLAN_IDS, planDTO.getId());
                redisLongTemplate.opsForSet().remove(AUTO_REGISTER_PLAN_ID_QUEUE, planDTO.getId());
                ParkMsgDTO parkMsg = saveTcpSendMessage(msg, region.getParkId(), carWhiteListMsg.getService(), carWhiteListMsg.getCar_number());
                redisLongTemplate.opsForSet().add(REDIS_KEY_CHECK_REGISTER_WHITELIST_TRUCK_NUMBER_QUEUE, parkMsg.getId());
            }

            Thread.sleep(1000);
        }
    }

    private boolean isRegionParkingTcpConnected(String regionParkId) {
        Set<String> openConnectionIds = Sets.newHashSet(cf.getOpenConnectionIds());
        if (parkId2ConnectionId.containsKey(regionParkId)
            && parkId2ConnectionId.get(regionParkId) != null
            && openConnectionIds.contains(parkId2ConnectionId.get(regionParkId))) {
            return true;
        } else {
            log.error("Failed to find connectionId for parkid: {}", regionParkId);
            return false;
        }
    }

    /**
     * 删除固定车白名单 (TCP)
     */
    @Scheduled(fixedRate = 30 * 1000)
    public void deleteRegisterCarWhiteList() throws JsonProcessingException, InterruptedException {
        Set<Long> autoDeletePlanIds = redisLongTemplate.opsForSet().members(AUTO_DELETE_PLAN_ID_QUEUE);
        if (autoDeletePlanIds == null || autoDeletePlanIds.isEmpty()) {
            return;
        }

        log.info("Find {} ShipPlan ids need to delete car whitelist", autoDeletePlanIds.size());
        for (Long planId : autoDeletePlanIds) {
            Optional<ShipPlanDTO> planDTO = shipPlanService.findOne(planId);
            if (redisLongTemplate.opsForSet().isMember(AUTO_DELETED_PLAN_IDS, planId)) {
                redisLongTemplate.opsForSet().remove(AUTO_DELETE_PLAN_ID_QUEUE, planId);
                continue;
            }

            if (planDTO.isPresent()) {
                ShipPlanDTO plan = planDTO.get();
                RegionDTO region = regionService.findByName(plan.getDeliverPosition());
                if (region == null || !region.isOpen() || region.getParkingConnectMethod() == null || !region.getParkingConnectMethod().equals(ParkingConnectMethod.TCP)) {
                    redisLongTemplate.opsForSet().add(AUTO_DELETED_PLAN_IDS, plan.getId());
                    redisLongTemplate.opsForSet().remove(AUTO_DELETE_PLAN_ID_QUEUE, plan.getId());
                    continue;
                }

                log.info("ShipPlan [id={}], apply_id: {}, truckNumber: {}, need to be removed from car whitelist",
                    plan.getId(), plan.getApplyId(), plan.getTruckNumber());

                String cardId = getCardIdByTruckNumber(plan.getTruckNumber());
                if (StringUtils.isBlank(cardId)) {
                    log.warn("Cannot find card_id for truckNumber: {}", plan.getTruckNumber());
                    redisLongTemplate.opsForSet().add(AUTO_DELETED_PLAN_IDS, plan.getId());
                    redisLongTemplate.opsForSet().remove(AUTO_DELETE_PLAN_ID_QUEUE, plan.getId());
                    continue;
                }

                if (!isRegionParkingTcpConnected(region.getParkId())) {
                    continue;
                }

                DeleteCarWhiteListMsg deleteMsg = new DeleteCarWhiteListMsg();
                deleteMsg.setCar_number(plan.getTruckNumber());
                deleteMsg.setCard_id(cardId);
                deleteMsg.setParkid(region.getParkId());
                String msg = objectMapper.writeValueAsString(deleteMsg);
                if (sendMessageToClient(msg, region.getParkId())) {
                    ParkMsgDTO parkMsg = saveTcpSendMessage(msg, deleteMsg.getParkid(), deleteMsg.getService(), deleteMsg.getCar_number());
                    redisLongTemplate.opsForSet().add(AUTO_DELETED_PLAN_IDS, plan.getId());
                    redisLongTemplate.opsForSet().remove(AUTO_DELETE_PLAN_ID_QUEUE, plan.getId());
                    redisLongTemplate.opsForSet().add(REDIS_KEY_CHECK_REGISTER_WHITELIST_TRUCK_NUMBER_QUEUE, parkMsg.getId());
                }
                Thread.sleep(1000);
            }
        }
    }

    private boolean sendMessageToClient(String msg, String parkId) {
        String connectionId = parkId2ConnectionId.get(parkId);
        Message<String> message = MessageBuilder.withPayload(msg)
            .setHeader(IpHeaders.CONNECTION_ID, connectionId)
            .build();

        if (tcpOut.send(message)) {
            log.info("Send TCP message to client successfully, parkId: {}, msg: {}", parkId, msg);
            return true;
        } else {
            log.error("Send TCP message to client failed, parkId: {}, msg: {}", parkId, msg);
            return false;
        }
    }

    /**
     * TCP upload/delete car whitelist
     */
    @Scheduled(fixedRate = 10 * 1000)
    public void uploadCarWhiteList() {
        Set<Long> appointmentIds = redisLongTemplate.opsForSet().members(REDIS_KEY_UPLOAD_CAR_WHITELIST);
        if (!CollectionUtils.isEmpty(appointmentIds)) {
            log.info("[TCP] Find {} appointmentIds need to upload car whitelist", appointmentIds.size());
            for (Long appointmentId : appointmentIds) {
                try {
                    AppointmentDTO appointment = appointmentService.findOne(appointmentId).get();
                    RegionDTO region = regionService.findOne(appointment.getRegionId()).get();

                    if (StringUtils.isBlank(region.getParkId())
                        || !region.isOpen()
                        || region.getParkingConnectMethod() == null || !region.getParkingConnectMethod().equals(ParkingConnectMethod.TCP)
                        || (region.isAutoAppointment() != null && region.isAutoAppointment())) {
                        log.warn("[TCP] no need to upload car whitelist for appointment of region: {}, name: {}", region.getId(), region.getName());
                        redisLongTemplate.opsForSet().remove(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointmentId);
                        continue;
                    }
                    if (isRegionParkingTcpConnected(region.getParkId())) {
                        UploadCarWhiteListMsg whiteListMsg = generateUploadCarWhiteListMsg(appointmentId);
                        if (whiteListMsg == null) {
                            redisLongTemplate.opsForSet().remove(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointmentId);
                            continue;
                        }
                        String sendMsg = objectMapper.writeValueAsString(whiteListMsg);
                        if (sendMessageToClient(sendMsg, whiteListMsg.getParkid())) {
                            log.info("[TCP] Upload car whitelist successfully. appointmentId: {}, msg: {}", appointmentId, sendMsg);
                            ParkMsgDTO parkMsg = saveTcpSendMessage(sendMsg, whiteListMsg.getParkid(), whiteListMsg.getService(), whiteListMsg.getCar_number());
                            redisLongTemplate.opsForSet().remove(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointmentId);
                            redisLongTemplate.opsForSet().add(REDIS_KEY_CHECK_REGISTER_WHITELIST_TRUCK_NUMBER_QUEUE, parkMsg.getId());
                        }
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    log.error("Failed to upload car whitelist, appointmentId: {}", appointmentId, e);
                }
            }
        }

        Set<Long> appointmentIdsDeleteQueue = redisLongTemplate.opsForSet().members(REDIS_KEY_DELETE_CAR_WHITELIST);
        if (!CollectionUtils.isEmpty(appointmentIdsDeleteQueue)) {
            log.info("Find {} appointmentIds need to delete car whitelist", appointmentIdsDeleteQueue.size());
            for (Long appointmentId : appointmentIdsDeleteQueue) {
                try {
                    AppointmentDTO appointment = appointmentService.findOne(appointmentId).get();
                    RegionDTO region = regionService.findOne(appointment.getRegionId()).get();

                    if (!region.isOpen()
                        || StringUtils.isBlank(region.getParkId())
                        || region.getParkingConnectMethod() == null || !region.getParkingConnectMethod().equals(ParkingConnectMethod.TCP)
                        || (region.isAutoAppointment() != null && region.isAutoAppointment())) {
                        log.warn("[TCP] no need to upload car whitelist for appointment of region: {}, name: {}", region.getId(), region.getName());
                        redisLongTemplate.opsForSet().remove(REDIS_KEY_DELETE_CAR_WHITELIST, appointmentId);
                        continue;
                    }

                    if (isRegionParkingTcpConnected(region.getParkId())) {
                        DeleteCarWhiteListMsg deleteCarWhiteListMsg = generateDeleteCarWhiteListMsg(appointmentId);
                        if (deleteCarWhiteListMsg == null) {
                            redisLongTemplate.opsForSet().remove(REDIS_KEY_DELETE_CAR_WHITELIST, appointmentId);
                            continue;
                        }
                        String sendMsg = objectMapper.writeValueAsString(deleteCarWhiteListMsg);
                        if (sendMessageToClient(sendMsg, deleteCarWhiteListMsg.getParkid())) {
                            log.info("[TCP] Delete car whitelist successfully. appointmentId: {}, msg: {}", appointmentId, sendMsg);
                            redisLongTemplate.opsForSet().remove(REDIS_KEY_DELETE_CAR_WHITELIST, appointmentId);
                            ParkMsgDTO parkMsg = saveTcpSendMessage(sendMsg, deleteCarWhiteListMsg.getParkid(), deleteCarWhiteListMsg.getService(), deleteCarWhiteListMsg.getCar_number());
                            redisLongTemplate.opsForSet().add(REDIS_KEY_CHECK_REGISTER_WHITELIST_TRUCK_NUMBER_QUEUE, parkMsg.getId());
                        }
                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    log.error("Failed to delete car whitelist, appointmentId: {}", appointmentId, e);
                }
            }
        }
    }

    private void updateCarInOutTime(String parkId, String truckNumber, RecordType recordType, String carInTime, String carOutTime) {
        try {
            RegionDTO regionDTO = regionService.findByParkId(parkId);
            if (regionDTO == null) {
                return;
            }
            ZonedDateTime outTime = null;
            ZonedDateTime inTime = null;
            if (StringUtils.isNotBlank(carInTime)) {
                inTime = ZonedDateTime.parse(carInTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()));
            }
            if (StringUtils.isNotBlank(carOutTime)) {
                outTime = ZonedDateTime.parse(carOutTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()));
            }
            carWhiteListService.updateCarInAndOutTime(regionDTO.getId(), truckNumber, recordType, inTime, outTime);
        } catch (Exception e) {
            log.error("failed to execute carWhiteListService.updateCarInAndOutTime(), " +
                "parkId: {}, truckNumber: {}, carInTime: {}, carOutTime: {}", parkId, truckNumber, carInTime, carOutTime);
            log.error("exception: ", e);
        }
    }


    public UploadCarWhiteListMsg generateUploadCarWhiteListMsg(Long appointmentId) {
        try {
            AppointmentDTO appointment = appointmentService.findOne(appointmentId).get();
            RegionDTO regionDTO = regionService.findOne(appointment.getRegionId()).get();

            if (StringUtils.isBlank(regionDTO.getParkId())) {
                log.warn("missing parkId of region {}, ignore generateUploadCarWhiteListMsg", regionDTO.getId());
                return null;
            }
            if (!regionDTO.isOpen()) {
                log.warn("region {} {}  is not opened for appointment", regionDTO.getId(), regionDTO.getName());
                return null;
            }
            if (regionDTO.isAutoAppointment() != null && regionDTO.isAutoAppointment()) {
                // 开启默认注册白名单，不需要预约后再注册
                log.warn("region {} {}  auto_appointment enabled", regionDTO.getId(), regionDTO.getName());
                return null;
            }
            UploadCarWhiteListMsg carWhiteListMsg = new UploadCarWhiteListMsg();
            String now = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            carWhiteListMsg.setParkid(regionDTO.getParkId());
            carWhiteListMsg.setCar_number(appointment.getLicensePlateNumber());
//            carWhiteListMsg.setCard_id(appointment.getLicensePlateNumber());
            if (StringUtils.isNotBlank(appointment.getUserLogin())) {
                carWhiteListMsg.setCarusername(appointment.getUserLogin());
            }
            carWhiteListMsg.setOperate_type(1); // 注册
            carWhiteListMsg.setStartdate(now);
            carWhiteListMsg.setValiddate(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            carWhiteListMsg.setCreate_time(now);
            carWhiteListMsg.setModify_time(now);
            return carWhiteListMsg;
        } catch (Exception e) {
            log.error("failed execute uploadCarWhiteList(), appointmentId: {}", appointmentId, e);
            return null;
        }
    }

    private DeleteCarWhiteListMsg generateDeleteCarWhiteListMsg(Long appointmentId) {
        try {
            AppointmentDTO appointment = appointmentService.findOne(appointmentId).get();
            RegionDTO regionDTO = regionService.findOne(appointment.getRegionId()).get();
            DeleteCarWhiteListMsg carWhiteListMsg = new DeleteCarWhiteListMsg();

            if (StringUtils.isBlank(regionDTO.getParkId())) {
                log.warn("missing parkId of region {}, ignore generateDeleteCarWhiteListMsg", regionDTO.getId());
                return null;
            }
            if (!regionDTO.isOpen()) {
                log.warn("region {} {}  is not opened for appointment", regionDTO.getId(), regionDTO.getName());
                return null;
            }
            if (regionDTO.isAutoAppointment() != null && regionDTO.isAutoAppointment()) {
                // 开启默认注册白名单，不需要预约后再注册
                log.warn("region {} {} auto_appointment enabled", regionDTO.getId(), regionDTO.getName());
                return null;
            }

            String cardId = getCardIdByTruckNumber(appointment.getLicensePlateNumber());
            if (StringUtils.isBlank(cardId)) {
                log.warn("Cannot find card_id for truckNumber: {}", appointment.getLicensePlateNumber());
                return null;
            }

            carWhiteListMsg.setParkid(regionDTO.getParkId());
            carWhiteListMsg.setCar_number(appointment.getLicensePlateNumber());
            carWhiteListMsg.setCard_id(cardId);
            return carWhiteListMsg;
        } catch (Exception e) {
            log.error("failed execute uploadCarWhiteList(), appointmentId: {}", appointmentId, e);
            return null;
        }
    }


}
