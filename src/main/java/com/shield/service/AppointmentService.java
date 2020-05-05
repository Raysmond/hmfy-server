package com.shield.service;

import com.shield.service.dto.AppointmentDTO;

import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.web.rest.vm.AppointmentStat;
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

    void countRemainQuota(RegionDTO region, boolean isVip);

    AppointmentDTO makeAppointment(Long regionId, AppointmentDTO appointmentDTO);

    List<AppointmentDTO> findByApplyIdIn(List<Long> applyIds);

    Map<Long, AppointmentDTO> findLastByApplyIdIn(List<Long> applyIds);

    AppointmentDTO findLastByApplyId(Long applyId);

    AppointmentDTO cancelAppointment(Long appointmentId);

    void updateStatusAfterCancelShipPlan(Long applyId);

    Integer calcNextQuotaWaitingTime(RegionDTO region);

    Integer getNextAppointmentNumber(Long regionId);

    void expireWaitAppointment(AppointmentDTO appointment);

    boolean autoMakeAppointmentForWaitUser(AppointmentDTO appointment);

    void expireAppointment(AppointmentDTO appointment);

    void autoSetAppointmentLeave(AppointmentDTO appointment);

    AppointmentStat countAppointmentStat(ZonedDateTime begin, ZonedDateTime end);

    AppointmentStat countAppointmentStat(String currentRegion, ZonedDateTime begin, ZonedDateTime end);

    List<AppointmentDTO> findLatestByTruckNumber(String licensePlateNumber, ZonedDateTime createTime);

    AppointmentDTO makeAppointmentForTomorrow(RegionDTO region, ShipPlanDTO plan, AppointmentDTO appointmentDTO);

    void setApplyNumber(List<AppointmentDTO> appointmentDTOS);
}
