package com.shield.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.ShipPlan;
import com.shield.domain.User;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.domain.enumeration.ParkingConnectMethod;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.security.SecurityUtils;
import com.shield.service.AppointmentService;
import com.shield.repository.AppointmentRepository;
import com.shield.service.UserService;
import com.shield.service.WxMpMsgService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.mapper.AppointmentMapper;
import com.shield.service.mapper.RegionMapper;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.shield.service.ParkingHandlerService.*;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RegionMapper regionMapper;

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

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    private static final Long INITIAL_APPOINTMENT_NUMBER = 10000L;
    private static final Long INITIAL_QUEUE_NUMBER = 100L;
    public static final String REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN = "sync_ship_plan_ids";

    // 手动VIP的预约 把出入场记录写到单独的表中
    public static final String REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS = "sync_vip_gate_log_appointment_ids";

    // 预约取消后，10min之内不能重新预约
    private static final Long PENALTY_TIME_MINUTES_CANCEL = 10L;
    private static final String PENALTY_TIME_MINUTES_CANCEL_USER_ID_KEY = "penalty_cancel_user_id:%d";

    // 自动过期的，60min之内不能重新预约
    private static final Long PENALTY_TIME_MINUTES_EXPIRE = 60L;
    private static final String PENALTY_TIME_MINUTES_EXPIRE_USER_ID_KEY = "penalty_expire_user_id:%d";

    // 四期区域ID
    public static final Long REGION_ID_HUACHAN = 2L;

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
    public boolean isUserInCancelPenalty(Long userId) {
        String k = String.format(PENALTY_TIME_MINUTES_CANCEL_USER_ID_KEY, userId);
        return redisLongTemplate.hasKey(k);
    }

    private void putUserInCancelPenalty(Long userId) {
        String k = String.format(PENALTY_TIME_MINUTES_CANCEL_USER_ID_KEY, userId);
        redisTemplate.opsForValue().set(k, "1");
        redisTemplate.expire(k, PENALTY_TIME_MINUTES_CANCEL, TimeUnit.MINUTES);
        log.info("Put userId {} in cancel penalty", userId);
    }

    @Override
    public boolean isUserInExpirePenalty(Long userId) {
        String k = String.format(PENALTY_TIME_MINUTES_EXPIRE_USER_ID_KEY, userId);
        return redisLongTemplate.hasKey(k);
    }

    /**
     * 如果计划取消了，还未进厂，则将预约作废
     */
    @Override
    public void updateStatusAfterCancelShipPlan(Long applyId) {
        List<Appointment> appointments = appointmentRepository.findByApplyIdIn(Lists.newArrayList(applyId));
        if (!CollectionUtils.isEmpty(appointments)) {
            appointments.sort(Comparator.comparing(Appointment::getCreateTime).reversed());
            Appointment appointment = appointments.get(0);
            if (appointment.isValid() && appointment.getStatus() == AppointmentStatus.START) {
                appointment.setValid(Boolean.FALSE);
                appointmentRepository.save(appointment);
                log.info("Set appointment[id={}], truckNumber: {} valid=false after ShipPlan[applyId={}] is canceled",
                    appointment.getId(), appointment.getLicensePlateNumber(), applyId);
            }
        }
    }

    /**
     * 如果用户预约过期未进厂，则罚时 60min 之内不能抢号
     *
     * @param userId
     */
    private void putUserInExpirePenalty(Long userId) {
        String k = String.format(PENALTY_TIME_MINUTES_EXPIRE_USER_ID_KEY, userId);
        redisTemplate.opsForValue().set(k, "1");
        redisTemplate.expire(k, PENALTY_TIME_MINUTES_EXPIRE, TimeUnit.MINUTES);
        log.info("Put userId {} in expire penalty", userId);
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

            if (!appointment.isVip() && appointment.getUser() != null) {
                putUserInCancelPenalty(appointment.getUser().getId());
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

                if (appointment.isVip() && appointment.getApplyId() == null) {
                    carWhiteListService.deplyPutSyncVipAppointmentGateLog(appointment.getId());
                }
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

    public Integer getNextAppointmentNumber(Long regionId) {
        String key = "unique_appointment_number";
        if (redisLongTemplate.hasKey(key) == Boolean.TRUE) {
            return 1 + redisLongTemplate.opsForValue().increment(key, 0).intValue();
        }
        return 0;
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
        if (appointmentDTO.getUserId() != null) {
            appointment.setUser(userService.getUserWithAuthorities(appointmentDTO.getUserId()).get());
        } else {
            Optional<User> user = userService.getUserWithAuthorities();
            user.ifPresent(appointment::setUser);
        }
        appointment.setVip(appointmentDTO.isVip());
        appointment.setValid(true);
        appointment.setApplyId(appointmentDTO.getApplyId());
        appointment.setStatus(AppointmentStatus.CREATE);
        appointmentRepository.save(appointment);

        if (!tryMakeAppointment(appointment)) {
            if (region.getQueueQuota() != null
                && region.getQueueQuota() > 0
                && region.getQueueQuota() > appointmentRepository.countAllWaitByRegionIdAndCreateTime(regionId, ZonedDateTime.now().minusHours(12))) {
                appointment.setStatus(AppointmentStatus.WAIT);
                appointment.setQueueNumber(generateQueueNumber(regionId));
            } else {
                appointment.setValid(false);
                appointmentRepository.save(appointment);
            }
        }
        return appointmentMapper.toDto(appointment);
    }

    /**
     * 统计剩余取号名额
     *
     * @param region
     * @param isVip
     */
    @Override
    public void countRemainQuota(RegionDTO region, boolean isVip) {
        // 统计剩余取号名额
        List<Appointment> appointments = appointmentRepository.findAllByRegionId(region.getId(), ZonedDateTime.now().minusDays(2), ZonedDateTime.now());
        appointments = appointments.stream()
            .filter(it -> it.isValid() && (it.getStatus() == AppointmentStatus.START || it.getStatus() == AppointmentStatus.ENTER))
            .collect(Collectors.toList());

//        long current = appointmentRepository.countAllValidByRegionIdAndCreateTime(region.getId(), startTime);
//        long curVip = appointmentRepository.countAllVipValidByRegionIdAndCreateTime(region.getId(), startTime);
        long current = appointments.size();
        long curVip = appointments.stream().filter(Appointment::isVip).count();
        long total = region.getQuota();
        if (isVip) {
            total = region.getQuota() + region.getVipQuota();
        } else if (curVip > 0) {
            // vip 额度在不超额时，不侵占普通取号名额
            total = Math.min(curVip + region.getQuota(), region.getQuota() + region.getVipQuota());
        }
        if (total - current < 0) {
            log.error("remain quota is less than zero, total: {}, current: {}, vip: {}", total, current, curVip);
        }
        region.setRemainQuota(Math.max((int) (total - current), 0));
        region.setQuota((int) total);
        region.setStatusStart(appointments.stream().filter(it -> it.getStatus() == AppointmentStatus.START).count());
        region.setStatusEnter(appointments.stream().filter(it -> it.getStatus() == AppointmentStatus.ENTER).count());
    }

    /**
     * 尝试预约抢号
     *
     * @param appointment
     * @return
     */
    private boolean tryMakeAppointment(Appointment appointment) {
        synchronized (this) {
            RegionDTO region = regionMapper.toDto(appointment.getRegion());
            this.countRemainQuota(region, appointment.isVip());
            if (region.getRemainQuota() > 0) {
                if (region.getId().equals(REGION_ID_HUACHAN)) {
                    appointment.setStatus(AppointmentStatus.START_CHECK);
                } else {
                    appointment.setStatus(AppointmentStatus.START);
                }
                appointment.setValid(Boolean.TRUE);
                appointment.setStartTime(ZonedDateTime.now());
                appointment.setUpdateTime(ZonedDateTime.now());
                appointment.setNumber(generateAppointmentNumber(region.getId()));
                appointmentRepository.save(appointment);

                log.info("Appointment [{}] made success at region ({}, {}), number: {}, queue number: {}, truckNumber: {}",
                    appointment.getId(), region.getId(), region.getName(), appointment.getNumber(), appointment.getQueueNumber(), appointment.getLicensePlateNumber());

                if (appointment.getApplyId() != null) {
                    List<ShipPlan> plans = shipPlanRepository.findByApplyIdIn(Lists.newArrayList(appointment.getApplyId()));
                    for (ShipPlan plan : plans) {
                        plan.setAllowInTime(ZonedDateTime.now().plusHours(region.getValidTime()));
                        shipPlanRepository.save(plan);
                        redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, plan.getId());
                    }
                }

                if (!region.getId().equals(REGION_ID_HUACHAN)) {
                    if (region.isOpen() && region.getParkingConnectMethod() != null && region.getParkingConnectMethod().equals(ParkingConnectMethod.DATABASE)) {
                        carWhiteListService.registerCarWhiteListByAppointmentId(appointment.getId());
                    } else {
                        redisLongTemplate.opsForSet().add(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointment.getId());
                    }
                    wxMpMsgService.sendAppointmentSuccessMsg(appointmentMapper.toDto(appointment));
                }
                return true;
            }
            return false;
        }
    }


    /**
     * 取号满额时，估计下一个号释放的时间
     */
    @Override
    public Integer calcNextQuotaWaitingTime(RegionDTO region) {
        List<Appointment> appointments = appointmentRepository.findAllByRegionIdAndUpdateTime(region.getId(), ZonedDateTime.now().minusHours(2), ZonedDateTime.now());
        appointments.sort(Comparator.comparing(Appointment::getUpdateTime));
        List<Long> times = Lists.newArrayList();
        List<ZonedDateTime> outTimes = Lists.newArrayList();
        for (Appointment appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.LEAVE && appointment.getStartTime() != null && appointment.getLeaveTime() != null) {
                outTimes.add(appointment.getLeaveTime());
            }
            if (appointment.getStatus() == AppointmentStatus.EXPIRED && appointment.getExpireTime() != null) {
                outTimes.add(appointment.getExpireTime());
            }
            if (appointment.getStatus() == AppointmentStatus.CANCELED) {
                outTimes.add(appointment.getUpdateTime());
            }
        }
        Integer avgGap = 0;
        Integer lastOutGap = 0;
        if (outTimes.size() > 0) {
            if (outTimes.size() <= 1) {
                return -1;
            }
            for (int i = 1; i < outTimes.size(); i++) {
                times.add(outTimes.get(i).toEpochSecond() - outTimes.get(i - 1).toEpochSecond());
            }
            Double avg = times.stream().mapToInt(Long::intValue).average().orElse(Double.NaN);
            avgGap = avg.intValue();
            lastOutGap = (int) (ZonedDateTime.now().toEpochSecond() - outTimes.get(outTimes.size() - 1).toEpochSecond());
            log.info("Calc avg wait time: {}, avg leave gap: {}, size: {}, last leave time: {}, gap: {}",
                avgGap - lastOutGap, avgGap, outTimes.size(), outTimes.get(outTimes.size() - 1), lastOutGap);
        }

        Integer nextWaitTime = avgGap - lastOutGap;
        if (nextWaitTime < 60) {
            nextWaitTime = 60;
        }
        Integer waitTimeInMinutes = nextWaitTime / 60 + (nextWaitTime % 60 > 0 ? 1 : 0);
        region.setNextQuotaWaitTime(waitTimeInMinutes);

        // 如果当前用户在排队等待中，则需要计算等待时间
        List<Appointment> waitingList = appointmentRepository.findWaitingList(region.getId(), ZonedDateTime.now().minusDays(2));
        if (waitingList.size() > 0) {
            region.setStatusWait((long) waitingList.size());
            if (SecurityUtils.isAuthenticated()) {
                User user = userService.getUserWithAuthorities().get();
                if (StringUtils.isNotBlank(user.getTruckNumber())) {
                    for (int i = 0; i < waitingList.size(); i++) {
                        if (waitingList.get(i).getLicensePlateNumber().equals(user.getTruckNumber())) {
                            region.setStatusWaitBeforeUser((long) i);
                            region.setUserInWaitingList(Boolean.TRUE);
                            break;
                        }
                    }
                }
            }

            if (region.getUserInWaitingList()) {
                if (region.getStatusWaitBeforeUser() > 0) {
                    Integer userWaitTimeMinutes = region.getStatusWaitBeforeUser().intValue() * avgGap + nextWaitTime;
                    userWaitTimeMinutes = userWaitTimeMinutes / 60 + (userWaitTimeMinutes % 60 > 0 ? 1 : 0);
                    region.setWaitTime(userWaitTimeMinutes);
                } else {
                    region.setWaitTime(waitTimeInMinutes);
                }
            }
        }
        return avgGap - lastOutGap;
    }

    /**
     * 排队自动抢号
     */
    @Scheduled(fixedRate = 3 * 1000)
    public void autoMakeAppointmentForWaitingUsers() {
        for (Region region : regionRepository.findAll()) {
            if (region.isOpen() && region.getQueueQuota() != null && region.getQueueQuota() > 0) {
                List<Appointment> waitingList = appointmentRepository.findWaitingList(region.getId(), ZonedDateTime.now().minusDays(2));
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

    /**
     * 检查预约状态
     * <p>
     * 1. 预约过期：START --> EXPIRED
     * 2. 拿不到出场时间失效：下磅之后1h，还没有拿到出场时间，则设置出场时间为当前系统时间，并且valid=FALSE
     */
    @Scheduled(fixedRate = 30 * 1000)
    public void checkAppointments() {
        for (Region region : regionRepository.findAll()) {
            if (!region.isOpen()) {
                return;
            }
            long validHours = region.getValidTime().longValue();
            List<Appointment> appointments = appointmentRepository.findAllByRegionId(region.getId(), AppointmentStatus.START, Boolean.TRUE, ZonedDateTime.now().minusDays(2));
            for (Appointment appointment : appointments) {
                if (!appointment.isVip() && (appointment.getStartTime() == null || appointment.getStartTime().plusHours(validHours).isBefore(ZonedDateTime.now()))) {
                    // vip 的不过期
                    log.info("Appointment [{}] expired after {} hours", appointment.getId(), validHours);
                    appointment.setUpdateTime(ZonedDateTime.now());
                    appointment.setValid(false);
                    appointment.setStatus(AppointmentStatus.EXPIRED);
                    appointment.setExpireTime(ZonedDateTime.now());
                    appointmentRepository.save(appointment);

                    if (!region.getId().equals(REGION_ID_HUACHAN)) {
                        redisLongTemplate.opsForSet().remove(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointment.getId());
                        redisLongTemplate.opsForSet().add(REDIS_KEY_DELETE_CAR_WHITELIST, appointment.getId());
                    }

                    if (appointment.getApplyId() != null) {
                        List<ShipPlan> plans = shipPlanRepository.findByApplyIdIn(Lists.newArrayList(appointment.getApplyId()));
                        for (ShipPlan plan : plans) {
                            plan.setAllowInTime(ZonedDateTime.now().plusHours(region.getValidTime()));
                            shipPlanRepository.save(plan);
                            redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, plan.getId());
                        }

                        if (appointment.getUser() != null) {
                            putUserInExpirePenalty(appointment.getUser().getId());
                        }
                    }

                    wxMpMsgService.sendAppointmentExpireMsg(appointmentMapper.toDto(appointment));
                }
            }

            // 进厂之后，有可能拿不到出场时间，会一直占好
            appointments = appointmentRepository.findAllByRegionId(region.getId(), AppointmentStatus.ENTER, Boolean.TRUE, ZonedDateTime.now().minusDays(2));
            for (Appointment appointment : appointments) {
                if (appointment.getApplyId() != null) {
                    List<ShipPlan> plans = shipPlanRepository.findByApplyIdIn(Lists.newArrayList(appointment.getApplyId()));
                    if (!CollectionUtils.isEmpty(plans)) {
                        ShipPlan plan = plans.get(0);
                        if (plan.getLoadingEndTime() != null && plan.getLoadingEndTime().plusHours(1).isBefore(ZonedDateTime.now())) {
                            log.info("[AUTO] set appointment [id={}] status to LEAVE , 1 hour after weight time {}, ShipPlan {}, truckNumber: {}",
                                appointment.getId(),
                                plan.getLoadingStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:SS")),
                                plan.getApplyId(),
                                plan.getTruckNumber());

                            appointment.setStatus(AppointmentStatus.LEAVE);
                            appointment.setUpdateTime(ZonedDateTime.now());
                            appointmentRepository.save(appointment);
                        }
                    }
                }
            }
        }
    }
}
