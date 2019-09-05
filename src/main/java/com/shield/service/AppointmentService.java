package com.shield.service;

import com.shield.domain.Appointment;
import com.shield.service.dto.AppointmentDTO;

import com.shield.service.dto.RegionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AppointmentService {

    /**
     * Save a appointment.
     *
     * @param appointmentDTO the entity to save.
     * @return the persisted entity.
     */
    AppointmentDTO save(AppointmentDTO appointmentDTO);

    /**
     * Get all the appointments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AppointmentDTO> findAll(Pageable pageable);


    /**
     * Get the "id" appointment.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AppointmentDTO> findOne(Long id);

    /**
     * Delete the "id" appointment.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    Long countAppointmentOfRegionId(Long regionId);

    Long countAllWaitByRegionId(Long regionId);

    void countRemainQuota(RegionDTO region, boolean isVip);

    Long countAppointmentOfRegionIdAndCreateTime(Long regionId, ZonedDateTime begin);

    AppointmentDTO makeAppointment(Long regionId, AppointmentDTO appointmentDTO);

    List<AppointmentDTO> findByApplyIdIn(List<Long> applyIds);

    Map<Long, AppointmentDTO> findLastByApplyIdIn(List<Long> applyIds);

    AppointmentDTO findLastByApplyId(Long applyId);

    AppointmentDTO cancelAppointment(Long appointmentId);


    void updateCarInAndOutTime(String parkId, String truckNumber, String service, String carInTime, String carOutTime);

    boolean isUserInCancelPenalty(Long userId);

    boolean isUserInExpirePenalty(Long userId);

    void updateStatusAfterCancelShipPlan(Long applyId);

    Integer calcNextQuotaWaitingTime(Long regionId);

    Integer getNextAppointmentNumber(Long regionId);
}
