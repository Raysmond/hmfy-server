package com.shield.repository;

import com.shield.domain.Appointment;
import com.shield.domain.enumeration.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("select a from Appointment a where a.region.id = ?1 and a.createTime >= ?2 and a.createTime < ?3 ")
    List<Appointment> findAllByCreateTime(Long regionId, ZonedDateTime startTime, ZonedDateTime endTime);

    @Query("select a from Appointment a where a.region.id = ?1 and a.createTime >= ?2 and a.createTime  < ?3")
    List<Appointment> findAllByRegionId(Long regionId, ZonedDateTime startTime, ZonedDateTime endTime);

    @Query("select a from Appointment a where a.region.id = ?1 and a.updateTime >= ?2 and a.updateTime < ?3")
    List<Appointment> findAllByRegionIdAndUpdateTime(Long regionId, ZonedDateTime startTime, ZonedDateTime endTime);

    @Query("select count(a.id) from Appointment a where a.region.id = ?1 and a.valid = true and a.status in ('START', 'ENTER', 'START_CHECK')")
    Long countAllValidByRegionId(Long regionId);

    @Query("select count(a.id) from Appointment a where a.region.id = ?1 and a.createTime > ?2 and a.valid = true and a.status in ('START', 'ENTER', 'START_CHECK')")
    Long countAllValidByRegionIdAndCreateTime(Long regionId, ZonedDateTime begin);

    @Query("select count(a.id) from Appointment a where a.region.id = ?1 and a.createTime > ?2 and a.valid = true and a.vip = true and a.status in ('START', 'ENTER', 'START_CHECK')")
    Long countAllVipValidByRegionIdAndCreateTime(Long regionId, ZonedDateTime begin);

    @Query("select count(a.id) from Appointment a where a.region.id = ?1 and a.valid = true and a.status = 'WAIT'")
    Long countAllWaitByRegionId(Long regionId);

    @Query("select count(a.id) from Appointment a where a.region.id = ?1 and a.createTime > ?2 and a.valid = true and a.status = 'WAIT'")
    Long countAllWaitByRegionIdAndCreateTime(Long regionId, ZonedDateTime begin);

    @Query("select a from Appointment a where a.region.id = ?1 and a.status = 'WAIT' and a.valid = true and a.createTime > ?2 order by a.queueNumber asc")
    List<Appointment> findWaitingList(Long regionId, ZonedDateTime begin);

    @Query("select a from Appointment a where a.region.id = ?1 and a.status = ?2 and a.valid = ?3 and a.createTime > ?4")
    List<Appointment> findAllByRegionId(Long regionId, AppointmentStatus status, Boolean valid, ZonedDateTime beginTime);

    @Query("select a from Appointment a where a.region.id = ?1 and a.status = ?2 and a.valid = ?3 and a.startTime > ?4")
    List<Appointment> findAllByStatusAndStartTime(Long regionId, AppointmentStatus status, Boolean valid, ZonedDateTime startTime);

    @Query("select a from Appointment a where a.region.id = ?1 and a.valid = true and a.status in ('START', 'ENTER', 'START_CHECK') and a.startTime >= ?2")
    Page<Appointment> findLastValid(Long regionId, ZonedDateTime startTime, Pageable pageable);

    @Query("select a from Appointment a where a.region.id = ?1 and a.valid = true and a.status in ('START', 'ENTER', 'START_CHECK') and a.startTime >= ?2")
    List<Appointment> findAllValid(Long regionId, ZonedDateTime startTime);

    @Query("select a from Appointment a where a.applyId in ?1 and a.createTime > ?2 and a.valid = true")
    List<Appointment> findByApplyIdIn(List<Long> applyIds, ZonedDateTime beginTime);

    @Query("select a from Appointment a where a.applyId in ?1 and a.status <> 'CANCELED'")
    List<Appointment> findByApplyIdIn(List<Long> applyIds);

    @Query("select a from Appointment a where a.region.id = ?1 and a.licensePlateNumber = ?2 and a.createTime >= ?3 and a.valid = true order by a.createTime desc")
    List<Appointment> findLatestByTruckNumber(Long regionId, String truckNumber, ZonedDateTime createTime);

    @Query("select a from Appointment a where a.licensePlateNumber = ?1 and a.createTime >= ?2 and a.valid = true order by a.id desc")
    List<Appointment> findLatestByTruckNumber(String truckNumber, ZonedDateTime createTime);

    @Query("select a.region.name as region, a.status as status, count(a.id) as count from Appointment a where a.createTime >= ?1 and a.createTime < ?2 group by a.region.name, a.status")
    List<AppointmentStatusCount> countAppointments(ZonedDateTime begin, ZonedDateTime end);

    interface AppointmentStatusCount {
        String getRegion();
        AppointmentStatus getStatus();
        Long getCount();
    }
}
