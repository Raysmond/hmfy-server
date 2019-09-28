package com.shield.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.shield.service.RegionService;
import com.shield.domain.Region;
import com.shield.repository.RegionRepository;
import com.shield.service.dto.CountDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.RegionStatCount;
import com.shield.service.mapper.RegionMapper;
import com.shield.web.rest.vm.RegionStatDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.shield.service.impl.AppointmentServiceImpl.REGION_ID_HUACHAN;

/**
 * Service Implementation for managing {@link Region}.
 */
@Service
@Transactional
public class RegionServiceImpl implements RegionService {

    private final Logger log = LoggerFactory.getLogger(RegionServiceImpl.class);

    private final RegionRepository regionRepository;

    private final RegionMapper regionMapper;

    public RegionServiceImpl(RegionRepository regionRepository, RegionMapper regionMapper) {
        this.regionRepository = regionRepository;
        this.regionMapper = regionMapper;
    }

    @Override
    public RegionStatDTO countRegionStat() {
        RegionStatDTO result = new RegionStatDTO();
        List<RegionStatCount> regionCount = regionRepository.getRegionStatCount();

        for (Region region : regionRepository.findAll()) {
            RegionDTO regionDTO = this.regionMapper.toDto(region);
            RegionStatDTO.RegionStatItem regionStat = new RegionStatDTO.RegionStatItem();
            result.getData().getRegions().add(regionStat);
            regionStat.setRegion(regionDTO);

//            for (RegionStatCount count : regionCount) {
//                if (count.getRegionId().equals(region.getId())) {
//                    regionStat.getStatus().put(count.getStatus(), count.getAppointmentCount().intValue());
//                    break;
//                }
//            }
        }

        return result;
    }

    @Override
    public Boolean isRegionOpen(Long regionId) {
        Region region = regionRepository.getOne(regionId);
        if (!region.isOpen()) {
            return false;
        }
        Set<Integer> validDays = Sets.newHashSet();
        for (String day : region.getDays().split(",")) {
            validDays.add(Integer.valueOf(day.trim()));
        }
        if (!validDays.contains(LocalDate.now().getDayOfWeek().getValue())) {
            return false;
        }
        if (StringUtils.isNotBlank(region.getStartTime())
            && region.getStartTime().matches("\\d\\d:\\d\\d")
            && ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")).compareTo(region.getStartTime()) < 0) {
            return false;
        }
        if (StringUtils.isNotBlank(region.getEndTime())
            && region.getEndTime().matches("\\d\\d:\\d\\d")
            && ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")).compareTo(region.getEndTime()) > 0) {
            return false;
        }
        return true;
    }

    @Override
    public Map<Long, Long> countDriversByRegionId() {
        List<CountDTO> countDTOS = regionRepository.countDriversByRegionId();
        Map<Long, Long> result = Maps.newHashMap();
        for (CountDTO count : countDTOS) {
            result.put(count.getKey(), count.getCount());
        }
        return result;
    }

    /**
     * Save a region.
     *
     * @param regionDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public RegionDTO save(RegionDTO regionDTO) {
        log.debug("Request to save Region : {}", regionDTO);
        Region region = regionMapper.toEntity(regionDTO);
        region.setUpdateTime(ZonedDateTime.now());
        region = regionRepository.save(region);
        return regionMapper.toDto(region);
    }

    /**
     * Get all the regions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RegionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Regions");
        return regionRepository.findAll(pageable)
            .map(regionMapper::toDto);
    }

    @Override
    public List<RegionDTO> findAllConnectParkingSystem() {
        return regionRepository.findAll().stream()
            .filter(it -> StringUtils.isNotBlank(it.getParkId()) && !it.getParkId().equals("NA") && !it.getId().equals(REGION_ID_HUACHAN))
            .map(regionMapper::toDto)
            .collect(Collectors.toList());
    }


    /**
     * Get one region by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RegionDTO> findOne(Long id) {
        log.debug("Request to get Region : {}", id);
        return regionRepository.findById(id)
            .map(regionMapper::toDto);
    }

    /**
     * Delete the region by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Region : {}", id);
        regionRepository.deleteById(id);
    }

    @Override
    public RegionDTO findByName(String name) {
        Region region = regionRepository.findOneByName(name);
        if (null != region) {
            return regionMapper.toDto(region);
        } else {
            return null;
        }
    }
}
