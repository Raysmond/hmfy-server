package com.shield.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.ParkMsgDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.tcp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
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
import java.util.List;
import java.util.Map;
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
    private AbstractServerConnectionFactory cf;

    private static final Gson GSON = new Gson();

    private static Map<String, String> parkId2ConnectionId = Maps.newHashMap();

    @Autowired
    private ShipPlanService shipPlanService;

    public static final String AUTO_REGISTERED_PLAN_IDS = "set_auto_registered_ship_plan_ids";
    public static final String AUTO_REGISTERED_DELETE_PLAN_IDS = "set_auto_deleted_ship_plan_ids";

    public static final String REDIS_KEY_UPLOAD_CAR_WHITELIST = "upload_car_whitelist_appointment_ids";
    public static final String REDIS_KEY_DELETE_CAR_WHITELIST = "delete_car_whitelist_appointment_ids";


    public String handle(Message<String> msg) {
        log.info("Receive TCP msg: {}", msg.getPayload());
        try {
            ServiceResponse response = handleService(msg);
            String responseMsg = objectMapper.writeValueAsString(response);
            log.info("Handle msg success, response: {}", responseMsg);
            return responseMsg;
        } catch (Exception e) {
            log.error("failed to handle msg: {}, exception: {}", msg.getPayload(), e);
            return "{\"service\": \"\", \"result_code\":1, \"message\": \"处理失败\"}\r\n";
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
            parkMsgDTO.setParkid(data.get("parkid").getAsString());
            parkMsgDTO.setService(data.get("service").getAsString());
            parkMsgDTO.setCreateTime(ZonedDateTime.now());
            if (!parkMsgDTO.getService().equals("heartbeat")) {
                parkMsgService.save(parkMsgDTO);
            }
        } catch (Exception e) {
            log.error("failed to save ParkMsg, msg: {}", msg);
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
                log.info("TCP socket ip_connectionId: {}, parkid: {}", connectionId, heartBeatMsg.getParkid());
                return new ServiceResponse(heartBeatMsg.getService(), 0, "在线");
            case "uploadcarin":
                UploadCarInMsg inMsg = objectMapper.readValue(msg, UploadCarInMsg.class);
                updateCarInOutTime(inMsg.getParkid(), inMsg.getCar_number(), inMsg.getIn_time(), null);
                return new UploadCarInOutResponse(inMsg.getService(), 0, "上传成功", inMsg.getOrder_id());
            case "uploadcarout":
                UploadCarOutMsg outMsg = objectMapper.readValue(msg, UploadCarOutMsg.class);
                updateCarInOutTime(outMsg.getParkid(), outMsg.getCar_number(), null, outMsg.getOut_time());
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


    @Scheduled(fixedRate = 60 * 1000)
    public void autoRegisterCarWhiteList() throws JsonProcessingException {
        Set<String> openConnectionIds = Sets.newHashSet(cf.getOpenConnectionIds());
        if (openConnectionIds.isEmpty()) {
            return;
        }
        ZonedDateTime todayBegin = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime todayEnd = todayBegin.plusDays(1L);
        List<RegionDTO> regions = regionService.findAll(PageRequest.of(0, 10000)).getContent();

        List<ShipPlanDTO> shipPlanDTOS = shipPlanService.findAllByDeliverTime(todayBegin, todayEnd, 1);

        for (RegionDTO region : regions) {
            if (region.isOpen() && region.isAutoAppointment() != null && region.isAutoAppointment() && StringUtils.isNotBlank(region.getParkId())) {
                // 自动注册白名单开启
                for (ShipPlanDTO planDTO : shipPlanDTOS) {
                    if (StringUtils.isBlank(planDTO.getDeliverPosition()) || !planDTO.getDeliverPosition().equals(region.getName())) {
                        continue;
                    }
                    if (redisLongTemplate.opsForSet().isMember(AUTO_REGISTERED_PLAN_IDS, planDTO.getId())) {
                        continue;
                    }
                    log.info("ShipPlan [id={}], apply_id: {}, truckNumber: {}, need to auto register whitelist",
                        planDTO.getId(), planDTO.getApplyId(), planDTO.getTruckNumber());

                    UploadCarWhiteListMsg carWhiteListMsg = new UploadCarWhiteListMsg();
                    String now = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    carWhiteListMsg.setParkid(region.getParkId());
                    carWhiteListMsg.setCar_number(planDTO.getTruckNumber());
                    carWhiteListMsg.setCard_id("sp_" + planDTO.getId());
                    carWhiteListMsg.setOperate_type(1);
                    carWhiteListMsg.setStartdate(now);
                    carWhiteListMsg.setValiddate(todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    carWhiteListMsg.setCreate_time(now);
                    carWhiteListMsg.setModify_time(now);

                    if (sendMessageToClient(objectMapper.writeValueAsString(carWhiteListMsg), region.getParkId())) {
                        redisLongTemplate.opsForSet().add(AUTO_REGISTERED_PLAN_IDS, planDTO.getId());
                    }
                }
            }
        }
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void deleteRegisterCarWhiteList() throws JsonProcessingException {
        Set<String> openConnectionIds = Sets.newHashSet(cf.getOpenConnectionIds());
        if (openConnectionIds.isEmpty()) {
            return;
        }
        ZonedDateTime todayBegin = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime todayEnd = todayBegin.plusDays(1L);
        List<RegionDTO> regions = regionService.findAll(PageRequest.of(0, 10000)).getContent();

        List<ShipPlanDTO> shipPlanDTOS = shipPlanService.findAllShouldDeleteCarWhiteList(todayBegin, todayEnd);

        for (RegionDTO region : regions) {
            if (region.isOpen() && StringUtils.isNotBlank(region.getParkId()) && region.isAutoAppointment() != null && region.isAutoAppointment()) {
                for (ShipPlanDTO planDTO : shipPlanDTOS) {
                    if (StringUtils.isBlank(planDTO.getDeliverPosition()) || !planDTO.getDeliverPosition().equals(region.getName())) {
                        continue;
                    }
                    if (redisLongTemplate.opsForSet().isMember(AUTO_REGISTERED_DELETE_PLAN_IDS, planDTO.getId())) {
                        continue;
                    }
                    log.info("ShipPlan [id={}], apply_id: {}, truckNumber: {}, need to be removed from car whitelist",
                        planDTO.getId(), planDTO.getApplyId(), planDTO.getTruckNumber());

                    DeleteCarWhiteListMsg msg = new DeleteCarWhiteListMsg();
                    msg.setCar_number(planDTO.getTruckNumber());
                    msg.setCard_id("sp_" + planDTO.getId());
                    msg.setParkid(region.getParkId());
                    if (sendMessageToClient(objectMapper.writeValueAsString(msg), region.getParkId())) {
                        redisLongTemplate.opsForSet().add(AUTO_REGISTERED_DELETE_PLAN_IDS, planDTO.getId());
                    }
                }
            }
        }
    }

    private boolean sendMessageToClient(String msg, String parkId) {
        Set<String> openConnectionIds = Sets.newHashSet(cf.getOpenConnectionIds());
        if (!parkId2ConnectionId.containsKey(parkId)
            || parkId2ConnectionId.get(parkId) == null
            || !openConnectionIds.contains(parkId2ConnectionId.get(parkId))) {
            log.error("Failed to find connectionId for parkid: {}", parkId);

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
                    }
                } catch (Exception e) {
                    log.error("Failed to upload car whitelist, appointmendId: {}, exception: {}", appointmentId, e);
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
                    }

                } catch (Exception e) {
                    log.error("Failed to delete car whitelist, appointmentId: {}, exception: {}", appointmentId, e);
                }
            }
        }
    }

    private void updateCarInOutTime(String parkId, String truckNumber, String carInTime, String carOutTime) {
        try {
            appointmentService.updateCarInAndOutTime(parkId, truckNumber, carInTime, carOutTime);
        } catch (Exception e) {
            log.error("failed to execute appointmentService.updateCarInAndOutTime(), " +
                "parkId: {}, truckNumber: {}, carInTime: {}, carOutTime: {}", parkId, truckNumber, carInTime, carOutTime);
        }
    }



    private void handleWhiteListResponse(WhiteListMsgResponse whiteListMsgResponse) {
        // pass
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
            carWhiteListMsg.setCard_id("app_" + appointment.getId());
            if (StringUtils.isNotBlank(appointment.getUserLogin())) {
                carWhiteListMsg.setCarusername(appointment.getUserLogin());
            }
            carWhiteListMsg.setOperate_type(1); // 注册
            carWhiteListMsg.setStartdate(now);
            carWhiteListMsg.setValiddate(ZonedDateTime.now().plusHours(12).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            carWhiteListMsg.setCreate_time(now);
            carWhiteListMsg.setModify_time(now);
            return carWhiteListMsg;
        } catch (Exception e) {
            log.error("failed execute uploadCarWhiteList(), appointmentId: {}", appointmentId);
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
            carWhiteListMsg.setParkid(regionDTO.getParkId());
            carWhiteListMsg.setCar_number(appointment.getLicensePlateNumber());
            carWhiteListMsg.setCard_id("app_" + appointment.getId());
            return carWhiteListMsg;
        } catch (Exception e) {
            log.error("failed execute uploadCarWhiteList(), appointmentId: {}", appointmentId);
            return null;
        }
    }


}
