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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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


    public Integer generateAppointmentNumber(Long regionId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
//        String key = String.format(APPOINTMENT_NUMBER_KEY, regionId, today);
        String key = "unique_appointment_number";
        if (redisLongTemplate.hasKey(key) == Boolean.FALSE) {
            redisLongTemplate.opsForValue().increment(key, INITIAL_APPOINTMENT_NUMBER);
            redisLongTemplate.expire(key, 7L, TimeUnit.DAYS);
        }
        return redisLongTemplate.opsForValue().increment(key, 1L).intValue();
    }

    public Integer generateQueueNumber(Long regionId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
//        String key = String.format(QUEUE_NUMBER_KEY, regionId, today);
        String key = "unique_queue_number";
        if (redisLongTemplate.hasKey(key) == Boolean.FALSE) {
            redisLongTemplate.opsForValue().increment(key, INITIAL_QUEUE_NUMBER);
            redisLongTemplate.expire(key, 7L, TimeUnit.DAYS);
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
        appointment.setVip(false);
        appointment.setValid(false);
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
            Long current = appointmentRepository.countAllValidByRegionId(region.getId());
            log.debug("Region {}: [{}] status: {}/{}", region.getId(), region.getName(), current, region.getQuota());
            if (current < region.getQuota()) {
                appointment.setStatus(AppointmentStatus.START);
                appointment.setValid(Boolean.TRUE);
                appointment.setStartTime(ZonedDateTime.now());
                appointment.setUpdateTime(ZonedDateTime.now());
                appointment.setNumber(generateAppointmentNumber(region.getId()));
                appointmentRepository.save(appointment);
                log.info("Appointment [{}] made success at region ({}, {}), number: {}, licensePlateNumber: {}",
                    appointment.getId(), region.getId(), region.getName(), appointment.getNumber(), appointment.getLicensePlateNumber());
                return true;
            }
            return false;
        }
    }

    private ZonedDateTime getTodayStartTime() {
        LocalDate today = LocalDate.now();
        LocalTime time = LocalTime.MIN;
        return ZonedDateTime.of(today, time, ZoneId.systemDefault());
    }

    private static final long DEFAULT_VALID_TIME_SECONDS = 7200;

    @Scheduled(fixedRate = 5000)
    public void checkAppointments() {
        for (Region region : regionRepository.findAll()) {
            Long validSeconds = region.getValidTime() != null ? region.getValidTime() : DEFAULT_VALID_TIME_SECONDS;
            List<Appointment> appointments = appointmentRepository.findAllByRegionId(region.getId(), AppointmentStatus.START, Boolean.TRUE);
            for (Appointment appointment : appointments) {
                if (appointment.getStartTime() == null || appointment.getStartTime().plusSeconds(validSeconds).isBefore(ZonedDateTime.now())) {
                    log.info("Appointment [{}] expired after {} seconds", appointment.getId(), validSeconds);
                    appointment.setUpdateTime(ZonedDateTime.now());
                    appointment.setValid(false);
                    appointmentRepository.save(appointment);
                }
            }

            List<Appointment> waitingList = appointmentRepository.findWaitingList(region.getId());
            waitingList.sort((Comparator.comparing(Appointment::getCreateTime)));
            for (Appointment appointment : waitingList) {
                if (region.getQueueValidTime() != null && appointment.getCreateTime().plusSeconds(region.getQueueValidTime()).isBefore(ZonedDateTime.now())) {
                    log.info("Appointment [{}] queue expired after {} seconds", appointment.getId(), region.getQueueValidTime());
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
