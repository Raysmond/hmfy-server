package com.shield.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.shield.domain.ShipPlan;
import com.shield.repository.ShipPlanRepository;
import com.shield.sqlserver.domain.VehDelivPlan;
import com.shield.sqlserver.repository.VehDelivPlanRepository;
import io.github.jhipster.config.JHipsterConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class VehPlanSyncService {

    @Autowired
    private VehDelivPlanRepository vehDelivPlanRepository;

    @Autowired
    private ShipPlanService shipPlanService;

    @Autowired
    private ShipPlanRepository shipPlanRepository;


    @Scheduled(fixedRate = 60 * 1000)
    private void syncVehPlans() {
        // 同步前两天开始的发运计划
        ZonedDateTime begin = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(2L);
        List<VehDelivPlan> plans = vehDelivPlanRepository.findAllByCreateTimeAfter(begin);
        log.info("start to sync {} veh plans, starts with {}", plans.size(), begin.format(DateTimeFormatter.BASIC_ISO_DATE));

        if (CollectionUtils.isEmpty(plans)) {
            return;
        }
        List<Long> applyIds = plans.stream().map(VehDelivPlan::getApplyId).collect(Collectors.toList());
        List<ShipPlan> shipPlans = shipPlanRepository.findByApplyIdIn(applyIds);
        log.info("all applyIds: {}", Joiner.on(",").join(applyIds));
        List<ShipPlan> allShipPlans = Lists.newArrayList();
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
                    if (!it.getAuditStatus().equals(plan.getAuditStatus())) {
                        it.setAuditStatus(plan.getAuditStatus());
                        it.setUpdateTime(ZonedDateTime.now());
                        changedApplyIds.add(plan.getApplyId());
                    }
                    return it;
                }).orElseGet(() -> {
                    ShipPlan newShipPlan = new ShipPlan();
                    newShipPlan.setApplyId(plan.getApplyId());
                    newShipPlan.setApplyNumber(plan.getApplyNumber());
                    newShipPlan.setTruckNumber(plan.getTruckNumber());
                    newShipPlan.setDeliverPosition(plan.getDeliverPosition());
                    newShipPlan.setDeliverTime(plan.getDeliverTime());
                    newShipPlan.setProductName(plan.getProductName());
                    newShipPlan.setAuditStatus(plan.getAuditStatus());
//                    newShipPlan.setCreateTime(plan.getCreateTime());
                    newShipPlan.setCreateTime(ZonedDateTime.now());
                    newShipPlan.setUpdateTime(ZonedDateTime.now());
                    changedApplyIds.add(plan.getApplyId());
                    return newShipPlan;
                });
            allShipPlans.add(shipPlan);
        }

        allShipPlans = allShipPlans.stream().filter(it -> changedApplyIds.contains(it.getApplyId())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(allShipPlans)) {
            shipPlanRepository.saveAll(allShipPlans);
            log.info("Synchronized {} veh plans", allShipPlans.size());
        }
    }
}
