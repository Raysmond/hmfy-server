package com.shield.service.event;

import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.service.*;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.shield.config.Constants.REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS;

@Service
@Slf4j
public class AppointmentEventListener {

    private final ShipPlanService shipPlanService;

    private final RedisTemplate<String, Long> redisLongTemplate;

    private final WxMpMsgService wxMpMsgService;

    private final PenaltyService penaltyService;

    private final RegionService regionService;

    private final CarWhiteListManager carWhiteListManager;

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentEventListener(
        ShipPlanService shipPlanService,
        @Qualifier("redisLongTemplate") RedisTemplate<String, Long> redisLongTemplate,
        WxMpMsgService wxMpMsgService,
        PenaltyService penaltyService,
        RegionService regionService,
        CarWhiteListManager carWhiteListManager, AppointmentService appointmentService) {
        this.shipPlanService = shipPlanService;
        this.redisLongTemplate = redisLongTemplate;
        this.wxMpMsgService = wxMpMsgService;
        this.penaltyService = penaltyService;
        this.regionService = regionService;
        this.carWhiteListManager = carWhiteListManager;
        this.appointmentService = appointmentService;
    }

    @Async
    @TransactionalEventListener
    public void handleAppointmentChangedEvent(AppointmentChangedEvent appointmentChangedEvent) {
        AppointmentDTO before = appointmentChangedEvent.getBefore();
        AppointmentDTO after = appointmentChangedEvent.getAfter();
        log.info("[EVENT] listen on AppointmentChangedEvent event, before: {}, after: {}", before, after);

        if (before != null) {
            if (before.getStatus() == AppointmentStatus.ENTER && after.getStatus() == AppointmentStatus.LEAVE) {
                afterAppointmentLeave(before, after);
            }

            if (before.getStatus() == AppointmentStatus.START && after.getStatus() == AppointmentStatus.ENTER) {
                afterAppointmentEnter(before, after);
            }

            if (before.getStatus() == AppointmentStatus.START && after.getStatus() == AppointmentStatus.EXPIRED) {
                afterAppointmentExpired(after);
            }

            if (!before.getStatus().equals(AppointmentStatus.CANCELED) && after.getStatus().equals(AppointmentStatus.CANCELED)) {
                afterAppointmentCanceled(before, after);
            }

            if (before.isValid() && !after.isValid()) {
                afterAppointmentInvalid(before, after);
            }
        }

        if (after.getStatus() == AppointmentStatus.START && (before == null || before.getStatus() != AppointmentStatus.START)) {
            // 预约成功
            RegionDTO region = regionService.findOne(after.getRegionId()).get();
            if (after.getApplyId() != null) {
                ShipPlanDTO plan = shipPlanService.findOneByApplyId(after.getApplyId());
                plan.setAllowInTime(after.getStartTime().plusHours(region.getValidTime()));
                shipPlanService.save(plan);
            }
            carWhiteListManager.registerCarWhiteList(after);
            wxMpMsgService.sendAppointmentSuccessMsg(after);
        }

        if (before != null && after.isVip()
            && ((before.getEnterTime() == null && after.getEnterTime() != null)
            || (before.getLeaveTime() == null && after.getLeaveTime() != null))) {
            redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS, after.getId());
        }

    }

    private void afterAppointmentEnter(AppointmentDTO before, AppointmentDTO after) {
        log.info("TRIGGER afterAppointmentEnter...");
        if (after.getApplyId() != null) {
            ShipPlanDTO plan = shipPlanService.findOneByApplyId(after.getApplyId());
            if (plan.getAuditStatus().equals(4)) {
                // 如果计划过期了，则将这个预约作废。此时他进行的应该是下一个计划
                after.setValid(false);
                log.info("Appointment in ENTER status but plan is expired, set valid to false, truckNumber: {}, appointmentId: {}, applyId: {}", after.getLicensePlateNumber(), after.getId(), after.getApplyId());
                appointmentService.save(after);
            }
        }
    }

    private void afterAppointmentLeave(AppointmentDTO before, AppointmentDTO after) {
        log.info("TRIGGER afterAppointmentLeave...");
        carWhiteListManager.deleteCarWhiteList(after);
    }

    private void afterAppointmentExpired(AppointmentDTO appointment) {
        log.info("TRIGGER afterAppointmentExpired...");
        carWhiteListManager.deleteCarWhiteList(appointment);

        // 惩罚
        if (appointment.getUserId() != null && !appointment.isVip()) {
            penaltyService.putUserInExpirePenalty(appointment.getUserId());
        }

        // 发送过期消息
        wxMpMsgService.sendAppointmentExpireMsg(appointment);
    }

    private void afterAppointmentInvalid(AppointmentDTO before, AppointmentDTO after) {
        log.info("TRIGGER afterAppointmentInvalid...");

        if (before.getStatus().equals(AppointmentStatus.START)) {
            carWhiteListManager.deleteCarWhiteList(after);

            if (before.getApplyId() != null) {
                ShipPlanDTO plan = shipPlanService.findOneByApplyId(before.getApplyId());
                plan.setAllowInTime(null);
                shipPlanService.save(plan);
            }

            wxMpMsgService.sendAppointmentCancelMsg(after);
        }
    }

    private void afterAppointmentCanceled(AppointmentDTO before, AppointmentDTO after) {
        log.info("TRIGGER afterAppointmentCanceled...");
        if (before.getStatus().equals(AppointmentStatus.START)) {
            carWhiteListManager.deleteCarWhiteList(after);

            if (before.getApplyId() != null) {
                ShipPlanDTO plan = shipPlanService.findOneByApplyId(before.getApplyId());
                plan.setAllowInTime(null);
                shipPlanService.save(plan);
            }

            wxMpMsgService.sendAppointmentCancelMsg(after);
        }

        if (!after.isVip() && after.getUserId() != null) {
            switch (before.getStatus()) {
                case WAIT:
                    penaltyService.putUserInCancelWaitPenalty(after.getUserId());
                    break;
                case START:
                    penaltyService.putUserInCancelPenalty(after.getUserId());
                    break;
            }
        }


    }
}
