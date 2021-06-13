package com.shield.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.ShipPlan;
import com.shield.domain.User;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.security.SecurityUtils;
import com.shield.service.*;
import com.shield.repository.AppointmentRepository;
import com.shield.service.common.ValidTransferCheck;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.event.AppointmentChangedEvent;
import com.shield.service.mapper.AppointmentMapper;
import com.shield.service.mapper.RegionMapper;
import com.shield.service.mapper.ShipPlanMapper;
import com.shield.utils.DateUtils;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.web.rest.vm.AppointmentStat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Autowired
    private RegionMapper regionMapper;

    @Autowired
    private ShipPlanRepository shipPlanRepository;


    private static final Long INITIAL_APPOINTMENT_NUMBER = 10000L;
    private static final Long INITIAL_QUEUE_NUMBER = 100L;

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

        new ValidTransferCheck().valid(before, appointmentDTO);

        appointment.setUpdateTime(ZonedDateTime.now());
        Appointment saved = appointmentRepository.save(appointment);
        AppointmentDTO after = appointmentMapper.toDto(saved);

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

    /**
     * 提前排第二天的计划，
     * 只有22-0点之间，才会进入这个流程
     */
    @Override
    public AppointmentDTO makeAppointmentForTomorrow(RegionDTO region, ShipPlanDTO plan, AppointmentDTO appointment) {
        appointment.setRegionId(region.getId());
        if (appointment.getUserId() == null) {
            userService.getUserWithAuthorities()
                .ifPresent(value -> appointment.setUserId(value.getId()));
        }
        appointment.setStatus(AppointmentStatus.CREATE);
        ZonedDateTime tomorrow = DateUtils.tomorrow();

        // 获取明天的有效预约
        List<Appointment> appointments = appointmentRepository.findAllByRegionId(region.getId(), ZonedDateTime.now().minusHours(24), ZonedDateTime.now().plusDays(1));
        appointments = appointments.stream()
            .filter(it -> it.getStartTime() != null
                && it.getStartTime().isAfter(tomorrow)
                && it.isValid()
                && (it.getStatus().equals(AppointmentStatus.START) || it.getStatus().equals(AppointmentStatus.START_CHECK) || it.getStatus().equals(AppointmentStatus.ENTER)))
            .collect(Collectors.toList());

        if (appointments.size() < region.getQuota() * 2) {
            if (appointments.size() < region.getQuota()) {
                // 上半夜，预设有效时间0～3点
                appointment.setStartTime(tomorrow.plusSeconds(1));
            } else {
                // 下半夜，预设有效时间3～6点
                appointment.setStartTime(tomorrow.plusHours(3));
            }
            appointment.setStatus(AppointmentStatus.START_CHECK);
            appointment.setValid(true);
            appointment.setUpdateTime(ZonedDateTime.now());
            appointment.setNumber(generateAppointmentNumber(region.getId()));
            AppointmentDTO saved = this.save(appointment);
            appointment.setId(saved.getId());
            log.info("[Tomorrow] Appointment [{}] made success at region ({}, {}), number: {}, queue number: {}, truckNumber: {}",
                appointment.getId(), region.getId(), region.getName(), appointment.getNumber(), appointment.getQueueNumber(), appointment.getLicensePlateNumber());
            return saved;
        } else {
            if (region.enabledQueue()) {
                Long waitCount = appointmentRepository.countAllWaitByRegionIdAndCreateTime(region.getId(), ZonedDateTime.now().minusHours(48));
                if (region.getQueueQuota() > waitCount) {
                    appointment.setStatus(AppointmentStatus.WAIT);
                    appointment.setQueueNumber(generateQueueNumber(region.getId()));
                    appointment.setValid(true);
                    AppointmentDTO saved = this.save(appointment);
                    appointment.setId(saved.getId());
                    log.info("[Tomorrow] Appointment [{}] enter WAIT queue at region ({}, {}), number: {}, queue number: {}, truckNumber: {}",
                        appointment.getId(), region.getId(), region.getName(), appointment.getNumber(), appointment.getQueueNumber(), appointment.getLicensePlateNumber());
                    return saved;
                }
            } else {
                throw new BadRequestAlertException("当前排队额度已用完", "appointment", "");
            }
        }
        return appointment;
    }

    @Override
    public void setApplyNumber(List<AppointmentDTO> appointmentDTOS) {
        List<Long> applyIds = appointmentDTOS.stream()
            .filter(it -> it.getApplyId() != null)
            .map(AppointmentDTO::getApplyId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(applyIds)) {
            return;
        }
        List<ShipPlan> shipPlans = shipPlanRepository.findByApplyIdIn(applyIds);
        for (AppointmentDTO appointmentDTO : appointmentDTOS) {
            for (ShipPlan shipPlan : shipPlans) {
                if (appointmentDTO.getApplyId() != null && appointmentDTO.getApplyId().equals(shipPlan.getApplyId())) {
                    appointmentDTO.setApplyNumber(shipPlan.getApplyNumber());
                    break;
                }
            }
        }
    }

    /**
     * 预约排队
     */
    @Override
    public AppointmentDTO makeAppointment(Long regionId, AppointmentDTO appointment) {
        RegionDTO region = regionService.findOne(appointment.getRegionId()).get();
        appointment.setRegionId(regionId);
        if (appointment.getUserId() == null) {
            Optional<User> user = userService.getUserWithAuthorities();
            user.ifPresent(value -> appointment.setUserId(value.getId()));
        }

        appointment.setValid(true);
        appointment.setStatus(AppointmentStatus.CREATE);

        Long waitCount = 0L;
        if (region.enabledQueue()) {
            waitCount = appointmentRepository.countAllWaitByRegionIdAndCreateTime(regionId, ZonedDateTime.now().minusHours(48));
        }

        if (region.enabledQueue() && !appointment.isVip() && waitCount > 0) {
            // 当前已经用完额度，开始排队了，后面的预约直接进入排队
            if (region.getQueueQuota() > waitCount) {
                appointment.setStatus(AppointmentStatus.WAIT);
                appointment.setQueueNumber(generateQueueNumber(regionId));
                return this.save(appointment);
            } else {
                appointment.setValid(false);
                throw new BadRequestAlertException("当前排队额度已用完", "appointment", "");
            }
        }

        if (!tryMakeAppointment(appointment)) {
            // 可能由于竞争关系，预约失败，再次检查加入排队
            if (region.enabledQueue() && region.getQueueQuota() > waitCount) {
                appointment.setStatus(AppointmentStatus.WAIT);
                appointment.setQueueNumber(generateQueueNumber(regionId));
                return this.save(appointment);
            } else {
                appointment.setValid(false);
                throw new BadRequestAlertException("当前排队额度已用完", "appointment", "");
            }
        } else {
            // 预约成功
            return appointment;
        }
    }

    /**
     * 统计剩余取号名额
     */
    @Override
    public void countRemainQuota(RegionDTO region, boolean isVip) {
        List<Appointment> appointments = appointmentRepository.findAllValid(region.getId(), ZonedDateTime.now().minusHours(24));
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
        region.setStatusStartCheck(appointments.stream().filter(it -> it.getStatus() == AppointmentStatus.START_CHECK).count());
        region.setStatusEnter(appointments.stream().filter(it -> it.getStatus() == AppointmentStatus.ENTER).count());
    }

    /**
     * 尝试预约抢号
     */
    private boolean tryMakeAppointment(AppointmentDTO appointment) {
        // 检查上一次预约的离场状态
        Appointment lastAppointment = appointmentRepository.findLatestByTruckNumberAndStatus(
            appointment.getRegionId(),
            appointment.getLicensePlateNumber(),
            AppointmentStatus.ENTER,
            ZonedDateTime.now().minusHours(24),
            PageRequest.of(0, 1))
            .stream().findFirst().orElse(null);
        if (lastAppointment != null && lastAppointment.isValid() && lastAppointment.getEnterTime() != null) {
            if (lastAppointment.getLeaveTime() == null
                || lastAppointment.getLeaveTime().plusMinutes(3).isAfter(ZonedDateTime.now())) {
                log.info("上一个预约没有离场，或者刚离场3min之内，无法进行预约，" +
                        "truckNumber: {}, last appointment id: {}",
                    appointment.getLicensePlateNumber(), lastAppointment.getId());
                return false;
            }
        }

        // 进行预约
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
                AppointmentDTO saved = this.save(appointment);
                if (appointment.getId() == null) {
                    appointment.setId(saved.getId());
                }

                log.info("Appointment [{}] made success at region ({}, {}), number: {}, queue number: {}, truckNumber: {}",
                    appointment.getId(), region.getId(), region.getName(), appointment.getNumber(), appointment.getQueueNumber(), appointment.getLicensePlateNumber());
                return true;
            }
            return false;
        }
    }

    private void getAppointmentOutTime(Long regionId, Long hours, List<ZonedDateTime> outTimes) {
        List<Appointment> appointments = appointmentRepository.findAllByRegionIdAndUpdateTime(regionId, ZonedDateTime.now().minusHours(hours), ZonedDateTime.now());
        appointments.sort(Comparator.comparing(Appointment::getUpdateTime));
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
    }


    /**
     * 取号满额时，估计下一个号释放的时间
     */
    @Override
    public Integer calcNextQuotaWaitingTime(RegionDTO region) {
        List<Long> times = Lists.newArrayList();
        List<ZonedDateTime> outTimes = Lists.newArrayList();
        getAppointmentOutTime(region.getId(), 2L, outTimes);
        if (outTimes.size() <= 1) {
            outTimes.clear();
            getAppointmentOutTime(region.getId(), 12L, outTimes);
            log.info("Change to fetch appointments in last 12 hours to calc avg leave time for regionId: {}", region.getId());
        }

        Integer avgGap = 0;
        Integer lastOutGap = 0;
        if (outTimes.size() > 1) {
            for (int i = 1; i < outTimes.size(); i++) {
                times.add(outTimes.get(i).toEpochSecond() - outTimes.get(i - 1).toEpochSecond());
            }
            Double avg = times.stream().mapToInt(Long::intValue).average().orElse(Double.NaN);
            avgGap = avg.intValue();
            lastOutGap = (int) (ZonedDateTime.now().toEpochSecond() - outTimes.get(outTimes.size() - 1).toEpochSecond());
            log.info("Region {}, Calc avg wait time: {}, avg leave gap: {}, size: {}, last leave time: {}, gap: {}",
                region.getId(), avgGap - lastOutGap, avgGap, outTimes.size(), outTimes.get(outTimes.size() - 1), lastOutGap);
        } else {
            log.info("Region {}, set avg wait time to default value 16 min", region.getId());
            avgGap = 16 * 60;
            lastOutGap = 0;
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
                            region.setStatusWaitBeforeUser((long) i + 1);
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

    @Override
    public AppointmentStat countAppointmentStat(ZonedDateTime begin, ZonedDateTime end) {
        AppointmentStat stat = new AppointmentStat();
        List<Region> allRegions = regionRepository.findAll(Sort.by(Sort.Order.desc("id")));
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
        List<AppointmentRepository.AppointmentStatusCount> validData = appointmentRepository.countValidAppointments(begin, end);
        Map<String, AppointmentStat.AppointmentStatItem> regionToStat = Maps.newHashMap();
        for (AppointmentRepository.AppointmentStatusCount item : validData) {
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

        for (AppointmentRepository.AppointmentStatusCount item : data) {
            AppointmentStat.AppointmentStatItem statItem = null;
            if (regionToStat.containsKey(item.getRegion())) {
                statItem = regionToStat.get(item.getRegion());
            } else {
                break;
            }
            if (item.getStatus().equals(AppointmentStatus.EXPIRED)) {
                statItem.setExpired(item.getCount());
            }
        }

        for (RegionDTO regionDTO : regionDTOS) {
            AppointmentStat.AppointmentStatItem item = regionToStat.get(regionDTO.getName());
            if (item == null) {
                item = new AppointmentStat.AppointmentStatItem();
                item.setRegion(regionDTO.getName());
                item.setAvailable(nameToRegion.get(item.getRegion()).getRemainQuota().longValue());
            }
            stat.getData().add(item);
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
    public List<AppointmentDTO> findLatestByTruckNumber(String licensePlateNumber, ZonedDateTime createTime) {
        return appointmentRepository.findLatestByTruckNumber(licensePlateNumber, createTime).stream().map(appointmentMapper::toDto).collect(Collectors.toList());
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
                && (appointment.getStatus() == AppointmentStatus.ENTER
                || appointment.getStatus() == AppointmentStatus.WAIT
                || appointment.getStatus() == AppointmentStatus.START)
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
