package com.shield.service.schedule;

<<<<<<< HEAD
import com.shield.service.gate.CarWhiteListService;
=======
import com.google.common.collect.Lists;
import com.shield.chepaipark.service.CarWhiteListService;
>>>>>>> origin/master
import com.shield.domain.Region;
import com.shield.domain.ShipPlan;
import com.shield.domain.enumeration.ParkingConnectMethod;
import com.shield.domain.enumeration.PlanStatus;
import com.shield.domain.enumeration.RecordType;
import com.shield.domain.enumeration.WeightSource;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.ShipPlanService;
import com.shield.service.WxMpMsgService;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.mapper.ShipPlanMapper;
import com.shield.sqlserver.domain.VehDelivPlan;
import com.shield.sqlserver.repository.VehDelivPlanRepository;
import com.shield.web.rest.api.CaitongPlanApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.shield.config.Constants.LEAVE_ALERT_TIME_AFTER_LOAD_END;

@Service
@Slf4j
public class ShipPlanScheduleService {

    private final RegionRepository regionRepository;

    private final ShipPlanRepository shipPlanRepository;

    private final WxMpMsgService wxMpMsgService;

    private final ShipPlanMapper shipPlanMapper;

    private final CarWhiteListService carWhiteListService;

    private final ShipPlanService shipPlanService;

    private final VehDelivPlanRepository vehDelivPlanRepository;

    private final CaitongPlanApi caitongPlanApi;

    @Autowired
    public ShipPlanScheduleService(
        RegionRepository regionRepository,
        ShipPlanRepository shipPlanRepository,
        WxMpMsgService wxMpMsgService,
        ShipPlanMapper shipPlanMapper,
        CarWhiteListService carWhiteListService,
        ShipPlanService shipPlanService,
        VehDelivPlanRepository vehDelivPlanRepository, CaitongPlanApi caitongPlanApi) {
        this.regionRepository = regionRepository;
        this.shipPlanRepository = shipPlanRepository;
        this.wxMpMsgService = wxMpMsgService;
        this.shipPlanMapper = shipPlanMapper;
        this.carWhiteListService = carWhiteListService;
        this.shipPlanService = shipPlanService;
        this.vehDelivPlanRepository = vehDelivPlanRepository;
        this.caitongPlanApi = caitongPlanApi;
    }

    /**
     * 兜底轮询发送称重数据到老万的财通系统
     */
    @Scheduled(fixedRate = 60_000)
    public void resendWeightDataToCaitong() {
        ZonedDateTime yesterday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusHours(1);
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        Map<Long, ShipPlanDTO> plans = shipPlanService
            .findAllByDeliverTime("五期", yesterday, today, PlanStatus.SHIPPED.getStatus())
            .stream()
            .filter(plan -> WeightSource.SANYI.equals(plan.getWeightSource()) && plan.getNetWeight() != null)
            .filter(plan -> plan.getUpdateTime().isBefore(ZonedDateTime.now().minusMinutes(5)))
            .collect(Collectors.toMap(ShipPlanDTO::getApplyId, x -> x));
        if (plans.isEmpty()) {
            return;
        }
        List<Long> applyIds = Lists.newArrayList(plans.keySet());
        List<VehDelivPlan> delivPlans = vehDelivPlanRepository.findAllByApplyIdIn(applyIds)
            .stream()
            .filter(plan -> plan.getNetWeight() == null)
            .filter(plan -> Objects.equals(plan.getAuditStatus(), PlanStatus.WAIT_SHIP.getStatus()))
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(delivPlans)) {
            log.info("Find {} veh plans without weight data, need to be synced", delivPlans.size());
            for (VehDelivPlan vehDelivPlan : delivPlans) {
                Long applyId = vehDelivPlan.getApplyId();
                ShipPlanDTO plan = plans.get(applyId);
                log.info("retry to send weight data to caitong system, plan: {}", plan);
                caitongPlanApi.endOfShipment(plan);
            }
        }
    }

    @Scheduled(fixedRate = 60_000)
    public void checkOnShipPlans() {
        List<Region> regions = regionRepository.findAll().stream().filter(Region::isOpen).collect(Collectors.toList());
        for (Region region : regions) {
            if (region.getLoadAlertTime() > 0) {
                List<ShipPlan> plans = shipPlanRepository
                    .findAllByGateTime(ZonedDateTime.now().minusMinutes(region.getLoadAlertTime() + 60), region.getName()).stream()
                    .filter(it -> it.getGateTime() != null && it.getLoadingStartTime() == null
                        && !it.isTareAlert() && it.getGateTime().plusMinutes(region.getLoadAlertTime()).isBefore(ZonedDateTime.now()))
                    .collect(Collectors.toList());
                for (ShipPlan plan : plans) {
                    log.info("[AUTO] set ShipPlan [applyId={},truckNumber={}, gateTime: {}], tareAlert=true after {} hours",
                        plan.getApplyId(), plan.getTruckNumber(), plan.getGateTime(), region.getLoadAlertTime());
                    plan.setTareAlert(true);
                    shipPlanRepository.save(plan);
                    wxMpMsgService.sendLoadStartAlertMsgToWxUser(plan, region.getLoadAlertTime());
                }
            }

            Long defaultLeaveTime = region.getLeaveAlertTime() > 0 ? region.getLeaveAlertTime() : LEAVE_ALERT_TIME_AFTER_LOAD_END;
            List<ShipPlanDTO> shipPlanDTOs = shipPlanRepository
                .findAllByLoadingEndTime(ZonedDateTime.now().minusMinutes(defaultLeaveTime * 2), region.getName()).stream()
                .filter(it ->
                    it.getLeaveTime() == null
                        && !it.isLeaveAlert()
                        && ZonedDateTime.now().minusMinutes(defaultLeaveTime).isAfter(it.getLoadingEndTime()))
                .map(shipPlanMapper::toDto)
                .collect(Collectors.toList());
            for (ShipPlanDTO plan : shipPlanDTOs) {
                log.info("[AUTO] set ShipPlan [applyId={},truckNumber={}, loadingEndTime: {}], set leaveTime to now {} after {} minutes",
                    plan.getApplyId(), plan.getTruckNumber(), plan.getLoadingEndTime(), ZonedDateTime.now(), defaultLeaveTime);
//                carWhiteListService.updateCarInAndOutTime(region.getId(), plan.getTruckNumber(), RecordType.OUT, null, ZonedDateTime.now());
            }
        }
    }


    /**
     * 每天凌晨00:01:00，将前一天待提货的计划改成过期
     */
    @Scheduled(cron = "0 1 2 * * *")
    public void autoExpireShipPlan() {
        Map<String, Region> regions = regionRepository.findAll().stream().collect(Collectors.toMap(Region::getName, it -> it));
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        List<ShipPlanDTO> shipPlans = shipPlanRepository.findAllNeedToExpire(today).stream().map(shipPlanMapper::toDto).collect(Collectors.toList());
        if (!shipPlans.isEmpty()) {
            log.info("Find {} ShipPlan in status 1, should be expired", shipPlans.size());
            for (ShipPlanDTO plan : shipPlans) {
                if (regions.containsKey(plan.getDeliverPosition())) {
                    Region region = regions.get(plan.getDeliverPosition());
                    if (region.isOpen() && !region.getParkingConnectMethod().equals(ParkingConnectMethod.HUA_CHAN_API)) {
                        plan.setAuditStatus(4);
                        plan.setUpdateTime(ZonedDateTime.now());
                        shipPlanService.save(plan);
                    }
                }
            }
        }
    }
}
