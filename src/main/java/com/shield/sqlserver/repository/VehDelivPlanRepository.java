package com.shield.sqlserver.repository;

import com.shield.sqlserver.domain.VehDelivPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface VehDelivPlanRepository extends JpaRepository<VehDelivPlan, Long> {

    List<VehDelivPlan> findAllByCreateTimeAfterOrderByCreateTime(ZonedDateTime createTime);
}
