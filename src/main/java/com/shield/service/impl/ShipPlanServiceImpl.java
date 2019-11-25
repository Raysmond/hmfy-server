package com.shield.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.RegionRepository;
import com.shield.service.*;
import com.shield.domain.ShipPlan;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.dto.*;
import com.shield.service.event.PlanChangedEvent;
import com.shield.service.mapper.ShipPlanMapper;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.web.rest.vm.WeightStat;
import io.github.jhipster.web.util.PageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.shield.config.Constants.REGION_ID_HUACHAN;

/**
 * Service Implementation for managing {@link ShipPlan}.
 */
@Service
@Transactional
public class ShipPlanServiceImpl implements ShipPlanService {

    private final Logger log = LoggerFactory.getLogger(ShipPlanServiceImpl.class);

    private final ShipPlanRepository shipPlanRepository;

    private final ShipPlanMapper shipPlanMapper;

    private final AppointmentRepository appointmentRepository;

    private final AppointmentService appointmentService;

    private final RegionService regionService;

    private final RegionRepository regionRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final RedisTemplate<String, Long> redisLongTemplate;


    @Autowired
    public ShipPlanServiceImpl(
        ShipPlanRepository shipPlanRepository,
        ShipPlanMapper shipPlanMapper,
        AppointmentRepository appointmentRepository,
        AppointmentService appointmentService,
        RegionService regionService,
        RegionRepository regionRepository, ApplicationEventPublisher applicationEventPublisher,
        @Qualifier("redisLongTemplate") RedisTemplate<String, Long> redisLongTemplate
    ) {
        this.shipPlanRepository = shipPlanRepository;
        this.shipPlanMapper = shipPlanMapper;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
        this.regionService = regionService;
        this.regionRepository = regionRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.redisLongTemplate = redisLongTemplate;
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
        ShipPlanDTO old = null;
        if (shipPlanDTO.getId() != null) {
            old = shipPlanMapper.toDto(shipPlanRepository.findById(shipPlanDTO.getId()).get());
        }

        shipPlanDTO.setUpdateTime(ZonedDateTime.now());
        ShipPlan shipPlan = shipPlanMapper.toEntity(shipPlanDTO);
        shipPlan = shipPlanRepository.save(shipPlan);
        ShipPlanDTO updated = shipPlanMapper.toDto(shipPlan);

        applicationEventPublisher.publishEvent(new PlanChangedEvent(this, old, updated));
        return updated;
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

    @Override
    public List<ShipPlanDTO> getAvailableByTruckNumber(Long regionId, String truckNumber) {
        Optional<RegionDTO> region = regionService.findOne(regionId);
        if (!region.isPresent()) {
            throw new BadRequestAlertException("Region not found", "", "");
        }
        RegionDTO regionDTO = region.get();

        ZonedDateTime begin = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime end = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusDays(1);
        if (regionId.equals(REGION_ID_HUACHAN) && ZonedDateTime.now().getHour() >= 22) {
            end = end.plusHours(1);
        }
        List<ShipPlan> plans = shipPlanRepository.findAvailableByTruckNumber(truckNumber, regionDTO.getName(), begin, end);

        if (!CollectionUtils.isEmpty(plans)) {
            List<Long> applyIds = plans.stream().map(ShipPlan::getApplyId).collect(Collectors.toList());
            List<Appointment> appointments = appointmentRepository.findByApplyIdIn(applyIds, begin);
            Set<Long> takenApplyIds = appointments.stream().filter(it -> it.getStatus().equals(AppointmentStatus.LEAVE))
                .map(Appointment::getApplyId).collect(Collectors.toSet());
            plans = plans.stream().filter(it -> !takenApplyIds.contains(it.getApplyId())).collect(Collectors.toList());
        }

        return plans.stream().map(shipPlanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<PlanDTO> getAllByTruckNumber(Pageable pageable, String truckNumber, Long shipPlanId) {
        ZonedDateTime begin = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime end = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusDays(1);

        Page<PlanDTO> result = Page.empty(pageable);

        if (shipPlanId == null) {
            Page<ShipPlanDTO> shipPlanDTOPage = shipPlanRepository.findAllByTruckNumber(truckNumber, Boolean.TRUE, pageable).map(shipPlanMapper::toDto);
            result = shipPlanDTOPage.map(PlanDTO::new);
        } else {
            ShipPlanDTO shipPlanDTO = shipPlanRepository.findById(shipPlanId).map(shipPlanMapper::toDto).orElse(null);
            if (shipPlanDTO != null) {
                result = PageUtil.createPageFromList(Lists.newArrayList(new PlanDTO(shipPlanDTO)), pageable);
            }
        }

        if (result.getContent().isEmpty()) {
            return result;
        }

        List<Long> applyIds = result.getContent().stream().map(it -> it.getPlan().getApplyId()).collect(Collectors.toList());
        Map<Long, AppointmentDTO> appointmentDTOS = appointmentService.findLastByApplyIdIn(applyIds);

        for (PlanDTO item : result) {
            if (appointmentDTOS.containsKey(item.getPlan().getApplyId())) {
                item.setAppointment(appointmentDTOS.get(item.getPlan().getApplyId()));
            }
            RegionDTO regionDTO = regionService.findByName(item.getPlan().getDeliverPosition());
            LocalDate deliverDay = item.getPlan().getDeliverTime().toLocalDate();
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            if (item.getAppointment() == null) {
                if (deliverDay.equals(today)) {
                    item.setStatus("可预约");
                } else if (deliverDay.equals(tomorrow)){
                    item.setStatus("未开始");
                } else {
                    item.setStatus("预约日期失效");
                }
                if (regionDTO != null && regionDTO.getId().equals(REGION_ID_HUACHAN)) {
                    if (ZonedDateTime.now().getHour() >= 22 && deliverDay.equals(tomorrow)) {
                        item.setStatus("可预约");
                    }
                }
            } else {
                AppointmentStatus status = item.getAppointment().getStatus();
                if (item.getAppointment().getStartTime() != null) {
//                    item.setMaxAllowInTime(item.getAppointment().getStartTime().plusSeconds(regionDTO.getValidTime()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    item.setMaxAllowInTime(item.getAppointment().getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        + " - " + item.getAppointment().getStartTime().plusHours(regionDTO.getValidTime()).format(DateTimeFormatter.ofPattern("HH:mm")));
                }
                if (!item.getAppointment().isValid()) {
                    item.setStatus("预约过期");
                } else {
                    if (status == AppointmentStatus.WAIT) {
                        item.setStatus("排队中");
                    } else if (status == AppointmentStatus.START) {
                        item.setStatus("预约成功");
                    } else if (status == AppointmentStatus.ENTER) {
                        item.setStatus("已进厂");
                    } else if (status == AppointmentStatus.LEAVE) {
                        item.setStatus("已离厂");
                    } else if (status == AppointmentStatus.START_CHECK) {
                        item.setStatus("待审批");
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<ShipPlanDTO> findAllByDeliverTime(String regionName, ZonedDateTime beginDeliverTime, ZonedDateTime endBeginDeliverTime, Integer auditStatus) {
        return shipPlanRepository.findAllByDeliverTime(regionName, beginDeliverTime, endBeginDeliverTime, auditStatus)
            .stream().map(shipPlanMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<ShipPlanDTO> findAllShouldDeleteCarWhiteList(ZonedDateTime todayBegin, ZonedDateTime todayEnd) {
        List<ShipPlan> shipPlans = shipPlanRepository.findByDeliverTime(todayBegin, todayEnd).stream()
            .sorted(Comparator.comparing(ShipPlan::getUpdateTime).reversed()).collect(Collectors.toList());
        List<ShipPlan> uniqueShipPlans = Lists.newArrayList();
        Set<Long> uniqueIds = Sets.newHashSet();
        for (ShipPlan plan : shipPlans) {
            if (!uniqueIds.contains(plan.getId())) {
                uniqueIds.add(plan.getId());
                uniqueShipPlans.add(plan);
            }
        }

        return uniqueShipPlans.stream().filter(it -> !it.getAuditStatus().equals(1)).map(shipPlanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public ShipPlanDTO findOneByApplyId(Long applyId) {
        ShipPlan plan = shipPlanRepository.findOneByApplyId(applyId);
        if (plan != null) {
            return shipPlanMapper.toDto(plan);
        } else {
            return null;
        }
    }

    @Override
    public WeightStat countWeightStat(String regionName, ZonedDateTime begin, ZonedDateTime end) {
        WeightStat stat = new WeightStat();
        stat.setDate(begin.format(DateTimeFormatter.ISO_LOCAL_DATE));
        for (ShipPlanRepository.WeightStatItem item : shipPlanRepository.findWeightStatTotal(regionName, begin, end)) {
            stat.getCompanies().add(item.getCompany());
            stat.getData().add(
                WeightStat.CompanyWeightStat.builder()
                    .name(item.getCompany())
                    .company(item.getCompany())
                    .count(item.getCount())
                    .productName(item.getProductName())
                    .weight(item.getWeight() == null ? 0 : item.getWeight()).build());
        }
        List<String> regionNames = regionRepository.findAll().stream().map(Region::getName).collect(Collectors.toList());
        stat.setRegion(Joiner.on(",").join(regionNames));
        return stat;
    }
}
