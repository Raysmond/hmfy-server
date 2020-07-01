package com.shield.service.schedule;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.shield.domain.Appointment;
import com.shield.domain.ShipPlan;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.ShipPlanService;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.mapper.ShipPlanMapper;
import com.shield.sqlserver.domain.VehDelivPlan;
import com.shield.sqlserver.domain.VipGateLog;
import com.shield.sqlserver.repository.VehDelivPlanRepository;
import com.shield.sqlserver.repository.VipGateLogRepository;
import com.shield.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.shield.config.Constants.REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN;
import static com.shield.config.Constants.REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS;

/**
 * 发运计划同步服务
 */
@Service
@Slf4j
public class VehPlanSyncScheduleService {

    private final VehDelivPlanRepository vehDelivPlanRepository;

    private final VipGateLogRepository vipGateLogRepository;

    private final ShipPlanRepository shipPlanRepository;

    private final RedisTemplate<String, Long> redisLongTemplate;

    private final AppointmentRepository appointmentRepository;

    private final ShipPlanMapper shipPlanMapper;

    private final ShipPlanService shipPlanService;

    @Autowired
    public VehPlanSyncScheduleService(
        VehDelivPlanRepository vehDelivPlanRepository,
        VipGateLogRepository vipGateLogRepository,
        ShipPlanRepository shipPlanRepository,
        @Qualifier("redisLongTemplate") RedisTemplate<String, Long> redisLongTemplate,
        AppointmentRepository appointmentRepository,
        ShipPlanMapper shipPlanMapper,
        ShipPlanService shipPlanService) {
        this.vehDelivPlanRepository = vehDelivPlanRepository;
        this.vipGateLogRepository = vipGateLogRepository;
        this.shipPlanRepository = shipPlanRepository;
        this.redisLongTemplate = redisLongTemplate;
        this.appointmentRepository = appointmentRepository;
        this.shipPlanMapper = shipPlanMapper;
        this.shipPlanService = shipPlanService;
    }

    /**
     * 同步发运计划
     * <p>
     * SQL SERVER --> MySQL
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void syncVehPlans() {
        List<VehDelivPlan> plans = vehDelivPlanRepository.findAllByDeliverTimeGreaterThanEqualOrderByCreateTime(DateUtils.yesterday());
        log.info("[SYNC] Find {} veh plans from SQLServer, deliverTime >= {}", plans.size(), DateUtils.yesterday().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
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
                    if ((it.getAuditStatus().equals(1) && !it.getAuditStatus().equals(plan.getAuditStatus()))
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
                        log.info("[SYNC] VehDelivPlan changed: applyId: {}, truckNumber: {}, " +
                                "getNetWeight: {} -> {}," +
                                "getWeigherNo: {} -> {}," +
                                "getTareTime: {} -> {}," +
                                "getWeightTime: {} -> {}," +
                                "", plan.getApplyId(), plan.getTruckNumber(),
                            it.getNetWeight(), plan.getNetWeight(),
                            it.getWeigherNo(), plan.getWeigherNo(),
                            it.getLoadingStartTime(), plan.getTareTime(),
                            it.getLoadingEndTime(), plan.getWeightTime());
                    }
                    return it;
                }).orElseGet(() -> {
                    ShipPlan newShipPlan = generateShipPlanFromVehPlan(plan);
                    changedApplyIds.add(plan.getApplyId());
                    log.info("[SYNC] new VehDelivPlan plan: {}", plan);
                    return newShipPlan;
                });
            updateShipPlans.add(shipPlan);
        }

        updateShipPlans = updateShipPlans.stream().filter(it -> changedApplyIds.contains(it.getApplyId())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(updateShipPlans)) {
            List<ShipPlanDTO> planDTOS = updateShipPlans.stream().map(shipPlanMapper::toDto).collect(Collectors.toList());
            for (ShipPlanDTO planDTO : planDTOS) {
                shipPlanService.save(planDTO);
            }
        }
        log.info("[SYNC] Synchronized {} veh plans to MySQL", updateShipPlans.size());
    }

    /**
     * 同步出入场时间 到 发运计划
     * <p>
     * MySQL --> SQL SERVER
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void syncShipPlan() {
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        Set<Long> shipPlanIds = redisLongTemplate.opsForSet().members(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN);
        if (shipPlanIds != null && shipPlanIds.size() > 0) {
            log.info("Find {} ShipPlan ids, start to sync data to SQL_SERVER", shipPlanIds.size());
            for (Long shipPlanId : shipPlanIds) {
                try {
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
                            if (!StringUtils.isEmpty(plan.getAppointmentNumber())) {
                                vehDelivPlan.setOrderNumber(plan.getAppointmentNumber());
                            } else {
                                vehDelivPlan.setOrderNumber("");
                            }
                        }
                        vehDelivPlanRepository.saveAll(vehDelivPlans);
                        log.info("Updated {} VehDelivPlan data with ShipPlan data, where apply_id = {} ", vehDelivPlans.size(), plan.getApplyId());
                        redisLongTemplate.opsForSet().remove(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, shipPlanId);

                        plan.setSyncTime(ZonedDateTime.now());
                        shipPlanRepository.save(plan);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to sync ShipPlan id:{} to SQLSERVER, skip...", shipPlanId, e);
                }
            }
        }
    }

    /**
     * 同步手动设置的 VIP 预约 出入场时间 到 SQLSERVER
     */
    @Scheduled(fixedRate = 300 * 1000)
    private void syncVipGateLog() {
        Set<Long> appointmentIds = redisLongTemplate.opsForSet().members(REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS);
        if (appointmentIds != null && appointmentIds.size() > 0) {
            for (Long appointmentId : appointmentIds) {
                Appointment ap = appointmentRepository.findById(appointmentId).orElse(null);
                if (ap == null || !ap.isVip()) {
                    redisLongTemplate.opsForSet().remove(REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS, appointmentId);
                    continue;
                }
                List<VipGateLog> logs = vipGateLogRepository.findByTruckNumber(ap.getLicensePlateNumber());
                VipGateLog lastGateLog = null;
                if (logs.size() > 0) {
                    logs.sort(Comparator.comparing(VipGateLog::getRowId).reversed());
                    lastGateLog = logs.get(0);
                }

                log.info("[START] Sync Appointment to VipGateLog [id={}, truckNumber={}, gateTime={}, leaveTime={}]",
                    appointmentId, ap.getLicensePlateNumber(), ap.getEnterTime(), ap.getLeaveTime());

                if (lastGateLog != null && lastGateLog.getInTime() != null && lastGateLog.getInTime().equals(ap.getLeaveTime())) {
                    lastGateLog.setOutTime(ap.getLeaveTime());
                    vipGateLogRepository.save(lastGateLog);
                    log.info("[UPDATE] Sync Appointment to VipGateLog [id={}, truckNumber={}, gateTime={}, leaveTime={}] to last VipGateLog[rowId={}, inTime={}]",
                        appointmentId, ap.getLicensePlateNumber(), ap.getEnterTime(), ap.getLeaveTime(), lastGateLog.getRowId(), lastGateLog.getInTime());
                } else {
                    VipGateLog newLog = new VipGateLog();
//                    Long maxRowId = vipGateLogRepository.findMaxRowId();
//                    if (maxRowId == null) {
//                        maxRowId = 1000L;
//                    }
//                    newLog.setRowId(maxRowId + 1);
                    newLog.setInTime(ap.getEnterTime());
                    newLog.setOutTime(ap.getLeaveTime());
                    newLog.setTruckNumber(ap.getLicensePlateNumber());
                    vipGateLogRepository.save(newLog);
                    log.info("[NEW] Sync Appointment to VipGateLog [id={}, truckNumber={}, gateTime={}, leaveTime={}] ",
                        appointmentId, ap.getLicensePlateNumber(), ap.getEnterTime(), ap.getLeaveTime());
                }
                redisLongTemplate.opsForSet().remove(REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS, appointmentId);
            }
        }

    }

    private ShipPlan generateShipPlanFromVehPlan(VehDelivPlan plan) {
        ShipPlan newShipPlan = new ShipPlan();
        newShipPlan.setVip(false);
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
        newShipPlan.setUpdateTime(ZonedDateTime.now());
        return newShipPlan;
    }
}
