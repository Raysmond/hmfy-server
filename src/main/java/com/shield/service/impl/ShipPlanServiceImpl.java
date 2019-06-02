package com.shield.service.impl;

import com.shield.service.ShipPlanService;
import com.shield.domain.ShipPlan;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.mapper.ShipPlanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link ShipPlan}.
 */
@Service
@Transactional
public class ShipPlanServiceImpl implements ShipPlanService {

    private final Logger log = LoggerFactory.getLogger(ShipPlanServiceImpl.class);

    private final ShipPlanRepository shipPlanRepository;

    private final ShipPlanMapper shipPlanMapper;

    public ShipPlanServiceImpl(ShipPlanRepository shipPlanRepository, ShipPlanMapper shipPlanMapper) {
        this.shipPlanRepository = shipPlanRepository;
        this.shipPlanMapper = shipPlanMapper;
    }

    /**
     * Save a shipPlan.
     *
     * @param shipPlanDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public ShipPlanDTO save(ShipPlanDTO shipPlanDTO) {
        log.debug("Request to save ShipPlan : {}", shipPlanDTO);
        ShipPlan shipPlan = shipPlanMapper.toEntity(shipPlanDTO);
        shipPlan = shipPlanRepository.save(shipPlan);
        return shipPlanMapper.toDto(shipPlan);
    }

    /**
     * Get all the shipPlans.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ShipPlanDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ShipPlans");
        return shipPlanRepository.findAll(pageable)
            .map(shipPlanMapper::toDto);
    }


    /**
     * Get one shipPlan by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ShipPlanDTO> findOne(Long id) {
        log.debug("Request to get ShipPlan : {}", id);
        return shipPlanRepository.findById(id)
            .map(shipPlanMapper::toDto);
    }

    /**
     * Delete the shipPlan by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ShipPlan : {}", id);
        shipPlanRepository.deleteById(id);
    }
}
