package com.shield.repository;

import com.shield.domain.ShipPlan;
import com.shield.web.rest.vm.WeightStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.DoubleStream;

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
        "where p.truckNumber = ?1 and p.auditStatus = 1 and p.deliverPosition = ?2 and p.deliverTime >= ?3 and p.deliverTime < ?4")
    List<ShipPlan> findAvailableByTruckNumber(String truckNumber, String deliverPosition, ZonedDateTime beginTime, ZonedDateTime endTime);

    @Query("select p from ShipPlan p where p.truckNumber = ?1 and p.valid = ?2")
    Page<ShipPlan> findAllByTruckNumber(String truckNumber, Boolean valid, Pageable pageable);

    @Query("select p from ShipPlan p where p.deliverPosition = ?1 and p.deliverTime >= ?2 and p.deliverTime < ?3 and p.auditStatus = ?4 order by p.createTime asc")
    List<ShipPlan> findAllByDeliverTime(String regionName, ZonedDateTime beginDeliverTime, ZonedDateTime endBeginDeliverTime, Integer auditStatus);

    @Query("select p from ShipPlan p where p.truckNumber = ?1 and p.deliverPosition = ?2 and p.deliverTime >= ?3 order by p.createTime desc")
    List<ShipPlan> findAllByTruckNumberAndDeliverTime(String truckNumber, String regionName, ZonedDateTime deliverTime);

    @Query("select p from ShipPlan p where p.deliverTime >= ?1 and p.deliverTime < ?2 and p.auditStatus <> 1")
    List<ShipPlan> findALlNeedToRemoveCarWhiteList(ZonedDateTime beginDeliverTime, ZonedDateTime endBeginDeliverTime);

    @Query("select p from ShipPlan p where p.deliverTime >= ?1 and p.deliverTime < ?2")
    List<ShipPlan> findByDeliverTime(ZonedDateTime beginDeliverTime, ZonedDateTime endBeginDeliverTime);

    Long countAllByDeliverPositionAndDeliverTimeAndAuditStatus(String deliverPosition, ZonedDateTime deliverTime, Integer auditStatus);

    Long countAllByDeliverTimeAndAuditStatus(ZonedDateTime deliverTime, Integer auditStatus);

    @Query("select p from ShipPlan p where p.gateTime > ?1 and p.deliverPosition = ?2")
    List<ShipPlan> findAllByGateTime(ZonedDateTime gateTime, String regionName);

    @Query("select p from ShipPlan p where p.loadingEndTime > ?1 and p.deliverPosition = ?2")
    List<ShipPlan> findAllByLoadingEndTime(ZonedDateTime loadingEndTime, String regionName);

    ShipPlan findOneByApplyId(Long applyId);

    @Query("select p from ShipPlan p where p.deliverTime < ?1 and p.auditStatus = 1")
    List<ShipPlan> findAllNeedToExpire(ZonedDateTime endOfDeliverTime);

    @Query("select p.company as company, p.productName as productName, count(p.applyId) as count, sum(p.netWeight) as weight " +
        "from ShipPlan p where p.deliverTime >= ?1 and p.auditStatus = 3 group by p.company, p.productName")
    List<WeightStatItem> findWeightStat(ZonedDateTime begin);

    @Query("select p.company as company, count(p.applyId) as count, sum(p.netWeight) as weight " +
        "from ShipPlan p where p.deliverPosition = ?1 and p.deliverTime >= ?2 and p.deliverTime < ?3 and p.auditStatus = 3 " +
        "group by p.company, p.productName")
    List<WeightStatItem> findWeightStatTotal(String region, ZonedDateTime begin, ZonedDateTime end);

    @Query("select count(p.applyId) as count, sum(p.netWeight) as weight " +
        "from ShipPlan p where p.deliverPosition = ?1 and p.deliverTime >= ?2 and p.deliverTime < ?3 and p.auditStatus = 3 group by p.deliverPosition")
    List<WeightStatItem> findWeightStatTotalAllCompany(String region, ZonedDateTime begin, ZonedDateTime end);


    interface WeightStatItem {
        String getRegionName();
        String getCompany();
        String getProductName();
        Long getCount();
        Double getWeight();
    }
}
