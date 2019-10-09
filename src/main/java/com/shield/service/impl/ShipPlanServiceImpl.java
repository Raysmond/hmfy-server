package com.shield.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.RegionRepository;
import com.shield.security.AuthoritiesConstants;
import com.shield.service.*;
import com.shield.domain.ShipPlan;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.dto.*;
import com.shield.service.mapper.ShipPlanMapper;
import com.shield.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.PageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.shield.service.impl.AppointmentServiceImpl.REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN;

/**
 * Service Implementation for managing {@link ShipPlan}.
 */
@Service
@Transactional
public class ShipPlanServiceImpl implements ShipPlanService {

    private final Logger log = LoggerFactory.getLogger(ShipPlanServiceImpl.class);

    private final ShipPlanRepository shipPlanRepository;

    private final ShipPlanMapper shipPlanMapper;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    @Qualifier("redisLongTemplate")
    private RedisTemplate<String, Long> redisLongTemplate;

    @Autowired
    private WxMpMsgService wxMpMsgService;

    @Autowired
    private UserService userService;


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
        boolean needSync = false;
        if (shipPlanDTO.getId() != null && shipPlanDTO.getApplyId() != null && !shipPlanDTO.getApplyId().equals(0L)) {
            Optional<ShipPlan> origin = shipPlanRepository.findById(shipPlanDTO.getId());
            if (origin.isPresent()) {
                ShipPlan originPlan = origin.get();
                if (timeNotEqual(originPlan.getGateTime(), shipPlanDTO.getGateTime())
                    || timeNotEqual(originPlan.getLeaveTime(), shipPlanDTO.getLeaveTime())
                    || timeNotEqual(originPlan.getAllowInTime(), shipPlanDTO.getAllowInTime())) {
                    needSync = true;
                }
            }
        }
        ShipPlan shipPlan = shipPlanMapper.toEntity(shipPlanDTO);
        shipPlan = shipPlanRepository.save(shipPlan);

        if (needSync) {
            redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, shipPlan.getId());
        }
        return shipPlanMapper.toDto(shipPlan);
    }

    private boolean timeNotEqual(ZonedDateTime t1, ZonedDateTime t2) {
        if (t1 == null && t2 == null) {
            return false;
        } else if (t1 == null || t2 == null) {
            return false;
        } else {
            return !t1.equals(t2);
        }
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

        ZonedDateTime begin = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(1);
        ZonedDateTime end = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusDays(1);
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

            ZonedDateTime dt = item.getPlan().getDeliverTime().plusSeconds(1L);
            if (item.getAppointment() == null) {
                if (dt.isAfter(begin) && dt.isBefore(end)) {
                    item.setStatus("可预约");
                } else if (dt.isAfter(end)) {
                    item.setStatus("未开始");
                } else {
                    item.setStatus("预约日期失效");
                }
            } else {
                AppointmentStatus status = item.getAppointment().getStatus();
                RegionDTO regionDTO = regionService.findOne(item.getAppointment().getRegionId()).get();
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
        List<ShipPlan> shipPlans = shipPlanRepository.findByDeliverTime(todayBegin, todayEnd)
            .stream().sorted(Comparator.comparing(ShipPlan::getUpdateTime).reversed()).collect(Collectors.toList());
        List<ShipPlan> uniqueShipPlans = Lists.newArrayList();
        Set<Long> uniqueIds = Sets.newHashSet();
        for (ShipPlan plan : shipPlans) {
            if (!uniqueIds.contains(plan.getId())) {
                uniqueIds.add(plan.getId());
                uniqueShipPlans.add(plan);
            }
        }

        return uniqueShipPlans.stream().filter(it -> !it.getAuditStatus().equals(Integer.valueOf(1))).map(shipPlanMapper::toDto).collect(Collectors.toList());
    }


    @Scheduled(fixedRate = 60 * 1000)
    public void checkAndAlert() {
        for (Region region : regionRepository.findAll()) {
            if (region.isOpen()) {
                List<ShipPlan> plans = shipPlanRepository.findAllByGateTime(ZonedDateTime.now().minusHours(3), region.getName());
                for (ShipPlan plan : plans) {
                    if (!plan.isTareAlert() && plan.getGateTime() != null
                        && (plan.getLoadingStartTime() == null && plan.getGateTime().plusHours(3L).isBefore(ZonedDateTime.now())
                        || plan.getLoadingStartTime() != null && plan.getGateTime().plusHours(3L).isBefore(plan.getLoadingStartTime()))) {
                        plan.setTareAlert(true);
                        shipPlanRepository.save(plan);
                        sendAlertMsgToWxUser(plan);
                    }
                }
            }
        }
    }

    private void sendAlertMsgToWxUser(ShipPlan delayedPlan) {
        try {
            AppointmentDTO appointmentDTO = appointmentService.findLastByApplyId(delayedPlan.getApplyId());
            if (appointmentDTO != null && appointmentDTO.getUserId() != null && Boolean.FALSE.equals(appointmentDTO.isVip()) && appointmentDTO.getStatus().equals(AppointmentStatus.ENTER)) {
                String remark = String.format("进厂时间：%s", delayedPlan.getGateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                wxMpMsgService.sendAlertMsg(appointmentDTO.getUserId(), null,
                    String.format("您好，您的提货计划%s有异常情况。", delayedPlan.getApplyId().toString()),
                    String.format("车牌%s在%s进厂之后三小时还未上磅提货！", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition()),
                    remark);

                if (appointmentDTO.getRegionId() != null) {
                    Page<UserDTO> users = userService.getAllManagedUsersByRegionId(PageRequest.of(0, 1000), appointmentDTO.getRegionId());
                    for (UserDTO user : users.getContent()) {
                        if (user.getAuthorities().contains(AuthoritiesConstants.REGION_ADMIN)) {
                            wxMpMsgService.sendAlertMsg(user.getId(), null,
                                String.format("提货计划%s有异常情况。", delayedPlan.getApplyId().toString()),
                                String.format("车牌%s在%s进厂之后三小时还未上磅提货！", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition()),
                                remark);
                        }
                    }
                }

                for (String openid : Lists.newArrayList("oZBny01fYBk-P1zpYZH00vm3uFQI", "oZBny09ivtl8EN8IVcdQKxyfA65c")) {
                    wxMpMsgService.sendAlertMsg(null, openid,
                        String.format("提货计划%s有异常情况。", delayedPlan.getApplyId().toString()),
                        String.format("车牌%s在%s进厂之后三小时还未上磅提货！", delayedPlan.getTruckNumber(), delayedPlan.getDeliverPosition()),
                        remark);
                }
            }
        } catch (Exception e) {
            log.error("failed to send alert msg sendAlertMsgToWxUser() {}", e.getMessage());
        }
    }
}
