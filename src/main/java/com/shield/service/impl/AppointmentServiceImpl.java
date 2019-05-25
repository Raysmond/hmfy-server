package com.shield.service.impl;

import com.google.common.collect.Sets;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.repository.RegionRepository;
import com.shield.service.AppointmentService;
import com.shield.repository.AppointmentRepository;
import com.shield.service.UserService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.mapper.AppointmentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service Implementation for managing {@link Appointment}.
 */
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
    private static final String APPOINTMENT_NUMBER_KEY = "appointment_%s_%s";
    private static final String QUEUE_NUMBER_KEY = "queue_number_%s_%s";
    private static final Long INITIAL_APPOINTMENT_NUMBER = 10000L;
    private static final Long INITIAL_QUEUE_NUMBER = 100L;


    private static final Set<AppointmentStatus> ACTIVE_STATUS = Sets.newHashSet(
        AppointmentStatus.WAIT,
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


    public Integer generateAppointmentNumber(Long regionId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String key = String.format(APPOINTMENT_NUMBER_KEY, regionId, today);
        if (redisLongTemplate.hasKey(key) == Boolean.FALSE) {
            redisLongTemplate.opsForValue().increment(key, INITIAL_APPOINTMENT_NUMBER);
            redisLongTemplate.expire(key, 7L, TimeUnit.DAYS);
        }
        return redisLongTemplate.opsForValue().increment(key, 1L).intValue();
    }

    public Integer generateQueueNumber(Long regionId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String key = String.format(QUEUE_NUMBER_KEY, regionId, today);
        if (redisLongTemplate.hasKey(key) == Boolean.FALSE) {
            redisLongTemplate.opsForValue().increment(key, INITIAL_QUEUE_NUMBER);
            redisLongTemplate.expire(key, 7L, TimeUnit.DAYS);
        }
        return redisLongTemplate.opsForValue().increment(key, 1L).intValue();
    }

    @Override
    public AppointmentDTO makeAppointment(Long regionId, AppointmentDTO appointmentDTO) {
        Region region = regionRepository.getOne(regionId);
        LocalDate today = LocalDate.now();
        LocalTime time = LocalTime.MIN;
        ZonedDateTime startTime = ZonedDateTime.of(today, time, ZoneId.systemDefault());
        ZonedDateTime endTime = startTime.plusHours(24);
        List<Appointment> appointments = appointmentRepository.findAllByRegionId(regionId, startTime, endTime);
        int current = (int) appointments.stream().filter(it -> null != it.getStatus() && ACTIVE_STATUS.contains(it.getStatus())).count();
        log.info("Region {}: [{}] status: {}/{}, WAIT: {}, START: {}, ENTER: {}, LEAVE: {}",
            region.getId(), region.getName(), current, region.getQuota(),
            appointments.stream().filter(it -> it.getStatus() == AppointmentStatus.WAIT).count(),
            appointments.stream().filter(it -> it.getStatus() == AppointmentStatus.START).count(),
            appointments.stream().filter(it -> it.getStatus() == AppointmentStatus.ENTER).count(),
            appointments.stream().filter(it -> it.getStatus() == AppointmentStatus.LEAVE).count()
        );

        Appointment appointment = new Appointment();
        appointment.setLicensePlateNumber(appointmentDTO.getLicensePlateNumber());
        appointment.setDriver(appointmentDTO.getDriver());
        appointment.setCreateTime(ZonedDateTime.now());
        appointment.setUpdateTime(ZonedDateTime.now());
        appointment.setRegion(region);
        appointment.setUser(userService.getUserWithAuthorities().get());
        appointment.setVip(false);

        if (current < region.getQuota()) {
            appointment.setValid(true);
            appointment.setStatus(AppointmentStatus.START);
            appointment.setStartTime(ZonedDateTime.now());
            appointment.setNumber(generateAppointmentNumber(regionId));
        } else {
            appointment.setValid(false);
            appointment.setStatus(AppointmentStatus.WAIT);
            appointment.setQueueNumber(generateQueueNumber(regionId));
        }

        appointmentRepository.save(appointment);
        return appointmentMapper.toDto(appointment);
    }
}
