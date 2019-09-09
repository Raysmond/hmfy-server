package com.shield.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.shield.chepaipark.domain.GateIO;
import com.shield.domain.ShipPlan;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.sqlserver.domain.VehDelivPlan;
import com.shield.sqlserver.domain.VipGateLog;
import com.shield.sqlserver.repository.VehDelivPlanRepository;
import com.shield.sqlserver.repository.VipGateLogRepository;
import io.github.jhipster.config.JHipsterConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.shield.service.ParkingTcpHandlerService.VIP_CUSTOMER_COMPANIES;
import static com.shield.service.impl.AppointmentServiceImpl.REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN;

@Service
@Slf4j
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class VehPlanSyncService {

    @Autowired
    private VehDelivPlanRepository vehDelivPlanRepository;

    @Autowired
    private VipGateLogRepository vipGateLogRepository;

    @Autowired
    private ShipPlanRepository shipPlanRepository;

    @Autowired
    @Qualifier("redisLongTemplate")
    private RedisTemplate<String, Long> redisLongTemplate;

    @Scheduled(fixedRate = 60 * 1000)
    public void syncVehPlans() {
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        List<VehDelivPlan> plans = vehDelivPlanRepository.findAllByDeliverTimeGreaterThanEqualOrderByCreateTime(today);
        log.info("Find {} veh plans from SQLServer, deliverTime >= {}", plans.size(), today.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (CollectionUtils.isEmpty(plans)) {
            return;
        }
        List<Long> applyIds = plans.stream().map(VehDelivPlan::getApplyId).collect(Collectors.toList());
        List<ShipPlan> shipPlans = shipPlanRepository.findByApplyIdIn(applyIds);
        List<ShipPlan> updateShipPlans = Lists.newArrayList();
        Set<Long> changedApplyIds = Sets.newHashSet();
        for (VehDelivPlan plan : plans) {
            if (changedApplyIds.contains(plan.getApplyId())) {
                log.info("apply_id {} is duplicated", plan.getApplyId());
                continue;
            }
            ShipPlan shipPlan = shipPlans
                .stream()
                .filter(it -> it.getApplyId().equals(plan.getApplyId()))
                .findFirst()
                .map(it -> {
                    if (!it.getAuditStatus().equals(plan.getAuditStatus())
                        || (it.getNetWeight() == null && plan.getNetWeight() != null)
                        || (it.getWeigherNo() == null && plan.getWeigherNo() != null)
                        || (it.getLoadingStartTime() == null && plan.getTareTime() != null)
                        || (it.getLoadingEndTime() == null && plan.getWeightTime() != null)) {
                        if (it.getLoadingStartTime() == null) {
                            it.setLoadingStartTime(plan.getTareTime());
                        }
                        if (it.getLoadingEndTime() == null) {
                            it.setLoadingEndTime(plan.getWeightTime());
                        }
                        if (it.getNetWeight() == null) {
                            it.setNetWeight(plan.getNetWeight());
                        }
                        if (it.getWeigherNo() == null) {
                            it.setWeigherNo(plan.getWeigherNo());
                        }
                        it.setAuditStatus(plan.getAuditStatus());
                        it.setUpdateTime(ZonedDateTime.now());
                        changedApplyIds.add(plan.getApplyId());
                    }
                    return it;
                }).orElseGet(() -> {
                    ShipPlan newShipPlan = generateShipPlanFromVehPlan(plan);
                    changedApplyIds.add(plan.getApplyId());
                    return newShipPlan;
                });
            updateShipPlans.add(shipPlan);
        }

        updateShipPlans = updateShipPlans.stream().filter(it -> changedApplyIds.contains(it.getApplyId())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(updateShipPlans)) {
            for (ShipPlan plan : updateShipPlans) {
                plan.setSyncTime(ZonedDateTime.now());
            }
            shipPlanRepository.saveAll(updateShipPlans);
        }
        log.info("Synchronized {} veh plans", updateShipPlans.size());
    }

    @Scheduled(fixedRate = 10 * 1000)
    public void syncShipPlan() {
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        Set<Long> shipPlanIds = redisLongTemplate.opsForSet().members(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN);
        if (shipPlanIds != null && shipPlanIds.size() > 0) {
            log.info("Find {} ShipPlan ids, start to sync data to SQL_SERVER", shipPlanIds.size());
            for (Long shipPlanId : shipPlanIds) {
                Optional<ShipPlan> shipPlan = shipPlanRepository.findById(shipPlanId);
                if (shipPlan.isPresent() && !StringUtils.isEmpty(shipPlan.get().getApplyId())) {
                    ShipPlan plan = shipPlan.get();
                    log.info("Start to sync data of ShipPlan [id={}] to SQL_SERVER, data: {}", shipPlanId, plan.toString());
                    List<VehDelivPlan> vehDelivPlans = vehDelivPlanRepository.findAllByApplyId(plan.getApplyId(), today, ZonedDateTime.now());
                    if (vehDelivPlans.size() > 1) {
                        log.warn("Find multiple ({}) VehDelivPlan where apply_id = {}", vehDelivPlans.size(), plan.getApplyId());
                    }
                    if (CollectionUtils.isEmpty(vehDelivPlans)) {
                        redisLongTemplate.opsForSet().remove(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, shipPlanId);
                        continue;
                    }
                    if (!StringUtils.isEmpty(plan.getApplyNumber()) && plan.getApplyNumber().startsWith("FKSN")) {
                        redisLongTemplate.opsForSet().remove(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, shipPlanId);
                        continue;
                    }
                    for (VehDelivPlan vehDelivPlan : vehDelivPlans) {
                        vehDelivPlan.setGateTime(plan.getGateTime());
                        vehDelivPlan.setLeaveTime(plan.getLeaveTime());
                        vehDelivPlan.setAllowInTime(plan.getAllowInTime());
                    }
                    vehDelivPlanRepository.saveAll(vehDelivPlans);
                    log.info("Updated {} VehDelivPlan data with ShipPlan data, where apply_id = {} ", vehDelivPlans.size(), plan.getApplyId());
                    redisLongTemplate.opsForSet().remove(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, shipPlanId);

                    plan.setSyncTime(ZonedDateTime.now());
                    shipPlanRepository.save(plan);

                    // 保存VIP出入场记录到单独的表
                    if (plan.getCompany() != null && VIP_CUSTOMER_COMPANIES.contains(plan.getCompany())) {
                        try {
                            syncVipGateLog(plan);
                        } catch (Exception e) {
                            log.info("Failed to sync ShipPlan[apply_id={}, truckNumber={}, gateTime={}, leaveTime={}] to last VipGateLog, exception: {}",
                                plan.getApplyId(), plan.getTruckNumber(), plan.getGateTime(), plan.getLeaveTime(), e);
                        }
                    }
                }
            }
        }
    }

    private void syncVipGateLog(ShipPlan plan) {
        if (plan.getGateTime() != null || plan.getLeaveTime() != null) {
            Page<VipGateLog> gateLogs = vipGateLogRepository.findByTruckNumber(plan.getTruckNumber(), PageRequest.of(0, 1, Sort.Direction.DESC, "rowId"));
            VipGateLog lastGateLog = gateLogs.getContent().isEmpty() ? null : gateLogs.getContent().get(0);
            if (lastGateLog != null && lastGateLog.getInTime() != null && lastGateLog.getInTime().equals(plan.getGateTime())) {
                lastGateLog.setOutTime(plan.getLeaveTime());
                vipGateLogRepository.save(lastGateLog);
                log.info("Sync ShipPlan[apply_id={}, truckNumber={}, gateTime={}, leaveTime={}] to last VipGateLog[rowId={}, inTime={}]",
                    plan.getApplyId(), plan.getTruckNumber(), plan.getGateTime(), plan.getLeaveTime(), lastGateLog.getRowId(), lastGateLog.getInTime());
            } else {
                VipGateLog newLog = new VipGateLog();
                newLog.setInTime(plan.getGateTime());
                newLog.setOutTime(plan.getLeaveTime());
                newLog.setTruckNumber(plan.getTruckNumber());
                vipGateLogRepository.save(newLog);
                log.info("Sync ShipPlan[apply_id={}, truckNumber={}, gateTime={}, leaveTime={}] to new VipGateLog",
                    plan.getApplyId(), plan.getTruckNumber(), plan.getGateTime(), plan.getLeaveTime());
            }
        }
    }

    private ShipPlan generateShipPlanFromVehPlan(VehDelivPlan plan) {
        ShipPlan newShipPlan = new ShipPlan();
        newShipPlan.setApplyId(plan.getApplyId());
        newShipPlan.setApplyNumber(plan.getApplyNumber());
        newShipPlan.setCompany(plan.getCustomer());
        newShipPlan.setTruckNumber(plan.getTruckNumber());
        newShipPlan.setDeliverPosition(plan.getDeliverPosition());
        newShipPlan.setDeliverTime(plan.getDeliverTime());
        newShipPlan.setValid(Boolean.TRUE);
        newShipPlan.setProductName(plan.getProductName());
        newShipPlan.setAuditStatus(plan.getAuditStatus());
        newShipPlan.setCreateTime(plan.getCreateTime());
        newShipPlan.setCreateTime(ZonedDateTime.now());
        newShipPlan.setUpdateTime(ZonedDateTime.now());
        return newShipPlan;
    }
}
