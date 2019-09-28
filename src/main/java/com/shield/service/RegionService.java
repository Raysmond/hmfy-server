package com.shield.service;

import com.shield.service.dto.RegionDTO;

import com.shield.web.rest.vm.RegionStatDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.shield.domain.Region}.
 */
public interface RegionService {

    RegionStatDTO countRegionStat();

    Boolean isRegionOpen(Long regionId);

    Map<Long, Long> countDriversByRegionId();

    /**
     * Save a region.
     *
     * @param regionDTO the entity to save.
     * @return the persisted entity.
     */
    RegionDTO save(RegionDTO regionDTO);

    /**
     * Get all the regions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<RegionDTO> findAll(Pageable pageable);


    // 获取所有和门禁对接的区域
    List<RegionDTO> findAllConnectParkingSystem();

    /**
     * Get the "id" region.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<RegionDTO> findOne(Long id);

    /**
     * Delete the "id" region.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    RegionDTO findByName(String name);
}
