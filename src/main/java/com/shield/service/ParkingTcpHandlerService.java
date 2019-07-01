package com.shield.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shield.domain.Region;
import com.shield.domain.enumeration.ParkMsgType;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.ParkMsgDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class ParkingTcpHandlerService {
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
    private AbstractServerConnectionFactory cf;

    private static final Gson GSON = new Gson();

    private static Map<String, String> parkId2ConnectionId = Maps.newHashMap();
    private static Map<String, String> connectionId2ParkId = Maps.newHashMap();

    @Autowired
    private ShipPlanService shipPlanService;

    public static final String AUTO_REGISTERED_PLAN_IDS = "set_auto_registered_ship_plan_ids";
    public static final String AUTO_REGISTERED_DELETE_PLAN_IDS = "set_auto_deleted_ship_plan_ids";
    public static final String AUTO_DELETE_PLAN_ID_QUEUE = "auto_delete_ship_plan_ids_queue";

    public static final String REDIS_KEY_UPLOAD_CAR_WHITELIST = "upload_car_whitelist_appointment_ids";
    public static final String REDIS_KEY_DELETE_CAR_WHITELIST = "delete_car_whitelist_appointment_ids";

    public static final String REDIS_KEY_TRUCK_NUMBER_CARD_ID = "truck_number_2_card_id";

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
                updateCarInOutTime(inMsg.getParkid(), inMsg.getCar_number(), inMsg.getService(), inMsg.getIn_time(), null);
                return new UploadCarInOutResponse(inMsg.getService(), 0, "上传成功", inMsg.getOrder_id());
            case "uploadcarout":
                UploadCarOutMsg outMsg = objectMapper.readValue(msg, UploadCarOutMsg.class);
                updateCarInOutTime(outMsg.getParkid(), outMsg.getCar_number(), outMsg.getService(), outMsg.getIn_time(), outMsg.getOut_time());
                return new UploadCarInOutResponse(outMsg.getService(), 0, "上传成功", outMsg.getOrder_id());
            case "whitelist_sync":
                // 固定车注册与修改 返回
                WhiteListMsgResponse whiteListMsgResponse = objectMapper.readValue(msg, WhiteListMsgResponse.class);
                handleWhiteListResponse(whiteListMsgResponse);
                return new ServiceResponse(whiteListMsgResponse.getService(), 0, "成功");
            default:
                return new ServiceResponse(data.get("service").getAsString(), 0, "无法处理此消息");
        }
    }

    private void saveTcpSendMessage(String msg, String parkId, String service) {
        ParkMsgDTO parkMsgDTO = new ParkMsgDTO();
        parkMsgDTO.setBody(msg);
        parkMsgDTO.setParkid(parkId);
        parkMsgDTO.setService(service);
        parkMsgDTO.setCreateTime(ZonedDateTime.now());
        parkMsgDTO.setType(ParkMsgType.OUT);
        parkMsgService.save(parkMsgDTO);
    }


    private void handleWhiteListResponse(WhiteListMsgResponse whiteListMsgResponse) {
        if (whiteListMsgResponse.getMessage().equals("注册固定车成功")) {
            putTruckNumber2CardId(whiteListMsgResponse.getCar_number(), whiteListMsgResponse.getCard_id());
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

    @PostConstruct
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

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 自动注册 固定车白名单
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void autoRegisterCarWhiteList() throws JsonProcessingException {
        Set<String> openConnectionIds = Sets.newHashSet(cf.getOpenConnectionIds());
        if (openConnectionIds.isEmpty()) {
            return;
        }
        ZonedDateTime todayBegin = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        List<RegionDTO> regions = regionService.findAll(PageRequest.of(0, 10000)).getContent();

        for (RegionDTO region : regions) {
            if (region.isOpen() && region.isAutoAppointment() != null && region.isAutoAppointment() && StringUtils.isNotBlank(region.getParkId())) {
                // 自动注册白名单开启
                List<ShipPlanDTO> shipPlanDTOS = shipPlanService.findAllByDeliverTime(region.getName(), todayBegin, ZonedDateTime.now(), 1);
                for (ShipPlanDTO planDTO : shipPlanDTOS) {
                    if (redisLongTemplate.opsForSet().isMember(AUTO_REGISTERED_PLAN_IDS, planDTO.getId())) {
                        continue;
                    }
                    log.info("ShipPlan [id={}], apply_id: {}, truckNumber: {}, need to auto register whitelist",
                        planDTO.getId(), planDTO.getApplyId(), planDTO.getTruckNumber());

                    UploadCarWhiteListMsg carWhiteListMsg = new UploadCarWhiteListMsg();
                    String now = ZonedDateTime.now().format(DATE_TIME_FORMAT);
                    carWhiteListMsg.setParkid(region.getParkId());
                    carWhiteListMsg.setCar_number(planDTO.getTruckNumber());
//                    carWhiteListMsg.setCard_id(planDTO.getTruckNumber());
                    carWhiteListMsg.setOperate_type(1);
                    carWhiteListMsg.setStartdate(now);
                    ZonedDateTime validDate = todayBegin.plusDays(1).minusSeconds(1);
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
                        saveTcpSendMessage(msg, region.getParkId(), carWhiteListMsg.getService());
                    }
                }
            }
        }
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void putAutoDeleteRegisterCarWhiteList() {
        ZonedDateTime begin = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime end = begin.plusDays(1).minusSeconds(1);
        List<ShipPlanDTO> plans = shipPlanService.findAllShouldDeleteCarWhiteList(begin, end);
        for (ShipPlanDTO plan : plans) {
            RegionDTO region = regionService.findByName(plan.getDeliverPosition());
            if (region == null || !region.isOpen()) {
                continue;
            }
            if (redisLongTemplate.opsForSet().isMember(AUTO_REGISTERED_DELETE_PLAN_IDS, plan.getId())) {
                continue;
            }
            if (plan.getLeaveTime() == null
                && plan.getAuditStatus().equals(Integer.valueOf(3))
                && plan.getUpdateTime().plusHours(1).isAfter(ZonedDateTime.now())) {
                // 装完货，再过8min，才将白名单删除
                continue;
            }

            log.info("ShipPlan id={} auditStatus = {}, truckNumber: {}, apply_id: {} " +
                    "need to remove car whitelist, add to delete queue",
                plan.getId(), plan.getAuditStatus(), plan.getTruckNumber(), plan.getApplyId());
            redisLongTemplate.opsForSet().add(AUTO_DELETE_PLAN_ID_QUEUE, plan.getId());
        }
    }

    /**
     * 删除固定车白名单
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void deleteRegisterCarWhiteList() throws JsonProcessingException {
        Set<String> openConnectionIds = Sets.newHashSet(cf.getOpenConnectionIds());
        if (openConnectionIds.isEmpty()) {
            return;
        }

        Set<Long> autoDeletePlanIds = redisLongTemplate.opsForSet().members(AUTO_DELETE_PLAN_ID_QUEUE);
        if (autoDeletePlanIds == null || autoDeletePlanIds.isEmpty()) {
            return;
        }

        log.info("Find {} ShipPlan ids need to delete car whitelist", autoDeletePlanIds.size());
        for (Long planId : autoDeletePlanIds) {
            Optional<ShipPlanDTO> planDTO = shipPlanService.findOne(planId);
            if (redisLongTemplate.opsForSet().isMember(AUTO_REGISTERED_DELETE_PLAN_IDS, planId)) {
                continue;
            }

            if (planDTO.isPresent()) {
                ShipPlanDTO plan = planDTO.get();
                RegionDTO region = regionService.findByName(plan.getDeliverPosition());
                if (region == null || StringUtils.isBlank(region.getParkId())) {
                    continue;
                }


                log.info("ShipPlan [id={}], apply_id: {}, truckNumber: {}, need to be removed from car whitelist",
                    plan.getId(), plan.getApplyId(), plan.getTruckNumber());

                sendDeleteCarWhiteListMsgForShipPlan(plan, region.getParkId());
                redisLongTemplate.opsForSet().add(AUTO_REGISTERED_DELETE_PLAN_IDS, plan.getId());
                redisLongTemplate.opsForSet().remove(AUTO_DELETE_PLAN_ID_QUEUE, plan.getId());
            }
        }
    }

    private boolean sendDeleteCarWhiteListMsgForShipPlan(ShipPlanDTO plan, String parkId) throws JsonProcessingException {
        String cardId = getCardIdByTruckNumber(plan.getTruckNumber());
        if (StringUtils.isBlank(cardId)) {
            log.warn("Cannot find card_id for truckNumber: {}", plan.getTruckNumber());
            return true;
        }

        DeleteCarWhiteListMsg deleteMsg = new DeleteCarWhiteListMsg();
        deleteMsg.setCar_number(plan.getTruckNumber());
        deleteMsg.setCard_id(cardId);
        deleteMsg.setParkid(parkId);
        String msg = objectMapper.writeValueAsString(deleteMsg);
        if (sendMessageToClient(msg, parkId)) {
            saveTcpSendMessage(msg, deleteMsg.getParkid(), deleteMsg.getService());
            return true;
        }
        return false;
    }

    private boolean sendMessageToClient(String msg, String parkId) {
        Set<String> openConnectionIds = Sets.newHashSet(cf.getOpenConnectionIds());
        if (!parkId2ConnectionId.containsKey(parkId)
            || parkId2ConnectionId.get(parkId) == null
            || !openConnectionIds.contains(parkId2ConnectionId.get(parkId))) {
            log.error("Failed to find connectionId for parkid: {}", parkId);
            return false;
        }

        String connectionId = parkId2ConnectionId.get(parkId);
        Message<String> message = MessageBuilder.withPayload(msg)
            .setHeader(IpHeaders.CONNECTION_ID, connectionId)
            .build();

        if (tcpOut.send(message)) {
            log.info("Send TCP message to client, parkId: {}, success: {}", parkId, msg);
            return true;
        } else {
            return false;
        }
    }


    @Scheduled(fixedRate = 10 * 1000)
    public void uploadCarWhiteList() {
        Set<String> openConnectionIds = Sets.newHashSet(cf.getOpenConnectionIds());
        if (openConnectionIds.isEmpty()) {
            return;
        }
        Set<Long> appointmentIds = redisLongTemplate.opsForSet().members(REDIS_KEY_UPLOAD_CAR_WHITELIST);
        if (!CollectionUtils.isEmpty(appointmentIds)) {
            log.info("Find {} appointmentIds need to upload car whitelist", appointmentIds.size());
            for (Long appointmentId : appointmentIds) {
                try {
                    UploadCarWhiteListMsg uploadCarWhiteListMsg = generateUploadCarWhiteListMsg(appointmentId);
                    if (uploadCarWhiteListMsg == null) {
                        redisLongTemplate.opsForSet().remove(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointmentId);
                        continue;
                    }
                    String sendMsg = objectMapper.writeValueAsString(uploadCarWhiteListMsg);
                    if (sendMessageToClient(sendMsg, uploadCarWhiteListMsg.getParkid())) {
                        redisLongTemplate.opsForSet().remove(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointmentId);
                        log.info("Upload car whitelist successfully. appointmentId: {}, msg: {}", appointmentId, sendMsg);
                        saveTcpSendMessage(sendMsg, uploadCarWhiteListMsg.getParkid(), uploadCarWhiteListMsg.getService());
                    }
                } catch (Exception e) {
                    log.error("Failed to upload car whitelist, appointmendId: {}", appointmentId, e);
                }
            }
        }

        Set<Long> appointmentIdsDeleteQueue = redisLongTemplate.opsForSet().members(REDIS_KEY_DELETE_CAR_WHITELIST);
        if (!CollectionUtils.isEmpty(appointmentIdsDeleteQueue)) {
            log.info("Find {} appointmentIds need to delete car whitelist", appointmentIdsDeleteQueue.size());
            for (Long appointmentId : appointmentIdsDeleteQueue) {
                try {
                    DeleteCarWhiteListMsg deleteCarWhiteListMsg = generateDeleteCarWhiteListMsg(appointmentId);
                    if (deleteCarWhiteListMsg == null) {
                        redisLongTemplate.opsForSet().remove(REDIS_KEY_DELETE_CAR_WHITELIST, appointmentId);
                        continue;
                    }

                    String sendMsg = objectMapper.writeValueAsString(deleteCarWhiteListMsg);
                    if (sendMessageToClient(sendMsg, deleteCarWhiteListMsg.getParkid())) {
                        log.info("Delete car whitelist successfully. appointmentId: {}, msg: {}", appointmentId, sendMsg);
                        redisLongTemplate.opsForSet().remove(REDIS_KEY_DELETE_CAR_WHITELIST, appointmentId);
                        saveTcpSendMessage(sendMsg, deleteCarWhiteListMsg.getParkid(), deleteCarWhiteListMsg.getService());
                    }
                } catch (Exception e) {
                    log.error("Failed to delete car whitelist, appointmentId: {}", appointmentId, e);
                }
            }
        }
    }

    private void updateCarInOutTime(String parkId, String truckNumber, String service, String carInTime, String carOutTime) {
        try {
            appointmentService.updateCarInAndOutTime(parkId, truckNumber, service, carInTime, carOutTime);
        } catch (Exception e) {
            log.error("failed to execute appointmentService.updateCarInAndOutTime(), " +
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
