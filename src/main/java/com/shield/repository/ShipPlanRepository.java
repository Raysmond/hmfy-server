package com.shield.repository;

import com.shield.domain.ShipPlan;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Spring Data  repository for the ShipPlan entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ShipPlanRepository extends JpaRepository<ShipPlan, Long>, JpaSpecificationExecutor<ShipPlan> {

    @Query("select shipPlan from ShipPlan shipPlan where shipPlan.user.login = ?#{principal.username}")
    List<ShipPlan> findByUserIsCurrentUser();


    List<ShipPlan> findByApplyIdIn(List<Long> applyIds);

    @Query("select p from ShipPlan p " +
        "where p.truckNumber = ?1 and p.auditStatus = 1 and p.deliverTime >= ?2 and p.deliverTime < ?3")
    List<ShipPlan> findAvailableByTruckNumber(String truckNumber, ZonedDateTime beginTime, ZonedDateTime endTime);
}
