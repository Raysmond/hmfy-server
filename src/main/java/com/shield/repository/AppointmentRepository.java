package com.shield.repository;

import com.shield.domain.Appointment;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Spring Data  repository for the Appointment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    @Query("select appointment from Appointment appointment where appointment.user.login = ?#{principal.username}")
    List<Appointment> findByUserIsCurrentUser();

    @Query("select a from Appointment a where a.region.id = ?1 and a.createTime >= ?2 and a.createTime  < ?3")
    List<Appointment> findAllByRegionId(Long regionId, ZonedDateTime startTime, ZonedDateTime endTime);
}
