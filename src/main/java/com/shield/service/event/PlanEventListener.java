package com.shield.service.event;

import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.domain.enumeration.PlanStatus;
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

import java.time.format.DateTimeFormatter;

import static com.shield.config.Constants.REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN;
import static com.shield.config.Constants.REGION_ID_HUACHAN;

/**
 * 监听计划状态变更
 */
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

    private final HuachanCarWhitelistService huachanCarWhitelistService;


    @Autowired
    public PlanEventListener(
        AppointmentService appointmentService,
        RegionService regionService,
        ShipPlanService shipPlanService,
        WxMpMsgService wxMpMsgService,
        CarWhiteListManager carWhiteListManager,
        @Qualifier("redisLongTemplate") RedisTemplate<String, Long> redisLongTemplate,
        CarWhiteListService carWhiteListService,
        HuachanCarWhitelistService huachanCarWhitelistService) {
        this.appointmentService = appointmentService;
        this.regionService = regionService;
        this.shipPlanService = shipPlanService;
        this.wxMpMsgService = wxMpMsgService;
        this.carWhiteListManager = carWhiteListManager;
        this.redisLongTemplate = redisLongTemplate;
        this.carWhiteListService = carWhiteListService;
        this.huachanCarWhitelistService = huachanCarWhitelistService;
    }

    @Async
    @TransactionalEventListener
    public void handlePlanChangedEvent(PlanChangedEvent planChangedEvent) {
        ShipPlanDTO before = planChangedEvent.getOld();
        ShipPlanDTO after = planChangedEvent.getUpdated();
        log.info("[EVENT] listen on PlanChangedEvent, applyId: {}, truckNumber: {}, before: {}, after: {}", after.getApplyId(), after.getTruckNumber(), before, after);

        try {
            if (before != null) {
                // 取消计划
                if (before.getAuditStatus().equals(PlanStatus.WAIT_SHIP.getStatus())
                    && after.getAuditStatus().equals(PlanStatus.CANCELED.getStatus())) {
                    afterShipPlanCanceled(before, after);
                }
                // 提货
                if (!before.getAuditStatus().equals(PlanStatus.SHIPPED.getStatus())
                    && after.getAuditStatus().equals(PlanStatus.SHIPPED.getStatus())) {
                    afterShipPlanShipped(before, after);
                }

                // 离场
                if (before.getLeaveTime() == null && after.getLeaveTime() != null) {
                    afterShipPlanLeave(before, after);
                }

                // 上磅
                if (before.getLoadingStartTime() == null && after.getLoadingStartTime() != null) {
                    afterLoadingStart(before, after);
                }

                // 下磅
                if (before.getLoadingEndTime() == null && after.getLoadingEndTime() != null) {
                    afterLoadingEnd(before, after);
                }

                // 过期
                if (!before.getAuditStatus().equals(1) && after.getAuditStatus().equals(4)) {
                    afterShipPlanExpired(after);
                }

                // 同步计划到 SQL_SERVER
                redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, after.getId());
            }

            if (before == null) {
                // 新建计划
                afterShipPlanCreated(after);
            }
        } catch (Exception e) {
            log.error("Failed to execute handlePlanChangedEvent()..., applyId: {}, truckNumber: {}", after.getApplyId(), after.getTruckNumber());
        }
    }

    /**
     * 计划过期后
     */
    private void afterShipPlanExpired(ShipPlanDTO after) {
        log.info("TRIGGER EVENT afterShipPlanExpired...");
        RegionDTO region = regionService.findByName(after.getDeliverPosition());
        if (region != null && region.isOpen()) {
            appointmentService.updateStatusAfterCancelShipPlan(after.getApplyId());
            carWhiteListManager.deleteCarWhiteList(after);
        }
    }

    /**
     * 上磅之后
     */
    private void afterLoadingStart(ShipPlanDTO old, ShipPlanDTO updated) {
        log.info("TRIGGER EVENT afterLoadingStart...");
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

    /**
     * 下磅之后
     */
    private void afterLoadingEnd(ShipPlanDTO old, ShipPlanDTO updated) {
        log.info("TRIGGER EVENT afterLoadingEnd...");

    }

    /**
     * 新创建计划
     */
    private void afterShipPlanCreated(ShipPlanDTO plan) {
        log.info("TRIGGER EVENT afterShipPlanCreated...");
        if (plan.getAuditStatus().equals(1)) {
            RegionDTO region = regionService.findByName(plan.getDeliverPosition());
            if (region != null && region.isOpen() && region.isAutoAppointment()) {
                log.info("[AUTO] region[name={}, autoAppointment={}], TRIGGER EVENT register car whitelist for ShipPlan[applyId={},truckNumber={},auditStatus={}]",
                    region.getName(), region.isAutoAppointment(), plan.getApplyId(), plan.getTruckNumber(), plan.getAuditStatus());
                carWhiteListManager.registerCarWhiteList(plan);
            }
        }
    }

    /**
     * 计划取消后 1 -> 2
     */
    private void afterShipPlanCanceled(ShipPlanDTO before, ShipPlanDTO after) {
        log.info("TRIGGER EVENT afterShipPlanCanceled...");
        RegionDTO region = regionService.findByName(after.getDeliverPosition());
        if (region != null && region.isOpen()) {
            appointmentService.updateStatusAfterCancelShipPlan(after.getApplyId());
            if (region.isAutoAppointment()) {
                carWhiteListManager.deleteCarWhiteList(after);
            }
        }
    }

    /**
     * 发货之后， 1　-> 3
     */
    private void afterShipPlanShipped(ShipPlanDTO before, ShipPlanDTO after) {
        log.info("TRIGGER EVENT afterShipPlanShipped...");
        RegionDTO region = regionService.findByName(after.getDeliverPosition());
        if (region != null && region.isOpen() && REGION_ID_HUACHAN.equals(region.getId())) {
            try {
                huachanCarWhitelistService.registerOutApplication(after);
            } catch (Exception e) {
                log.warn("registerOutApplication failed", e);
            }
        }
    }

    /**
     * 离场后（拿到离场时间）
     */
    private void afterShipPlanLeave(ShipPlanDTO before, ShipPlanDTO after) {
        log.info("TRIGGER EVENT afterShipPlanLeave...");
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

}
