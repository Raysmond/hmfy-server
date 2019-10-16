package com.shield.service.schedule;

import com.google.common.collect.Lists;
import com.shield.domain.Region;
import com.shield.domain.ShipPlan;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.security.AuthoritiesConstants;
import com.shield.service.AppointmentService;
import com.shield.service.UserService;
import com.shield.service.WxMpMsgService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.UserDTO;
import io.github.jhipster.config.JHipsterConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.shield.config.Constants.LOADING_START_ALERT_EXPIRED_TIMES_HOURS_AFTER_ENTER;

@Service
@Slf4j
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class ShipPlanScheduleService {

    private final RegionRepository regionRepository;

    private final ShipPlanRepository shipPlanRepository;

    private final AppointmentService appointmentService;

    private final WxMpMsgService wxMpMsgService;

    private final UserService userService;

    @Autowired
    public ShipPlanScheduleService(
        RegionRepository regionRepository,
        ShipPlanRepository shipPlanRepository,
        AppointmentService appointmentService,
        WxMpMsgService wxMpMsgService,
        UserService userService
    ) {
        this.regionRepository = regionRepository;
        this.shipPlanRepository = shipPlanRepository;
        this.appointmentService = appointmentService;
        this.wxMpMsgService = wxMpMsgService;
        this.userService = userService;
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void checkAndAlert() {
        List<Region> regions = regionRepository.findAll().stream().filter(Region::isOpen).collect(Collectors.toList());
        for (Region region : regions) {
            List<ShipPlan> plans = shipPlanRepository.findAllByGateTime(ZonedDateTime.now().minusHours(LOADING_START_ALERT_EXPIRED_TIMES_HOURS_AFTER_ENTER + 1), region.getName());
            for (ShipPlan plan : plans) {
                if (plan.getLoadingStartTime() == null || plan.getGateTime() == null || plan.isTareAlert()) {
                    continue;
                }
                if (plan.getGateTime().plusHours(LOADING_START_ALERT_EXPIRED_TIMES_HOURS_AFTER_ENTER).isBefore(plan.getLoadingStartTime())) {
                    plan.setTareAlert(true);
                    shipPlanRepository.save(plan);
                    sendAlertMsgToWxUser(plan);
                }
            }
        }
    }

    private void sendAlertMsgToWxUser(ShipPlan delayedPlan) {
        try {
            AppointmentDTO appointmentDTO = appointmentService.findLastByApplyId(delayedPlan.getApplyId());
            if (appointmentDTO != null && appointmentDTO.getUserId() != null && Boolean.FALSE.equals(appointmentDTO.isVip()) && appointmentDTO.getStatus().equals(AppointmentStatus.ENTER)) {
                String remark = String.format("进厂时间：%s", delayedPlan.getGateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                wxMpMsgService.sendAlertMsg(appointmentDTO.getUserId(), null,
                    String.format("您好，您的提货计划%s有异常情况。", delayedPlan.getApplyId().toString()),
                    String.format("车牌%s在%s进厂之后三小时还未上磅提货！", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition()),
                    remark);

                if (appointmentDTO.getRegionId() != null) {
                    Page<UserDTO> users = userService.getAllManagedUsersByRegionId(PageRequest.of(0, 1000), appointmentDTO.getRegionId());
                    for (UserDTO user : users.getContent()) {
                        if (user.getAuthorities().contains(AuthoritiesConstants.REGION_ADMIN)) {
                            wxMpMsgService.sendAlertMsg(user.getId(), null,
                                String.format("提货计划%s有异常情况。", delayedPlan.getApplyId().toString()),
                                String.format("车牌%s在%s进厂之后三小时还未上磅提货！", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition()),
                                remark);
                        }
                    }
                }

                for (String openid : Lists.newArrayList("oZBny01fYBk-P1zpYZH00vm3uFQI", "oZBny09ivtl8EN8IVcdQKxyfA65c")) {
                    wxMpMsgService.sendAlertMsg(null, openid,
                        String.format("提货计划%s有异常情况。", delayedPlan.getApplyId().toString()),
                        String.format("车牌%s在%s进厂之后三小时还未上磅提货！", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition()),
                        remark);
                }
            }
        } catch (Exception e) {
            log.error("failed to send alert msg sendAlertMsgToWxUser() {}", e.getMessage());
        }
    }


    /**
     * 每天凌晨1点，将前一天待提货的计划改成过期
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void autoExpireShipPlan() {
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        List<ShipPlan> shipPlans = shipPlanRepository.findAllNeedToExpire(today);
        if (!shipPlans.isEmpty()) {
            log.info("Find {} ShipPlan in status 1, should be expired");
            for (ShipPlan shipPlan : shipPlans) {
                shipPlan.setAuditStatus(4);
                shipPlan.setUpdateTime(ZonedDateTime.now());
            }
            shipPlanRepository.saveAll(shipPlans);
        }
    }
}
