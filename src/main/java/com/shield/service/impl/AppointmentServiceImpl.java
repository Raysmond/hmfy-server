package com.shield.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.User;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.repository.RegionRepository;
import com.shield.security.SecurityUtils;
import com.shield.service.*;
import com.shield.repository.AppointmentRepository;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.event.AppointmentChangedEvent;
import com.shield.service.mapper.AppointmentMapper;
import com.shield.service.mapper.RegionMapper;
import com.shield.service.mapper.ShipPlanMapper;
import com.shield.web.rest.vm.AppointmentStat;
import org.apache.commons.lang3.StringUtils;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.shield.config.Constants.*;

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
    @Qualifier("redisLongTemplate")
    private RedisTemplate<String, Long> redisLongTemplate;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ShipPlanService shipPlanService;

    @Autowired
    private ShipPlanMapper shipPlanMapper;

    @Autowired
    private RegionService regionService;

    private static final Long INITIAL_APPOINTMENT_NUMBER = 10000L;
    private static final Long INITIAL_QUEUE_NUMBER = 100L;

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
        AppointmentDTO before = null;
        if (appointment.getId() == null) {
            appointment.setCreateTime(ZonedDateTime.now());
        } else {
            before = appointmentMapper.toDto(appointmentRepository.findById(appointmentDTO.getId()).get());
        }
        appointment.setUpdateTime(ZonedDateTime.now());
        appointment = appointmentRepository.save(appointment);
        AppointmentDTO after = appointmentMapper.toDto(appointment);

        applicationEventPublisher.publishEvent(new AppointmentChangedEvent(this, before, after));
        return after;
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
    public AppointmentDTO makeAppointment(Long regionId, AppointmentDTO appointment) {
        Region region = regionRepository.getOne(regionId);
        appointment.setRegionId(regionId);
        if (appointment.getUserId() == null) {
            Optional<User> user = userService.getUserWithAuthorities();
            if (user.isPresent()) {
                appointment.setUserId(user.get().getId());
            }
        }
        appointment.setValid(true);
        appointment.setStatus(AppointmentStatus.CREATE);

        boolean enableQueue = region.getQueueQuota() != null && region.getQueueQuota() > 0;
        if (enableQueue && !appointment.isVip()) {
            Long waitCount = appointmentRepository.countAllWaitByRegionIdAndCreateTime(regionId, ZonedDateTime.now().minusHours(12));
            if (waitCount > 0) {
                if (region.getQueueQuota() > waitCount) {
                    appointment.setStatus(AppointmentStatus.WAIT);
                    appointment.setQueueNumber(generateQueueNumber(regionId));
                } else {
                    appointment.setValid(false);
                }
                return this.save(appointment);
            }
        }

        if (!tryMakeAppointment(appointment)) {
            if (enableQueue && region.getQueueQuota() > appointmentRepository.countAllWaitByRegionIdAndCreateTime(regionId, ZonedDateTime.now().minusHours(12))) {
                appointment.setStatus(AppointmentStatus.WAIT);
                appointment.setQueueNumber(generateQueueNumber(regionId));
            } else {
                appointment.setValid(false);
            }
            appointment = this.save(appointment);
        }
        return appointment;
    }

    /**
     * 统计剩余取号名额
     */
    @Override
    public void countRemainQuota(RegionDTO region, boolean isVip) {
        List<Appointment> appointments = appointmentRepository.findAllValid(region.getId(), ZonedDateTime.now().minusHours(6));
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
    private boolean tryMakeAppointment(AppointmentDTO appointment) {
        synchronized (this) {
            RegionDTO region = regionService.findOne(appointment.getRegionId()).get();
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
                this.save(appointment);

                log.info("Appointment [{}] made success at region ({}, {}), number: {}, queue number: {}, truckNumber: {}",
                    appointment.getId(), region.getId(), region.getName(), appointment.getNumber(), appointment.getQueueNumber(), appointment.getLicensePlateNumber());
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


    @Override
    public void expireAppointment(AppointmentDTO appointment) {
        log.info("Appointment [{}] expired after {} hours", appointment.getId(), regionRepository.findById(appointment.getRegionId()).get().getValidTime());
        appointment.setValid(false);
        appointment.setStatus(AppointmentStatus.EXPIRED);
        appointment.setUpdateTime(ZonedDateTime.now());
        appointment.setExpireTime(ZonedDateTime.now());
        this.save(appointment);
    }


    /**
     * 拿不到出场时间失效：下磅之后1h，还没有拿到出场时间，则设置出场时间为当前系统时间
     */
    @Override
    public void autoSetAppointmentLeave(AppointmentDTO appointment) {
        ShipPlanDTO plan = shipPlanService.findOneByApplyId(appointment.getApplyId());
        log.info("[AUTO] set appointment [id={}] status to LEAVE, 1 hour after weight time {}, ShipPlan {}, truckNumber: {}",
            appointment.getId(),
            plan.getLoadingStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:SS")),
            plan.getApplyId(),
            plan.getTruckNumber());

        appointment.setStatus(AppointmentStatus.LEAVE);
        appointment.setUpdateTime(ZonedDateTime.now());
        this.save(appointment);
    }

    @Autowired
    private RegionMapper regionMapper;

    @Override
    public AppointmentStat countAppointmentStat(ZonedDateTime begin, ZonedDateTime end) {
        AppointmentStat stat = new AppointmentStat();
        List<Region> allRegions = regionRepository.findAll();
        List<String> regions = allRegions.stream().map(Region::getName).collect(Collectors.toList());
        List<RegionDTO> regionDTOS = allRegions.stream().map(it -> regionMapper.toDto(it)).collect(Collectors.toList());
        Map<String, RegionDTO> nameToRegion = Maps.newHashMap();
        for (RegionDTO regionDTO : regionDTOS) {
            this.countRemainQuota(regionDTO, false);
            nameToRegion.put(regionDTO.getName(), regionDTO);
        }

        stat.setRegions(regions);
        stat.setDate(begin.format(DateTimeFormatter.BASIC_ISO_DATE));
        List<AppointmentRepository.AppointmentStatusCount> data = appointmentRepository.countAppointments(begin, end);
        Map<String, AppointmentStat.AppointmentStatItem> regionToStat = Maps.newHashMap();
        for (AppointmentRepository.AppointmentStatusCount item : data) {
            AppointmentStat.AppointmentStatItem statItem = new AppointmentStat.AppointmentStatItem();
            if (regionToStat.containsKey(item.getRegion())) {
                statItem = regionToStat.get(item.getRegion());
            }
            statItem.setAvailable(nameToRegion.get(item.getRegion()).getRemainQuota().longValue());
            statItem.setRegion(item.getRegion());
            switch (item.getStatus()) {
                case WAIT:
                    statItem.setWait(item.getCount());
                    break;
                case START:
                case START_CHECK:
                    statItem.setStart(item.getCount());
                    break;
                case EXPIRED:
                    statItem.setExpired(item.getCount());
                    break;
                case ENTER:
                    statItem.setEnter(item.getCount());
                    break;
                case LEAVE:
                    statItem.setLeave(item.getCount());
                    break;
            }
            regionToStat.put(item.getRegion(), statItem);
        }
        regionToStat.forEach((k, v) -> {
            stat.getData().add(v);
        });
        for (String region : regions) {
            if (!regionToStat.containsKey(region)) {
                AppointmentStat.AppointmentStatItem item = new AppointmentStat.AppointmentStatItem();
                item.setRegion(region);
                item.setAvailable(nameToRegion.get(item.getRegion()).getRemainQuota().longValue());
                stat.getData().add(item);
            }
        }
        return stat;
    }


    private String getHour(ZonedDateTime t) {
        int hour = t.getHour();
        return hour < 10 ? "0" + hour : "" + hour;
    }

    @Override
    public AppointmentStat countAppointmentStat(String currentRegion, ZonedDateTime begin, ZonedDateTime end) {
        Region region = regionRepository.findOneByName(currentRegion);
        List<Appointment> appointments = appointmentRepository.findAllByCreateTime(region.getId(), begin, end);
        AppointmentStat stat = new AppointmentStat();
        stat.setDate(begin.format(DateTimeFormatter.BASIC_ISO_DATE));
        List<Region> allRegions = regionRepository.findAll();
        List<String> regionNames = allRegions.stream().map(Region::getName).collect(Collectors.toList());
        List<RegionDTO> regionDTOS = allRegions.stream().map(it -> regionMapper.toDto(it)).collect(Collectors.toList());
        Map<String, RegionDTO> nameToRegion = Maps.newHashMap();
        for (RegionDTO regionDTO : regionDTOS) {
            this.countRemainQuota(regionDTO, false);
            nameToRegion.put(regionDTO.getName(), regionDTO);
        }
        stat.setRegions(regionNames);
        Map<String, AppointmentStat.AppointmentStatItem> hourStat = Maps.newHashMap();
        for (int hour = 0; hour < 24; hour++) {
//        for (int hour = 0; hour <= ZonedDateTime.now().getHour(); hour++) {
            for (String regionName : regionNames) {
                String k = hour < 10 ? "0" + hour : "" + hour;
                AppointmentStat.AppointmentStatItem item = new AppointmentStat.AppointmentStatItem();
                item.setRegion(regionName);
                item.setHour(k);
                hourStat.put(regionName + "_" + k, item);
            }
        }

        for (Appointment appointment : appointments) {
            if (appointment.getQueueNumber() != null) {
                AppointmentStat.AppointmentStatItem item = hourStat.get(appointment.getRegion().getName() + "_" + getHour(appointment.getCreateTime()));
                item.setWait(item.getWait() + 1);
            }
            if (appointment.getStartTime() != null) {
                AppointmentStat.AppointmentStatItem item = hourStat.get(appointment.getRegion().getName() + "_" + getHour(appointment.getStartTime()));
                item.setStart(item.getStart() + 1);
            }
            if (appointment.getEnterTime() != null) {
                AppointmentStat.AppointmentStatItem item = hourStat.get(appointment.getRegion().getName() + "_" + getHour(appointment.getEnterTime()));
                item.setEnter(item.getEnter() + 1);
            }
            if (appointment.getLeaveTime() != null) {
                AppointmentStat.AppointmentStatItem item = hourStat.get(appointment.getRegion().getName() + "_" + getHour(appointment.getLeaveTime()));
                item.setLeave(item.getLeave() + 1);
            }
            if (appointment.getExpireTime() != null) {
                AppointmentStat.AppointmentStatItem item = hourStat.get(appointment.getRegion().getName() + "_" + getHour(appointment.getExpireTime()));
                item.setExpired(item.getExpired() + 1);
            }
        }

        hourStat.forEach((k, v) -> {
            stat.getData().add(v);
        });
        return stat;
    }

    @Override
    public void expireWaitAppointment(AppointmentDTO appointment) {
        log.info("Appointment [{}] queue expired after {} hours", appointment.getId(), regionRepository.findById(appointment.getRegionId()).get().getQueueValidTime());
        appointment.setValid(false);
        appointment.setStatus(AppointmentStatus.EXPIRED);
        appointment.setExpireTime(ZonedDateTime.now());
        this.save(appointment);
    }

    @Override
    public boolean autoMakeAppointmentForWaitUser(AppointmentDTO appointment) {
        log.info("[BEGIN] auto make appointment for appointment in WAIT status, id={}, truckNumber: {}, wait number: {}",
            appointment.getId(), appointment.getLicensePlateNumber(), appointment.getQueueNumber());
        if (this.tryMakeAppointment(appointment)) {
            return true;
        } else {
            log.warn("Failed to auto make appointment");
            return false;
        }
    }

    /**
     * 如果计划取消了，还未进厂，则将预约作废
     */
    @Override
    public void updateStatusAfterCancelShipPlan(Long applyId) {
        List<Appointment> appointments = appointmentRepository.findByApplyIdIn(Lists.newArrayList(applyId));
        if (!CollectionUtils.isEmpty(appointments)) {
            appointments.sort(Comparator.comparing(Appointment::getCreateTime).reversed());
            AppointmentDTO appointment = appointmentMapper.toDto(appointments.get(0));
            if (appointment.isValid()
                && (appointment.getStatus() == AppointmentStatus.START
                || appointment.getStatus() == AppointmentStatus.START_CHECK
                || appointment.getStatus() == AppointmentStatus.WAIT)
            ) {
                appointment.setValid(Boolean.FALSE);
                appointment.setUpdateTime(ZonedDateTime.now());
                this.save(appointment);
                log.info("Set appointment[id={}], truckNumber: {} valid=false after ShipPlan[applyId={}] is canceled",
                    appointment.getId(), appointment.getLicensePlateNumber(), applyId);
            }
        }
    }

    @Override
    public AppointmentDTO cancelAppointment(Long appointmentId) {
        AppointmentDTO appointment = appointmentMapper.toDto(appointmentRepository.getOne(appointmentId));
        appointment.setValid(Boolean.FALSE);
        appointment.setStatus(AppointmentStatus.CANCELED);
        return this.save(appointment);
    }
}
