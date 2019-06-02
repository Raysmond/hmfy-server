package com.shield.repository;

import com.shield.domain.Region;
import com.shield.service.dto.RegionStatCount;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data  repository for the Region entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    @Query("select a.region.id as regionId, a.status, count(a.id) as appointmentCount from Appointment a group by a.region.id, a.status")
    List<RegionStatCount> getRegionStatCount();
}
