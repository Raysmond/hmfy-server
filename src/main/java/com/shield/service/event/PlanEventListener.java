package com.shield.service.event;

import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.domain.enumeration.RecordType;
import com.shield.service.*;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.shield.config.Constants.REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN;

@Service
@Slf4j
public class PlanEventListener {

    private final AppointmentService appointmentService;

    private final RegionService regionService;

    private final ShipPlanService shipPlanService;

    private final WxMpMsgService wxMpMsgService;

    private final CarWhiteListManager carWhiteListManager;

    private final RedisTemplate<String, Long> redisLongTemplate;

    private final CarWhiteListService carWhiteListService;


    @Autowired
    public PlanEventListener(
        AppointmentService appointmentService,
        RegionService regionService,
        ShipPlanService shipPlanService,
        WxMpMsgService wxMpMsgService,
        CarWhiteListManager carWhiteListManager,
        @Qualifier("redisLongTemplate") RedisTemplate<String, Long> redisLongTemplate,
        CarWhiteListService carWhiteListService) {
        this.appointmentService = appointmentService;
        this.regionService = regionService;
        this.shipPlanService = shipPlanService;
        this.wxMpMsgService = wxMpMsgService;
        this.carWhiteListManager = carWhiteListManager;
        this.redisLongTemplate = redisLongTemplate;
        this.carWhiteListService = carWhiteListService;
    }

    @Async
    @TransactionalEventListener
    public void handlePlanChangedEvent(PlanChangedEvent planChangedEvent) {
        ShipPlanDTO old = planChangedEvent.getOld();
        ShipPlanDTO updated = planChangedEvent.getUpdated();

        log.info("[EVENT] listen on PlanChangedEvent, applyId: {}, truckNumber: {}, before: {}, after: {}",
            planChangedEvent.getUpdated().getApplyId(),
            planChangedEvent.getUpdated().getTruckNumber(),
            planChangedEvent.getOld(),
            planChangedEvent.getUpdated());


        if (old != null) {
            redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, old.getId());

            if (old.getAuditStatus().equals(1) && updated.getAuditStatus().equals(2)) {
                // 计划取消
                afterShipPlanCanceled(old, updated);
            }

            if (old.getLeaveTime() == null && updated.getLeaveTime() != null) {
                afterShipPlanLeave(old, updated);
            }

            if (old.getLoadingStartTime() == null && updated.getLoadingStartTime() != null) {
                afterLoadingStart(old, updated);
            }
        }

        if (old == null) {
            afterShipPlanCreated(updated);
        }
    }

    private void afterLoadingStart(ShipPlanDTO old, ShipPlanDTO updated) {
        log.info("TRIGGER afterLoadingStart...");
        RegionDTO region = regionService.findByName(updated.getDeliverPosition());
        if (region != null && region.isOpen()) {
            if (updated.getGateTime() == null) {
                log.info("[AUTO] ShipPlan[applyId={}, truckNumber={}] loadingStartTime {} is set, but gateTime is null, auto set gateTime {} 30min before loadingStartTime",
                    updated.getApplyId(),
                    updated.getTruckNumber(),
                    updated.getLoadingStartTime().format(DateTimeFormatter.ofPattern("yyyyMMddHHMMSS")),
                    updated.getLoadingStartTime().minusMinutes(30).format(DateTimeFormatter.ofPattern("yyyyMMddHHMMSS"))
                );
                carWhiteListService.updateCarInAndOutTime(region.getId(), updated.getTruckNumber(), RecordType.IN, updated.getLoadingStartTime().minusMinutes(30), null);
            }
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
        if (region != null && region.isOpen() && region.getLeaveAlertTime() > 0) {
            if (region.isAutoAppointment()) {
                carWhiteListManager.deleteCarWhiteList(after);
            }

            if (!after.getLeaveAlert()
                && after.getLoadingEndTime() != null
                && after.getLeaveTime().isAfter(after.getLoadingEndTime().plusMinutes(region.getLeaveAlertTime()))) {
                after.setLeaveAlert(true);
                shipPlanService.save(after);
                wxMpMsgService.sendLeaveAlertMsg(after, region.getLeaveAlertTime());
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
