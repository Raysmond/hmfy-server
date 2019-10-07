package com.shield.repository;

import com.shield.domain.GateRecord;
import com.shield.domain.enumeration.RecordType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;


/**
 * Spring Data  repository for the GateRecord entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GateRecordRepository extends JpaRepository<GateRecord, Long>, JpaSpecificationExecutor<GateRecord> {
    GateRecord findOneByRid(String rid);

    List<GateRecord> findAllByRegionIdAndRecordTimeGreaterThanEqual(Long regionId, ZonedDateTime recordTime);

    @Query("select g from GateRecord g where g.regionId = ?1 and g.recordType = ?2 and g.truckNumber = ?3 and g.recordTime > ?4 order by g.recordTime asc")
    List<GateRecord> findByTruckNumber(Long regionId, RecordType recordType, String truckNumber, ZonedDateTime beginRecordTime);
}
