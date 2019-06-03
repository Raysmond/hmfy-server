package com.shield.repository;

import com.shield.domain.Region;
import com.shield.service.dto.CountDTO;
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

    @Query(value = "select u.region_id as `key`, count(distinct u.id) as `count` from jhi_user u " +
        "join jhi_user_authority ju on ju.user_id = u.id and ju.authority_name = 'ROLE_APPOINTMENT' " +
        "group by u.region_id", nativeQuery = true)
    List<CountDTO> countDriversByRegionId();

}
