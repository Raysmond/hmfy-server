package com.shield.service;

import com.shield.domain.Appointment;
import com.shield.domain.ShipPlan;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.PlanDTO;
import com.shield.service.dto.ShipPlanDTO;

import com.shield.web.rest.vm.WeightStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.shield.domain.ShipPlan}.
 */
public interface ShipPlanService {

    /**
     * Save a shipPlan.
     *
     * @param shipPlanDTO the entity to save.
     * @return the persisted entity.
     */
    ShipPlanDTO save(ShipPlanDTO shipPlanDTO);

    /**
     * Get all the shipPlans.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ShipPlanDTO> findAll(Pageable pageable);


    /**
     * Get the "id" shipPlan.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ShipPlanDTO> findOne(Long id);

    /**
     * Delete the "id" shipPlan.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    List<ShipPlanDTO> getAvailableByTruckNumber(Long regionId, String truckNumber);

    Page<PlanDTO> getAllByTruckNumber(Pageable pageable, String truckNumber, Long shipPlanId);


    List<ShipPlanDTO> findAllByDeliverTime(String regionName, ZonedDateTime beginDeliverTime, ZonedDateTime endBeginDeliverTime, Integer auditStatus);

    List<ShipPlanDTO> findAllShouldDeleteCarWhiteList(ZonedDateTime todayBegin, ZonedDateTime todayEnd);

    ShipPlanDTO findOneByApplyId(Long applyId);

    WeightStat countWeightStat(String region, ZonedDateTime begin, ZonedDateTime end);
}
