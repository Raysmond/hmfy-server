package com.shield.service.event;

import com.shield.service.*;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.schedule.VehPlanSyncScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.ZonedDateTime;

import static com.shield.config.Constants.LEAVE_ALERT_TIME_AFTER_LOAD_END;
import static com.shield.config.Constants.REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN;

@Service
@Slf4j
public class PlanEventListener {

    private final AppointmentService appointmentService;

    private final VehPlanSyncScheduleService vehPlanSyncScheduleService;

    private final RegionService regionService;

    private final ShipPlanService shipPlanService;

    private final WxMpMsgService wxMpMsgService;

    private final CarWhiteListManager carWhiteListManager;

    private final RedisTemplate<String, Long> redisLongTemplate;


    @Autowired
    public PlanEventListener(
        AppointmentService appointmentService,
        VehPlanSyncScheduleService vehPlanSyncScheduleService,
        RegionService regionService,
        ShipPlanService shipPlanService,
        WxMpMsgService wxMpMsgService,
        CarWhiteListManager carWhiteListManager,
        @Qualifier("redisLongTemplate") RedisTemplate<String, Long> redisLongTemplate) {
        this.appointmentService = appointmentService;
        this.vehPlanSyncScheduleService = vehPlanSyncScheduleService;
        this.regionService = regionService;
        this.shipPlanService = shipPlanService;
        this.wxMpMsgService = wxMpMsgService;
        this.carWhiteListManager = carWhiteListManager;
        this.redisLongTemplate = redisLongTemplate;
    }

    @Async
    @TransactionalEventListener
    public void handlePlanChangedEvent(PlanChangedEvent planChangedEvent) {
        log.info("[EVENT] listen on PlanChangedEvent, applyId: {}, truckNumber: {}, before: {}, after: {}",
            planChangedEvent.getOld().getApplyId(), planChangedEvent.getOld().getTruckNumber(), planChangedEvent.getOld(), planChangedEvent.getUpdated());

        ShipPlanDTO old = planChangedEvent.getOld();
        ShipPlanDTO updated = planChangedEvent.getUpdated();

        if (old != null) {
            redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN);

            if (old.getAuditStatus().equals(1) && updated.getAuditStatus().equals(2)) {
                // 计划取消
                afterShipPlanCanceled(old, updated);
            }

            if (old.getLeaveTime() == null && updated.getLeaveTime() != null) {
                afterShipPlanLeave(old, updated);
            }
        }

        if (old == null) {
            afterShipPlanCreated(updated);
        }
    }

    private void afterShipPlanCreated(ShipPlanDTO plan) {
        log.info("TRIGGER afterShipPlanCreated...");
        if (plan.getAuditStatus().equals(1)) {
            RegionDTO region = regionService.findByName(plan.getDeliverPosition());
            if (region != null && region.isOpen() && region.isAutoAppointment()) {
                log.info("[AUTO] region[name={}, autoAppointment={}], trigger register car whitelist for ShipPlan[applyId={},truckNumber={},auditStatus={}]",
                    region.getName(), region.isAutoAppointment(), plan.getApplyId(), plan.getTruckNumber(), plan.getAuditStatus());
                carWhiteListManager.registerCarWhiteList(plan);
            }
        }
    }

    private void afterShipPlanCanceled(ShipPlanDTO before, ShipPlanDTO after) {
        log.info("TRIGGER afterShipPlanCanceled...");
        RegionDTO region = regionService.findByName(after.getDeliverPosition());
        if (region != null && region.isOpen()) {
            appointmentService.updateStatusAfterCancelShipPlan(after.getApplyId());
            if (region.isAutoAppointment()) {
                carWhiteListManager.deleteCarWhiteList(after);
            }
        }
    }

    private void afterShipPlanLeave(ShipPlanDTO before, ShipPlanDTO after) {
        log.info("TRIGGER afterShipPlanLeave...");
        RegionDTO region = regionService.findByName(after.getDeliverPosition());
        if (region != null && region.isOpen()) {
            if (region.isAutoAppointment()) {
                carWhiteListManager.deleteCarWhiteList(after);
            }

            if (!after.getLeaveAlert()
                && after.getLoadingEndTime() != null
                && after.getLeaveTime().isAfter(after.getLoadingEndTime().plusMinutes(LEAVE_ALERT_TIME_AFTER_LOAD_END))) {
                after.setLeaveAlert(true);
                shipPlanService.save(after);
                wxMpMsgService.sendLeaveAlertMsg(after);
            }
        }
    }

    private boolean timeNotEqual(ZonedDateTime t1, ZonedDateTime t2) {
        if (t1 == null && t2 == null) {
            return false;
        } else if (t1 == null || t2 == null) {
            return false;
        } else {
            return !t1.equals(t2);
        }
    }

}
