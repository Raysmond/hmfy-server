package com.shield.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.ShipPlan;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.domain.enumeration.ParkingConnectMethod;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.AppointmentService;
import com.shield.repository.AppointmentRepository;
import com.shield.service.UserService;
import com.shield.service.WxMpMsgService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.mapper.AppointmentMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.shield.service.ParkingTcpHandlerService.*;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ShipPlanRepository shipPlanRepository;

    @Autowired
    private WxMpMsgService wxMpMsgService;

    @Autowired
    private CarWhiteListService carWhiteListService;

    @Autowired
    @Qualifier("redisLongTemplate")
    private RedisTemplate<String, Long> redisLongTemplate;
    private static final String APPOINTMENT_NUMBER_KEY = "appointment_%s_%s";
    private static final String QUEUE_NUMBER_KEY = "queue_number_%s_%s";
    private static final Long INITIAL_APPOINTMENT_NUMBER = 10000L;
    private static final Long INITIAL_QUEUE_NUMBER = 100L;
    public static final String REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN = "sync_ship_plan_ids";

    private static final Set<AppointmentStatus> ACTIVE_STATUS = Sets.newHashSet(
        AppointmentStatus.START,
        AppointmentStatus.ENTER);

    /**
     * Save a appointment.
     *
     * @param appointmentDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AppointmentDTO save(AppointmentDTO appointmentDTO) {
        log.debug("Request to save Appointment : {}", appointmentDTO);
        Appointment appointment = appointmentMapper.toEntity(appointmentDTO);
        if (appointment.getId() == null) {
            appointment.setCreateTime(ZonedDateTime.now());
        }
        appointment.setUpdateTime(ZonedDateTime.now());
        appointment = appointmentRepository.save(appointment);
        return appointmentMapper.toDto(appointment);
    }

    /**
     * Get all the appointments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Appointments");
        return appointmentRepository.findAll(pageable)
            .map(appointmentMapper::toDto);
    }


    /**
     * Get one appointment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AppointmentDTO> findOne(Long id) {
        log.debug("Request to get Appointment : {}", id);
        return appointmentRepository.findById(id)
            .map(appointmentMapper::toDto);
    }

    /**
     * Delete the appointment by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Appointment : {}", id);
        appointmentRepository.deleteById(id);
    }


    @Override
    public List<AppointmentDTO> findByApplyIdIn(List<Long> applyIds) {
        return appointmentRepository.findByApplyIdIn(applyIds)
            .stream()
            .map(it -> appointmentMapper.toDto(it)).collect(Collectors.toList());
    }

    @Override
    public Map<Long, AppointmentDTO> findLastByApplyIdIn(List<Long> applyIds) {
        List<AppointmentDTO> items = this.findByApplyIdIn(applyIds);
        items.sort(Comparator.comparing(AppointmentDTO::getCreateTime).reversed());
        Map<Long, AppointmentDTO> applyId2Appointment = Maps.newHashMap();
        for (AppointmentDTO item : items) {
            if (!applyId2Appointment.containsKey(item.getApplyId())) {
                applyId2Appointment.put(item.getApplyId(), item);
            }
        }
        return applyId2Appointment;
    }

    @Override
    public AppointmentDTO findLastByApplyId(Long applyId) {
        Map<Long, AppointmentDTO> result = findLastByApplyIdIn(Lists.newArrayList(applyId));
        if (result.containsKey(applyId)) {
            return result.get(applyId);
        }
        return null;
    }

    @Override
    public AppointmentDTO cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.getOne(appointmentId);
        AppointmentStatus currentStatus = appointment.getStatus();
        appointment.setValid(Boolean.FALSE);
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setUpdateTime(ZonedDateTime.now());
        appointment = appointmentRepository.save(appointment);

        if (currentStatus.equals(AppointmentStatus.START)) {
            redisLongTemplate.opsForSet().remove(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointment.getId());
            redisLongTemplate.opsForSet().add(REDIS_KEY_DELETE_CAR_WHITELIST, appointment.getId());

            if (appointment.getApplyId() != null) {
                List<ShipPlan> plans = shipPlanRepository.findByApplyIdIn(Lists.newArrayList(appointment.getApplyId()));
                for (ShipPlan plan : plans) {
                    plan.setAllowInTime(null);
                    plan.setUpdateTime(ZonedDateTime.now());
                    shipPlanRepository.save(plan);
                    redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, plan.getId());
                }
            }

            wxMpMsgService.sendAppointmentCancelMsg(appointmentMapper.toDto(appointment));
        }

        return appointmentMapper.toDto(appointment);
    }

    @Override
    public void updateCarInAndOutTime(String parkId, String truckNumber, String service, String carInTime, String carOutTime) {
        Region region = regionRepository.findOneByParkId(parkId);
        if (region == null) {
            log.error("Cannot find region by parkid: {}", parkId);
            return;
        }
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        if (service.equals("uploadcarin")) {
            // 车辆入场
            ZonedDateTime inTime = ZonedDateTime.parse(carInTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()));
            List<ShipPlan> shipPlans = shipPlanRepository.findAllByTruckNumberAndDeliverTime(truckNumber, region.getName(), today)
                .stream().filter(it -> it.getAuditStatus().equals(Integer.valueOf(1)))
                .sorted(Comparator.comparing(ShipPlan::getApplyId)).collect(Collectors.toList());
            if (shipPlans.size() > 1) {
                // 理论上车辆入场，当计划audit_status=1的只有一条
                // 不排除同时建了多个计划的情况，有多个有效计划时，只取最早创建的一条
                log.error("Multiple plans found for truckNumber {}, region: {}, deliverDate: {}, planIds: [{}]",
                    truckNumber, region.getName(), today, Joiner.on(",").join(shipPlans.stream().map(ShipPlan::getId).collect(Collectors.toList())));
            }
            if (!CollectionUtils.isEmpty(shipPlans)) {
                ShipPlan plan = shipPlans.get(0);
                log.info("Find ShipPlan id={} for truckNumber {}, uploadcarin gateTime: {}", plan.getId(), truckNumber, carInTime);
                if (plan.getGateTime() == null) {
                    plan.setGateTime(inTime);
                    plan.setUpdateTime(ZonedDateTime.now());
                    shipPlanRepository.save(plan);
                    log.info("update ShipPlan {} gateTime: {}, truckNumber: {}", plan.getApplyId(), carInTime, truckNumber);
                    redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, plan.getId());
                }
            } else {
                log.error("Cannot find ShipPlan for truckNumber {}, uploadcarin gateTime: {}", truckNumber, carInTime);
            }
        } else if (service.equals("uploadcarout")) {
            // 车辆出厂
            ZonedDateTime outTime = ZonedDateTime.parse(carOutTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()));
            if (outTime.getYear() < ZonedDateTime.now().getYear()) {
                log.info("uploadcarout leave time error, %s", carOutTime);
                outTime = ZonedDateTime.now();
            }
            List<ShipPlan> shipPlans = shipPlanRepository.findAllByTruckNumberAndDeliverTime(truckNumber, region.getName(), today)
                .stream().filter(it -> it.getAuditStatus().equals(Integer.valueOf(3)) || (it.getLeaveTime() == null && it.getGateTime() != null))
                .sorted(Comparator.comparing(ShipPlan::getApplyId).reversed())
                .collect(Collectors.toList());
            // 取未出场的，最近一个提货完成的计划
            if (!shipPlans.isEmpty()) {
                ShipPlan plan = shipPlans.get(0);
                log.info("Find ShipPlan id={} for truckNumber {}, uploadcarout gateTime: {}, leaveTime: {}", plan.getId(), truckNumber, carInTime, carOutTime);
                plan.setLeaveTime(outTime);
                if (plan.getGateTime() == null && StringUtils.isNotBlank(carInTime)) {
                    ZonedDateTime inTime = ZonedDateTime.parse(carInTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()));
                    plan.setGateTime(inTime);
                }
                plan.setUpdateTime(ZonedDateTime.now());
                shipPlanRepository.save(plan);
                log.info("update ShipPlan {} leaveTime: {}, truckNumber: {}", plan.getApplyId(), carOutTime, truckNumber);
                redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, plan.getId());
                redisLongTemplate.opsForSet().add(AUTO_DELETE_PLAN_ID_QUEUE, plan.getId());
            } else {
                log.error("Cannot find ShipPlan for truckNumber {}, uploadcarout gateTime: {}, leaveTime: {}", truckNumber, carInTime, carOutTime);
            }
        }

        List<Appointment> appointments = appointmentRepository.findLatestByTruckNumber(region.getId(), truckNumber, today);
        if (!CollectionUtils.isEmpty(appointments)) {
            Appointment appointment = appointments.get(0);
            boolean save = false;
            if (service.equals("uploadcarin")) {
                if (appointment.getStatus().equals(AppointmentStatus.START)) {
                    appointment.setStatus(AppointmentStatus.ENTER);
                    ZonedDateTime inTime = ZonedDateTime.parse(carInTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()));
                    appointment.setEnterTime(inTime);
                    save = true;
                }
            } else if (service.equals("uploadcarout")) {
                if (appointment.getStatus().equals(AppointmentStatus.ENTER)) {
                    ZonedDateTime outTime = ZonedDateTime.parse(carOutTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()));
                    appointment.setLeaveTime(outTime);
                    appointment.setStatus(AppointmentStatus.LEAVE);
                    save = true;

                    redisLongTemplate.opsForSet().add(REDIS_KEY_DELETE_CAR_WHITELIST, appointment.getId());
                }
            }

            if (save) {
                appointment.setUpdateTime(ZonedDateTime.now());
                appointmentRepository.save(appointment);
                log.info("Update appointment [{}] status: {}, inTime: {}, outTime: {}, truckNumber: {}",
                    appointment.getId(), appointment.getStatus(), carInTime, carOutTime, truckNumber);
            }
        }
    }

    @Override
    public Long countAppointmentOfRegionId(Long regionId) {
        return appointmentRepository.countAllValidByRegionId(regionId);
    }

    @Override
    public Long countAppointmentOfRegionIdAndCreateTime(Long regionId, ZonedDateTime begin) {
        return appointmentRepository.countAllValidByRegionIdAndCreateTime(regionId, begin);
    }

    @Override
    public Long countAllWaitByRegionId(Long regionId) {
        return appointmentRepository.countAllWaitByRegionId(regionId);
    }

    public Integer generateAppointmentNumber(Long regionId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
//        String key = String.format(APPOINTMENT_NUMBER_KEY, regionId, today);
        String key = "unique_appointment_number";
        if (redisLongTemplate.hasKey(key) == Boolean.FALSE) {
            redisLongTemplate.opsForValue().increment(key, INITIAL_APPOINTMENT_NUMBER);
//            redisLongTemplate.expire(key, 7L, TimeUnit.DAYS);
        }
        return redisLongTemplate.opsForValue().increment(key, 1L).intValue();
    }

    public Integer generateQueueNumber(Long regionId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
//        String key = String.format(QUEUE_NUMBER_KEY, regionId, today);
        String key = "unique_queue_number";
        if (redisLongTemplate.hasKey(key) == Boolean.FALSE) {
            redisLongTemplate.opsForValue().increment(key, INITIAL_QUEUE_NUMBER);
//            redisLongTemplate.expire(key, 7L, TimeUnit.DAYS);
        }
        return redisLongTemplate.opsForValue().increment(key, 1L).intValue();
    }

    @Override
    public AppointmentDTO makeAppointment(Long regionId, AppointmentDTO appointmentDTO) {
        Region region = regionRepository.getOne(regionId);
        Appointment appointment = new Appointment();
        appointment.setLicensePlateNumber(appointmentDTO.getLicensePlateNumber());
        appointment.setDriver(appointmentDTO.getDriver());
        appointment.setCreateTime(ZonedDateTime.now());
        appointment.setUpdateTime(ZonedDateTime.now());
        appointment.setRegion(region);
        appointment.setUser(userService.getUserWithAuthorities().get());
        appointment.setVip(appointmentDTO.isVip());
        appointment.setValid(true);
        appointment.setApplyId(appointmentDTO.getApplyId());
        appointment.setStatus(AppointmentStatus.CREATE);
        appointmentRepository.save(appointment);

        if (!tryMakeAppointment(appointment)) {
            appointment.setStatus(AppointmentStatus.WAIT);
            appointment.setQueueNumber(generateQueueNumber(regionId));
        }
        return appointmentMapper.toDto(appointment);
    }

    private boolean tryMakeAppointment(Appointment appointment) {
        synchronized (this) {
            Region region = appointment.getRegion();
            Long current = appointmentRepository.countAllValidByRegionIdAndCreateTime(region.getId(), ZonedDateTime.now().minusHours(12));
            log.debug("Region {}: [{}] status: {}/{}", region.getId(), region.getName(), current, region.getQuota());
            if (current < region.getQuota() || (appointment.isVip() && current < (region.getQuota() + region.getVipQuota()))) {
                appointment.setStatus(AppointmentStatus.START);
                appointment.setValid(Boolean.TRUE);
                appointment.setStartTime(ZonedDateTime.now());
                appointment.setUpdateTime(ZonedDateTime.now());
                appointment.setNumber(generateAppointmentNumber(region.getId()));
                appointmentRepository.save(appointment);
                log.info("Appointment [{}] made success at region ({}, {}), number: {}, truckNumber: {}",
                    appointment.getId(), region.getId(), region.getName(), appointment.getNumber(), appointment.getLicensePlateNumber());

                if (appointment.getApplyId() != null) {
                    List<ShipPlan> plans = shipPlanRepository.findByApplyIdIn(Lists.newArrayList(appointment.getApplyId()));
                    for (ShipPlan plan : plans) {
                        plan.setAllowInTime(ZonedDateTime.now().plusHours(region.getValidTime()));
                        shipPlanRepository.save(plan);
                        redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, plan.getId());
                    }
                }

                if (region.isOpen() && region.getParkingConnectMethod() != null && region.getParkingConnectMethod().equals(ParkingConnectMethod.DATABASE)) {
                    carWhiteListService.registerCarWhiteListByAppointmentId(appointment.getId());
                } else {
                    redisLongTemplate.opsForSet().add(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointment.getId());
                }
                wxMpMsgService.sendAppointmentSuccessMsg(appointmentMapper.toDto(appointment));
                return true;
            }
            return false;
        }
    }

    private ZonedDateTime getTodayStartTime() {
        LocalDate today = LocalDate.now();
        LocalTime time = LocalTime.MIN;
        return ZonedDateTime.of(today, time, ZoneId.systemDefault()).minusDays(2L);
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void checkAppointments() {
        for (Region region : regionRepository.findAll()) {
            Long validHours = region.getValidTime().longValue();
            List<Appointment> appointments = appointmentRepository.findAllByRegionId(region.getId(), AppointmentStatus.START, Boolean.TRUE, getTodayStartTime());
            for (Appointment appointment : appointments) {
                if (appointment.getStartTime() == null || appointment.getStartTime().plusHours(validHours).isBefore(ZonedDateTime.now())) {
                    log.info("Appointment [{}] expired after {} hours", appointment.getId(), validHours);
                    appointment.setUpdateTime(ZonedDateTime.now());
                    appointment.setValid(false);
                    appointment.setStatus(AppointmentStatus.EXPIRED);
                    appointmentRepository.save(appointment);

                    redisLongTemplate.opsForSet().remove(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointment.getId());
                    redisLongTemplate.opsForSet().add(REDIS_KEY_DELETE_CAR_WHITELIST, appointment.getId());

                    if (appointment.getApplyId() != null) {
                        List<ShipPlan> plans = shipPlanRepository.findByApplyIdIn(Lists.newArrayList(appointment.getApplyId()));
                        for (ShipPlan plan : plans) {
                            plan.setAllowInTime(ZonedDateTime.now().plusHours(region.getValidTime()));
                            shipPlanRepository.save(plan);
                            redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, plan.getId());
                        }
                    }

                    wxMpMsgService.sendAppointmentExpireMsg(appointmentMapper.toDto(appointment));
                }
            }

            List<Appointment> waitingList = appointmentRepository.findWaitingList(region.getId());
            waitingList.sort((Comparator.comparing(Appointment::getCreateTime)));
            for (Appointment appointment : waitingList) {
                if (region.getQueueValidTime() != null && appointment.getCreateTime().plusHours(region.getQueueValidTime()).isBefore(ZonedDateTime.now())) {
                    log.info("Appointment [{}] queue expired after {} hours", appointment.getId(), region.getQueueValidTime());
                    appointment.setUpdateTime(ZonedDateTime.now());
                    appointment.setValid(false);
                    appointmentRepository.save(appointment);
                } else if (!this.tryMakeAppointment(appointment)) {
                    break;
                }
            }
        }
    }
}
