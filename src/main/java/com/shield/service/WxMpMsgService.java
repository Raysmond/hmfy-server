package com.shield.service;

import com.google.common.collect.Lists;
import com.shield.domain.*;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.repository.ShipPlanRepository;
import com.shield.repository.WxMaUserRepository;
import com.shield.security.AuthoritiesConstants;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class WxMpMsgService {
    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WxMaUserService wxMaUserService;

    @Autowired
    private WxMaUserRepository wxMaUserRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentService appointmentService;

    private static final String MINI_PROGRAM_APP_ID = "wx32a67eb90d6d98e9";

    private String getMpUserOpenIdByUserId(Long userId) {
        Optional<User> findUser = userService.getUserWithAuthorities(userId);
        if (findUser.isPresent() && findUser.get().getWxMaUser() != null && StringUtils.isNotBlank(findUser.get().getWxMaUser().getUnionId())) {
            User user = findUser.get();
            String unionId = findUser.get().getWxMaUser().getUnionId();
            log.info("Need to send appointment success msg to user {}, unionid: {}", user.getLogin(), unionId);
            Optional<WxMaUser> findWxMaUser = wxMaUserRepository.findByAppIdAndUnionId(wxMpService.getWxMpConfigStorage().getAppId(), unionId);
            if (findWxMaUser.isPresent()) {
                WxMaUser wxMaUser = findWxMaUser.get();
                return wxMaUser.getOpenId();
            } else {
                log.info("Cannot find mp user openid by unionid: {}", unionId);
            }
        }
        return null;
    }

    @Autowired
    private RegionService regionService;

    @Autowired
    private ShipPlanRepository shipPlanRepository;

    private Long getRegionValidTime(Long regionId) {
        return regionService.findOne(regionId).get().getValidTime().longValue();
    }

    private String getShipPlanProductName(Long applyId) {
        List<ShipPlan> plans = shipPlanRepository.findByApplyIdIn(Lists.newArrayList(applyId));
        if (plans.isEmpty()) {
            return "??????";
        } else {
            return plans.get(0).getProductName();
        }
    }

    @Async
    public void sendAlertMsg(Long userId, String openId, String title, String content, String remark) {
        try {
            if (StringUtils.isBlank(openId)) {
                openId = getMpUserOpenIdByUserId(userId);
                if (StringUtils.isBlank(openId)) {
                    log.info("Cannot find openId for userId: {}", userId);
                    return;
                }
            }
            WxMpTemplateMessage msg = new WxMpTemplateMessage();
            msg.setTemplateId("dGYzobbYF9PPIOjTouDmtw_aZrbSAVHdxtMCRYBX3Fk");
            msg.setToUser(openId);
            msg.setMiniProgram(new WxMpTemplateMessage.MiniProgram(MINI_PROGRAM_APP_ID, "pages/index/index", true));
            List<WxMpTemplateData> data = Lists.newArrayList();
            data.add(new WxMpTemplateData("first", title));
            data.add(new WxMpTemplateData("keyword1", content));
            data.add(new WxMpTemplateData("keyword2", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            data.add(new WxMpTemplateData("remark", remark));
            msg.setData(data);
            wxMpService.getTemplateMsgService().sendTemplateMsg(msg);
        } catch (Exception e) {
            log.error("failed to send alert msg, userId: {}, title: {}, content: {}, remark: {}", userId, title, content, remark, e);
        }
    }

    @Async
    public void sendAppointmentSuccessMsg(AppointmentDTO appointment) {
        if (appointment.getUserId() == null) {
            return;
        }
        try {
            String openId = getMpUserOpenIdByUserId(appointment.getUserId());
            if (openId == null) {
                return;
            }
            WxMpTemplateMessage msg = new WxMpTemplateMessage();
            msg.setTemplateId("Z6BntQH_HujX_wSs8hOJUccLhJnCEHdklYDLfKKs_2w");
            msg.setToUser(openId);
            msg.setMiniProgram(new WxMpTemplateMessage.MiniProgram(MINI_PROGRAM_APP_ID, "pages/index/index", true));
            List<WxMpTemplateData> data = Lists.newArrayList();

            String first = String.format("????????? %s ????????????%s????????????",
                appointment.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                appointment.getRegionName());
            data.add(new WxMpTemplateData("first", first));
            data.add(new WxMpTemplateData("keyword1", appointment.getNumber().toString()));
            data.add(new WxMpTemplateData("keyword2", "?????????????????????"));
            data.add(new WxMpTemplateData("keyword3", appointment.getLicensePlateNumber()));
            data.add(new WxMpTemplateData("keyword4",
                appointment.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    + " - "
                    + appointment.getStartTime().plusHours(getRegionValidTime(appointment.getRegionId())).format(DateTimeFormatter.ofPattern("HH:mm"))));
            data.add(new WxMpTemplateData("keyword5", getShipPlanProductName(appointment.getApplyId())));
            data.add(new WxMpTemplateData("remark", "???????????????????????????????????????????????????"));
            msg.setData(data);
            wxMpService.getTemplateMsgService().sendTemplateMsg(msg);
        } catch (Exception e) {
            log.error("failed to send appointment success msg, appointmentId: {}", appointment.getId(), e);
        }
    }


    /**
     * ???????????????????????????
     */
    @Async
    public void sendOutgateApplicationSuccessMsg(ShipPlanDTO plan, AppointmentDTO appointment, ZonedDateTime startTime, ZonedDateTime endTime, String gateNumber) {
        if (appointment.getUserId() == null) {
            return;
        }
        try {
            String openId = getMpUserOpenIdByUserId(appointment.getUserId());
            if (openId == null) {
                return;
            }
            WxMpTemplateMessage msg = new WxMpTemplateMessage();
            msg.setTemplateId("Z6BntQH_HujX_wSs8hOJUccLhJnCEHdklYDLfKKs_2w");
            msg.setToUser(openId);
            msg.setMiniProgram(new WxMpTemplateMessage.MiniProgram(MINI_PROGRAM_APP_ID, "pages/index/index", true));
            List<WxMpTemplateData> data = Lists.newArrayList();

            String first = String.format("????????? %s ????????????%s???????????????",
                appointment.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                appointment.getRegionName());
            data.add(new WxMpTemplateData("first", first));
            data.add(new WxMpTemplateData("keyword1", appointment.getNumber().toString()));
            data.add(new WxMpTemplateData("keyword2", "??????" + gateNumber + "??????"));
            data.add(new WxMpTemplateData("keyword3", appointment.getLicensePlateNumber()));
            data.add(new WxMpTemplateData("keyword4",
                startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                    + " - "
                    + endTime.format(DateTimeFormatter.ofPattern("HH:mm"))));
            data.add(new WxMpTemplateData("keyword5", getShipPlanProductName(appointment.getApplyId())));
            data.add(new WxMpTemplateData("remark", "???????????????????????????????????????????????????"));
            msg.setData(data);
            wxMpService.getTemplateMsgService().sendTemplateMsg(msg);
        } catch (Exception e) {
            log.error("success msg failed, sendOutgateApplicationSuccessMsg, applyId: {}, truckNumber: {}", plan.getApplyId(), plan.getTruckNumber(), e);
        }
    }

    @Async
    public void sendAppointmentCancelMsg(AppointmentDTO appointment) {
        if (appointment.getUserId() == null || (appointment.isVip() != null && appointment.isVip())) {
            return;
        }
        try {
            String openId = getMpUserOpenIdByUserId(appointment.getUserId());
            if (openId == null) {
                return;
            }
            WxMpTemplateMessage msg = new WxMpTemplateMessage();
            msg.setTemplateId("PEDVuVu74LAK4aNqJGA_Eafz-yi-JXuUlcJh7dFbKdY");
            msg.setToUser(openId);
            msg.setMiniProgram(new WxMpTemplateMessage.MiniProgram(MINI_PROGRAM_APP_ID, "pages/index/index", true));
            List<WxMpTemplateData> data = Lists.newArrayList();

            String allowPeriod = appointment.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                + " - "
                + appointment.getStartTime().plusHours(getRegionValidTime(appointment.getRegionId())).format(DateTimeFormatter.ofPattern("HH:mm"));

            data.add(
                new WxMpTemplateData("first", String.format("????????????%s %s???%s???????????????",
                    appointment.getStartTime().format(DateTimeFormatter.ofPattern("MM???dd???")),
                    allowPeriod,
                    appointment.getRegionName()
                )));
            data.add(new WxMpTemplateData("keyword1", appointment.getNumber().toString()));
            data.add(new WxMpTemplateData("keyword2", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            data.add(new WxMpTemplateData("keyword3", appointment.getLicensePlateNumber()));
            data.add(new WxMpTemplateData("remark", "???????????????????????????????????????????????????"));
            msg.setData(data);
            wxMpService.getTemplateMsgService().sendTemplateMsg(msg);
        } catch (Exception e) {
            log.error("failed to send appointment success msg, appointmentId: {}", appointment.getId(), e);
        }
    }

    @Async
    public void sendAppointmentExpireMsg(AppointmentDTO appointment) {
        if (appointment.getUserId() == null || (appointment.isVip() != null && appointment.isVip())) {
            return;
        }
        try {
            String openId = getMpUserOpenIdByUserId(appointment.getUserId());
            if (openId == null) {
                return;
            }
            WxMpTemplateMessage msg = new WxMpTemplateMessage();
            msg.setTemplateId("PEDVuVu74LAK4aNqJGA_Eafz-yi-JXuUlcJh7dFbKdY");
            msg.setToUser(openId);
            msg.setMiniProgram(new WxMpTemplateMessage.MiniProgram(MINI_PROGRAM_APP_ID, "pages/index/index", true));
            List<WxMpTemplateData> data = Lists.newArrayList();

            String allowPeriod = appointment.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                + " - "
                + appointment.getStartTime().plusHours(getRegionValidTime(appointment.getRegionId())).format(DateTimeFormatter.ofPattern("HH:mm"));

            data.add(
                new WxMpTemplateData("first", String.format("??????%s %s ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????",
                    appointment.getStartTime().format(DateTimeFormatter.ofPattern("MM???dd???")),
                    allowPeriod
                )));
            data.add(new WxMpTemplateData("keyword1", appointment.getNumber().toString()));
            data.add(new WxMpTemplateData("keyword2", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            data.add(new WxMpTemplateData("keyword3", appointment.getLicensePlateNumber()));
            data.add(new WxMpTemplateData("remark", "???????????????????????????????????????????????????"));
            msg.setData(data);
            wxMpService.getTemplateMsgService().sendTemplateMsg(msg);
        } catch (Exception e) {
            log.error("failed to send appointment success msg, appointmentId: {}", appointment.getId(), e);
        }
    }

    @Async
    public void sendLoadStartAlertMsgToWxUser(ShipPlan delayedPlan, Integer loadAlertTime) {
        try {
            AppointmentDTO appointmentDTO = appointmentService.findLastByApplyId(delayedPlan.getApplyId());
            if (appointmentDTO != null && appointmentDTO.getUserId() != null && Boolean.FALSE.equals(appointmentDTO.isVip()) && appointmentDTO.getStatus().equals(AppointmentStatus.ENTER)) {
                String remark = String.format("???????????????%s", delayedPlan.getGateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                this.sendAlertMsg(appointmentDTO.getUserId(), null,
                    String.format("???????????????????????????%s??????????????????", delayedPlan.getApplyId().toString()),
                    String.format("??????%s???%s????????????%d???????????????????????????", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition(), loadAlertTime / 60),
                    remark);

                if (appointmentDTO.getRegionId() != null) {
                    Page<UserDTO> users = userService.getAllManagedUsersByRegionId(PageRequest.of(0, 1000), appointmentDTO.getRegionId());
                    for (UserDTO user : users.getContent()) {
                        if (user.getAuthorities().contains(AuthoritiesConstants.REGION_ADMIN)) {
                            this.sendAlertMsg(user.getId(), null,
                                String.format("????????????%s??????????????????", delayedPlan.getApplyId().toString()),
                                String.format("??????%s???%s????????????%d???????????????????????????", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition(), loadAlertTime / 60),
                                remark);
                        }
                    }
                }

                for (String openid : Lists.newArrayList("oZBny01fYBk-P1zpYZH00vm3uFQI", "oZBny09ivtl8EN8IVcdQKxyfA65c")) {
                    this.sendAlertMsg(null, openid,
                        String.format("????????????%s??????????????????", delayedPlan.getApplyId().toString()),
                        String.format("??????%s???%s????????????%d???????????????????????????", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition(), loadAlertTime / 60),
                        remark);
                }
            }
        } catch (Exception e) {
            log.error("failed to send alert msg sendAlertMsgToWxUser()", e);
        }
    }

    @Async
    public void sendLeaveAlertMsg(ShipPlanDTO delayedPlan, Integer leaveAlertTime) {
        try {
            AppointmentDTO appointmentDTO = appointmentService.findLastByApplyId(delayedPlan.getApplyId());
            if (appointmentDTO != null && appointmentDTO.getUserId() != null && Boolean.FALSE.equals(appointmentDTO.isVip()) && appointmentDTO.getStatus().equals(AppointmentStatus.ENTER)) {
                String remark;
                if (delayedPlan.getLoadingEndTime() != null) {
                    remark = String.format("???????????????%s??????????????????%s", delayedPlan.getGateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), delayedPlan.getLoadingEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                } else {
                    remark = String.format("???????????????%s", delayedPlan.getGateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }

                this.sendAlertMsg(appointmentDTO.getUserId(), null,
                    String.format("???????????????????????????%s??????????????????", delayedPlan.getApplyId().toString()),
                    String.format("??????%s???%s????????????%d????????????????????????", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition(), leaveAlertTime),
                    remark);

                if (appointmentDTO.getRegionId() != null) {
                    Page<UserDTO> users = userService.getAllManagedUsersByRegionId(PageRequest.of(0, 1000), appointmentDTO.getRegionId());
                    for (UserDTO user : users.getContent()) {
                        if (user.getAuthorities().contains(AuthoritiesConstants.REGION_ADMIN)) {
                            this.sendAlertMsg(user.getId(), null,
                                String.format("????????????%s??????????????????", delayedPlan.getApplyId().toString()),
                                String.format("??????%s???%s????????????%d????????????????????????", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition(), leaveAlertTime),
                                remark);
                        }
                    }
                }

                for (String openid : Lists.newArrayList("oZBny01fYBk-P1zpYZH00vm3uFQI", "oZBny09ivtl8EN8IVcdQKxyfA65c")) {
                    this.sendAlertMsg(null, openid,
                        String.format("????????????%s??????????????????", delayedPlan.getApplyId().toString()),
                        String.format("??????%s???%s????????????%d????????????????????????", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition(), leaveAlertTime),
                        remark);
                }
            }
        } catch (Exception e) {
            log.error("failed to send alert msg sendAlertMsgToWxUser() {}", e.getMessage());
        }
    }
}
