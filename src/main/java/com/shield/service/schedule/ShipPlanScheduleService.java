package com.shield.service.schedule;

import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.domain.Region;
import com.shield.domain.ShipPlan;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.WxMpMsgService;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.mapper.ShipPlanMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.shield.config.Constants.LEAVE_ALERT_TIME_AFTER_LOAD_END;
import static com.shield.config.Constants.LOADING_START_ALERT_EXPIRED_TIMES_HOURS_AFTER_ENTER;

@Service
@Slf4j
public class ShipPlanScheduleService {

    private final RegionRepository regionRepository;

    private final ShipPlanRepository shipPlanRepository;


    private final WxMpMsgService wxMpMsgService;

    private final ShipPlanMapper shipPlanMapper;

    private final CarWhiteListService carWhiteListService;

    @Autowired
    public ShipPlanScheduleService(
        RegionRepository regionRepository,
        ShipPlanRepository shipPlanRepository,
        WxMpMsgService wxMpMsgService,
        ShipPlanMapper shipPlanMapper,
        CarWhiteListService carWhiteListService) {
        this.regionRepository = regionRepository;
        this.shipPlanRepository = shipPlanRepository;
        this.wxMpMsgService = wxMpMsgService;
        this.shipPlanMapper = shipPlanMapper;
        this.carWhiteListService = carWhiteListService;
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void checkOnShipPlans() {
        List<Region> regions = regionRepository.findAll().stream().filter(Region::isOpen).collect(Collectors.toList());
        for (Region region : regions) {
            List<ShipPlan> plans = shipPlanRepository
                .findAllByGateTime(ZonedDateTime.now().minusHours(LOADING_START_ALERT_EXPIRED_TIMES_HOURS_AFTER_ENTER + 1), region.getName()).stream()
                .filter(it ->
                    it.getGateTime() != null
                        && it.getLoadingStartTime() == null
                        && !it.isTareAlert() && it.getGateTime().plusHours(LOADING_START_ALERT_EXPIRED_TIMES_HOURS_AFTER_ENTER).isBefore(ZonedDateTime.now()))
                .collect(Collectors.toList());
            for (ShipPlan plan : plans) {
                log.info("[AUTO] set ShipPlan [applyId={},truckNumber={}, gateTime: {}], tareAlert=true after {} hours",
                    plan.getApplyId(), plan.getTruckNumber(), plan.getGateTime(), LOADING_START_ALERT_EXPIRED_TIMES_HOURS_AFTER_ENTER);
                plan.setTareAlert(true);
                shipPlanRepository.save(plan);
                wxMpMsgService.sendLoadStartAlertMsgToWxUser(plan);
            }

            List<ShipPlanDTO> shipPlanDTOs = shipPlanRepository
                .findAllByLoadingEndTime(ZonedDateTime.now().minusMinutes(LEAVE_ALERT_TIME_AFTER_LOAD_END * 2), region.getName()).stream()
                .filter(it ->
                    it.getLeaveTime() == null
                        && !it.isLeaveAlert()
                        && it.getLoadingEndTime().plusMinutes(LEAVE_ALERT_TIME_AFTER_LOAD_END).isAfter(ZonedDateTime.now()))
                .map(shipPlanMapper::toDto)
                .collect(Collectors.toList());
            for (ShipPlanDTO plan : shipPlanDTOs) {
                log.info("[AUTO] set ShipPlan [applyId={},truckNumber={}, loadingEndTime: {}], set leaveTime to now {} after {} minutes",
                    plan.getApplyId(), plan.getTruckNumber(), plan.getLoadingEndTime(), ZonedDateTime.now(), LEAVE_ALERT_TIME_AFTER_LOAD_END);
                carWhiteListService.updateCarInAndOutTime(region.getId(), plan.getTruckNumber(), "uploadcarout", null, ZonedDateTime.now());
            }
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
