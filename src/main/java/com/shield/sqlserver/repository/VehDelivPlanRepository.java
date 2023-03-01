package com.shield.sqlserver.repository;

import com.shield.sqlserver.domain.VehDelivPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;

public interface VehDelivPlanRepository extends JpaRepository<VehDelivPlan, Long> {

    List<VehDelivPlan> findAllByCreateTimeAfterOrderByCreateTime(ZonedDateTime createTime);

    List<VehDelivPlan> findAllByDeliverTimeGreaterThanEqualOrderByCreateTime(ZonedDateTime startDeliverTime);

    @Query("select p from VehDelivPlan p where p.applyId = ?1 and p.deliverTime >= ?2 and p.deliverTime < ?3 order by p.createTime asc")
    List<VehDelivPlan> findAllByApplyId(Long applyId, ZonedDateTime today, ZonedDateTime now);

    List<VehDelivPlan> findAllByApplyIdIn(List<Long> applyIds);
}
