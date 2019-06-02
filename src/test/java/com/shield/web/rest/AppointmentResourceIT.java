package com.shield.web.rest;

import com.shield.ShieldApp;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.User;
import com.shield.repository.AppointmentRepository;
import com.shield.service.AppointmentService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.mapper.AppointmentMapper;
import com.shield.web.rest.errors.ExceptionTranslator;
import com.shield.service.dto.AppointmentCriteria;
import com.shield.service.AppointmentQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static com.shield.web.rest.TestUtil.sameInstant;
import static com.shield.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shield.domain.enumeration.AppointmentStatus;
/**
 * Integration tests for the {@Link AppointmentResource} REST controller.
 */
@SpringBootTest(classes = ShieldApp.class)
public class AppointmentResourceIT {

    private static final String DEFAULT_LICENSE_PLATE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_LICENSE_PLATE_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_DRIVER = "AAAAAAAAAA";
    private static final String UPDATED_DRIVER = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final Integer DEFAULT_NUMBER = 1;
    private static final Integer UPDATED_NUMBER = 2;

    private static final Boolean DEFAULT_VALID = false;
    private static final Boolean UPDATED_VALID = true;

    private static final AppointmentStatus DEFAULT_STATUS = AppointmentStatus.CREATE;
    private static final AppointmentStatus UPDATED_STATUS = AppointmentStatus.WAIT;

    private static final Integer DEFAULT_QUEUE_NUMBER = 1;
    private static final Integer UPDATED_QUEUE_NUMBER = 2;

    private static final Boolean DEFAULT_VIP = false;
    private static final Boolean UPDATED_VIP = true;

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_START_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_START_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_ENTER_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_ENTER_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_LEAVE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_LEAVE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_EXPIRE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_EXPIRE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentQueryService appointmentQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restAppointmentMockMvc;

    private Appointment appointment;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AppointmentResource appointmentResource = new AppointmentResource(appointmentService, appointmentQueryService);
        this.restAppointmentMockMvc = MockMvcBuilders.standaloneSetup(appointmentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Appointment createEntity(EntityManager em) {
        Appointment appointment = new Appointment()
            .licensePlateNumber(DEFAULT_LICENSE_PLATE_NUMBER)
            .driver(DEFAULT_DRIVER)
            .phone(DEFAULT_PHONE)
            .number(DEFAULT_NUMBER)
            .valid(DEFAULT_VALID)
            .status(DEFAULT_STATUS)
            .queueNumber(DEFAULT_QUEUE_NUMBER)
            .vip(DEFAULT_VIP)
            .createTime(DEFAULT_CREATE_TIME)
            .updateTime(DEFAULT_UPDATE_TIME)
            .startTime(DEFAULT_START_TIME)
            .enterTime(DEFAULT_ENTER_TIME)
            .leaveTime(DEFAULT_LEAVE_TIME)
            .expireTime(DEFAULT_EXPIRE_TIME);
        // Add required entity
        Region region;
        if (TestUtil.findAll(em, Region.class).isEmpty()) {
            region = RegionResourceIT.createEntity(em);
            em.persist(region);
            em.flush();
        } else {
            region = TestUtil.findAll(em, Region.class).get(0);
        }
        appointment.setRegion(region);
        // Add required entity
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        appointment.setUser(user);
        return appointment;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Appointment createUpdatedEntity(EntityManager em) {
        Appointment appointment = new Appointment()
            .licensePlateNumber(UPDATED_LICENSE_PLATE_NUMBER)
            .driver(UPDATED_DRIVER)
            .phone(UPDATED_PHONE)
            .number(UPDATED_NUMBER)
            .valid(UPDATED_VALID)
            .status(UPDATED_STATUS)
            .queueNumber(UPDATED_QUEUE_NUMBER)
            .vip(UPDATED_VIP)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME)
            .startTime(UPDATED_START_TIME)
            .enterTime(UPDATED_ENTER_TIME)
            .leaveTime(UPDATED_LEAVE_TIME)
            .expireTime(UPDATED_EXPIRE_TIME);
        // Add required entity
        Region region;
        if (TestUtil.findAll(em, Region.class).isEmpty()) {
            region = RegionResourceIT.createUpdatedEntity(em);
            em.persist(region);
            em.flush();
        } else {
            region = TestUtil.findAll(em, Region.class).get(0);
        }
        appointment.setRegion(region);
        // Add required entity
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        appointment.setUser(user);
        return appointment;
    }

    @BeforeEach
    public void initTest() {
        appointment = createEntity(em);
    }

    @Test
    @Transactional
    public void createAppointment() throws Exception {
        int databaseSizeBeforeCreate = appointmentRepository.findAll().size();

        // Create the Appointment
        AppointmentDTO appointmentDTO = appointmentMapper.toDto(appointment);
        restAppointmentMockMvc.perform(post("/api/appointments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(appointmentDTO)))
            .andExpect(status().isCreated());

        // Validate the Appointment in the database
        List<Appointment> appointmentList = appointmentRepository.findAll();
        assertThat(appointmentList).hasSize(databaseSizeBeforeCreate + 1);
        Appointment testAppointment = appointmentList.get(appointmentList.size() - 1);
        assertThat(testAppointment.getLicensePlateNumber()).isEqualTo(DEFAULT_LICENSE_PLATE_NUMBER);
        assertThat(testAppointment.getDriver()).isEqualTo(DEFAULT_DRIVER);
        assertThat(testAppointment.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(testAppointment.getNumber()).isEqualTo(DEFAULT_NUMBER);
        assertThat(testAppointment.isValid()).isEqualTo(DEFAULT_VALID);
        assertThat(testAppointment.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testAppointment.getQueueNumber()).isEqualTo(DEFAULT_QUEUE_NUMBER);
        assertThat(testAppointment.isVip()).isEqualTo(DEFAULT_VIP);
        assertThat(testAppointment.getCreateTime()).isEqualTo(DEFAULT_CREATE_TIME);
        assertThat(testAppointment.getUpdateTime()).isEqualTo(DEFAULT_UPDATE_TIME);
        assertThat(testAppointment.getStartTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(testAppointment.getEnterTime()).isEqualTo(DEFAULT_ENTER_TIME);
        assertThat(testAppointment.getLeaveTime()).isEqualTo(DEFAULT_LEAVE_TIME);
        assertThat(testAppointment.getExpireTime()).isEqualTo(DEFAULT_EXPIRE_TIME);
    }

    @Test
    @Transactional
    public void createAppointmentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = appointmentRepository.findAll().size();

        // Create the Appointment with an existing ID
        appointment.setId(1L);
        AppointmentDTO appointmentDTO = appointmentMapper.toDto(appointment);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAppointmentMockMvc.perform(post("/api/appointments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(appointmentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Appointment in the database
        List<Appointment> appointmentList = appointmentRepository.findAll();
        assertThat(appointmentList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkLicensePlateNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = appointmentRepository.findAll().size();
        // set the field null
        appointment.setLicensePlateNumber(null);

        // Create the Appointment, which fails.
        AppointmentDTO appointmentDTO = appointmentMapper.toDto(appointment);

        restAppointmentMockMvc.perform(post("/api/appointments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(appointmentDTO)))
            .andExpect(status().isBadRequest());

        List<Appointment> appointmentList = appointmentRepository.findAll();
        assertThat(appointmentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDriverIsRequired() throws Exception {
        int databaseSizeBeforeTest = appointmentRepository.findAll().size();
        // set the field null
        appointment.setDriver(null);

        // Create the Appointment, which fails.
        AppointmentDTO appointmentDTO = appointmentMapper.toDto(appointment);

        restAppointmentMockMvc.perform(post("/api/appointments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(appointmentDTO)))
            .andExpect(status().isBadRequest());

        List<Appointment> appointmentList = appointmentRepository.findAll();
        assertThat(appointmentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValidIsRequired() throws Exception {
        int databaseSizeBeforeTest = appointmentRepository.findAll().size();
        // set the field null
        appointment.setValid(null);

        // Create the Appointment, which fails.
        AppointmentDTO appointmentDTO = appointmentMapper.toDto(appointment);

        restAppointmentMockMvc.perform(post("/api/appointments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(appointmentDTO)))
            .andExpect(status().isBadRequest());

        List<Appointment> appointmentList = appointmentRepository.findAll();
        assertThat(appointmentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = appointmentRepository.findAll().size();
        // set the field null
        appointment.setStatus(null);

        // Create the Appointment, which fails.
        AppointmentDTO appointmentDTO = appointmentMapper.toDto(appointment);

        restAppointmentMockMvc.perform(post("/api/appointments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(appointmentDTO)))
            .andExpect(status().isBadRequest());

        List<Appointment> appointmentList = appointmentRepository.findAll();
        assertThat(appointmentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVipIsRequired() throws Exception {
        int databaseSizeBeforeTest = appointmentRepository.findAll().size();
        // set the field null
        appointment.setVip(null);

        // Create the Appointment, which fails.
        AppointmentDTO appointmentDTO = appointmentMapper.toDto(appointment);

        restAppointmentMockMvc.perform(post("/api/appointments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(appointmentDTO)))
            .andExpect(status().isBadRequest());

        List<Appointment> appointmentList = appointmentRepository.findAll();
        assertThat(appointmentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAppointments() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList
        restAppointmentMockMvc.perform(get("/api/appointments?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appointment.getId().intValue())))
            .andExpect(jsonPath("$.[*].licensePlateNumber").value(hasItem(DEFAULT_LICENSE_PLATE_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].driver").value(hasItem(DEFAULT_DRIVER.toString())))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE.toString())))
            .andExpect(jsonPath("$.[*].number").value(hasItem(DEFAULT_NUMBER)))
            .andExpect(jsonPath("$.[*].valid").value(hasItem(DEFAULT_VALID.booleanValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].queueNumber").value(hasItem(DEFAULT_QUEUE_NUMBER)))
            .andExpect(jsonPath("$.[*].vip").value(hasItem(DEFAULT_VIP.booleanValue())))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(sameInstant(DEFAULT_START_TIME))))
            .andExpect(jsonPath("$.[*].enterTime").value(hasItem(sameInstant(DEFAULT_ENTER_TIME))))
            .andExpect(jsonPath("$.[*].leaveTime").value(hasItem(sameInstant(DEFAULT_LEAVE_TIME))))
            .andExpect(jsonPath("$.[*].expireTime").value(hasItem(sameInstant(DEFAULT_EXPIRE_TIME))));
    }
    
    @Test
    @Transactional
    public void getAppointment() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get the appointment
        restAppointmentMockMvc.perform(get("/api/appointments/{id}", appointment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(appointment.getId().intValue()))
            .andExpect(jsonPath("$.licensePlateNumber").value(DEFAULT_LICENSE_PLATE_NUMBER.toString()))
            .andExpect(jsonPath("$.driver").value(DEFAULT_DRIVER.toString()))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE.toString()))
            .andExpect(jsonPath("$.number").value(DEFAULT_NUMBER))
            .andExpect(jsonPath("$.valid").value(DEFAULT_VALID.booleanValue()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.queueNumber").value(DEFAULT_QUEUE_NUMBER))
            .andExpect(jsonPath("$.vip").value(DEFAULT_VIP.booleanValue()))
            .andExpect(jsonPath("$.createTime").value(sameInstant(DEFAULT_CREATE_TIME)))
            .andExpect(jsonPath("$.updateTime").value(sameInstant(DEFAULT_UPDATE_TIME)))
            .andExpect(jsonPath("$.startTime").value(sameInstant(DEFAULT_START_TIME)))
            .andExpect(jsonPath("$.enterTime").value(sameInstant(DEFAULT_ENTER_TIME)))
            .andExpect(jsonPath("$.leaveTime").value(sameInstant(DEFAULT_LEAVE_TIME)))
            .andExpect(jsonPath("$.expireTime").value(sameInstant(DEFAULT_EXPIRE_TIME)));
    }

    @Test
    @Transactional
    public void getAllAppointmentsByLicensePlateNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where licensePlateNumber equals to DEFAULT_LICENSE_PLATE_NUMBER
        defaultAppointmentShouldBeFound("licensePlateNumber.equals=" + DEFAULT_LICENSE_PLATE_NUMBER);

        // Get all the appointmentList where licensePlateNumber equals to UPDATED_LICENSE_PLATE_NUMBER
        defaultAppointmentShouldNotBeFound("licensePlateNumber.equals=" + UPDATED_LICENSE_PLATE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByLicensePlateNumberIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where licensePlateNumber in DEFAULT_LICENSE_PLATE_NUMBER or UPDATED_LICENSE_PLATE_NUMBER
        defaultAppointmentShouldBeFound("licensePlateNumber.in=" + DEFAULT_LICENSE_PLATE_NUMBER + "," + UPDATED_LICENSE_PLATE_NUMBER);

        // Get all the appointmentList where licensePlateNumber equals to UPDATED_LICENSE_PLATE_NUMBER
        defaultAppointmentShouldNotBeFound("licensePlateNumber.in=" + UPDATED_LICENSE_PLATE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByLicensePlateNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where licensePlateNumber is not null
        defaultAppointmentShouldBeFound("licensePlateNumber.specified=true");

        // Get all the appointmentList where licensePlateNumber is null
        defaultAppointmentShouldNotBeFound("licensePlateNumber.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByDriverIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where driver equals to DEFAULT_DRIVER
        defaultAppointmentShouldBeFound("driver.equals=" + DEFAULT_DRIVER);

        // Get all the appointmentList where driver equals to UPDATED_DRIVER
        defaultAppointmentShouldNotBeFound("driver.equals=" + UPDATED_DRIVER);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByDriverIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where driver in DEFAULT_DRIVER or UPDATED_DRIVER
        defaultAppointmentShouldBeFound("driver.in=" + DEFAULT_DRIVER + "," + UPDATED_DRIVER);

        // Get all the appointmentList where driver equals to UPDATED_DRIVER
        defaultAppointmentShouldNotBeFound("driver.in=" + UPDATED_DRIVER);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByDriverIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where driver is not null
        defaultAppointmentShouldBeFound("driver.specified=true");

        // Get all the appointmentList where driver is null
        defaultAppointmentShouldNotBeFound("driver.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByPhoneIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where phone equals to DEFAULT_PHONE
        defaultAppointmentShouldBeFound("phone.equals=" + DEFAULT_PHONE);

        // Get all the appointmentList where phone equals to UPDATED_PHONE
        defaultAppointmentShouldNotBeFound("phone.equals=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByPhoneIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where phone in DEFAULT_PHONE or UPDATED_PHONE
        defaultAppointmentShouldBeFound("phone.in=" + DEFAULT_PHONE + "," + UPDATED_PHONE);

        // Get all the appointmentList where phone equals to UPDATED_PHONE
        defaultAppointmentShouldNotBeFound("phone.in=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByPhoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where phone is not null
        defaultAppointmentShouldBeFound("phone.specified=true");

        // Get all the appointmentList where phone is null
        defaultAppointmentShouldNotBeFound("phone.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where number equals to DEFAULT_NUMBER
        defaultAppointmentShouldBeFound("number.equals=" + DEFAULT_NUMBER);

        // Get all the appointmentList where number equals to UPDATED_NUMBER
        defaultAppointmentShouldNotBeFound("number.equals=" + UPDATED_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByNumberIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where number in DEFAULT_NUMBER or UPDATED_NUMBER
        defaultAppointmentShouldBeFound("number.in=" + DEFAULT_NUMBER + "," + UPDATED_NUMBER);

        // Get all the appointmentList where number equals to UPDATED_NUMBER
        defaultAppointmentShouldNotBeFound("number.in=" + UPDATED_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where number is not null
        defaultAppointmentShouldBeFound("number.specified=true");

        // Get all the appointmentList where number is null
        defaultAppointmentShouldNotBeFound("number.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByNumberIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where number greater than or equals to DEFAULT_NUMBER
        defaultAppointmentShouldBeFound("number.greaterOrEqualThan=" + DEFAULT_NUMBER);

        // Get all the appointmentList where number greater than or equals to UPDATED_NUMBER
        defaultAppointmentShouldNotBeFound("number.greaterOrEqualThan=" + UPDATED_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByNumberIsLessThanSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where number less than or equals to DEFAULT_NUMBER
        defaultAppointmentShouldNotBeFound("number.lessThan=" + DEFAULT_NUMBER);

        // Get all the appointmentList where number less than or equals to UPDATED_NUMBER
        defaultAppointmentShouldBeFound("number.lessThan=" + UPDATED_NUMBER);
    }


    @Test
    @Transactional
    public void getAllAppointmentsByValidIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where valid equals to DEFAULT_VALID
        defaultAppointmentShouldBeFound("valid.equals=" + DEFAULT_VALID);

        // Get all the appointmentList where valid equals to UPDATED_VALID
        defaultAppointmentShouldNotBeFound("valid.equals=" + UPDATED_VALID);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByValidIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where valid in DEFAULT_VALID or UPDATED_VALID
        defaultAppointmentShouldBeFound("valid.in=" + DEFAULT_VALID + "," + UPDATED_VALID);

        // Get all the appointmentList where valid equals to UPDATED_VALID
        defaultAppointmentShouldNotBeFound("valid.in=" + UPDATED_VALID);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByValidIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where valid is not null
        defaultAppointmentShouldBeFound("valid.specified=true");

        // Get all the appointmentList where valid is null
        defaultAppointmentShouldNotBeFound("valid.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where status equals to DEFAULT_STATUS
        defaultAppointmentShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the appointmentList where status equals to UPDATED_STATUS
        defaultAppointmentShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultAppointmentShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the appointmentList where status equals to UPDATED_STATUS
        defaultAppointmentShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where status is not null
        defaultAppointmentShouldBeFound("status.specified=true");

        // Get all the appointmentList where status is null
        defaultAppointmentShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByQueueNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where queueNumber equals to DEFAULT_QUEUE_NUMBER
        defaultAppointmentShouldBeFound("queueNumber.equals=" + DEFAULT_QUEUE_NUMBER);

        // Get all the appointmentList where queueNumber equals to UPDATED_QUEUE_NUMBER
        defaultAppointmentShouldNotBeFound("queueNumber.equals=" + UPDATED_QUEUE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByQueueNumberIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where queueNumber in DEFAULT_QUEUE_NUMBER or UPDATED_QUEUE_NUMBER
        defaultAppointmentShouldBeFound("queueNumber.in=" + DEFAULT_QUEUE_NUMBER + "," + UPDATED_QUEUE_NUMBER);

        // Get all the appointmentList where queueNumber equals to UPDATED_QUEUE_NUMBER
        defaultAppointmentShouldNotBeFound("queueNumber.in=" + UPDATED_QUEUE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByQueueNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where queueNumber is not null
        defaultAppointmentShouldBeFound("queueNumber.specified=true");

        // Get all the appointmentList where queueNumber is null
        defaultAppointmentShouldNotBeFound("queueNumber.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByQueueNumberIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where queueNumber greater than or equals to DEFAULT_QUEUE_NUMBER
        defaultAppointmentShouldBeFound("queueNumber.greaterOrEqualThan=" + DEFAULT_QUEUE_NUMBER);

        // Get all the appointmentList where queueNumber greater than or equals to UPDATED_QUEUE_NUMBER
        defaultAppointmentShouldNotBeFound("queueNumber.greaterOrEqualThan=" + UPDATED_QUEUE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByQueueNumberIsLessThanSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where queueNumber less than or equals to DEFAULT_QUEUE_NUMBER
        defaultAppointmentShouldNotBeFound("queueNumber.lessThan=" + DEFAULT_QUEUE_NUMBER);

        // Get all the appointmentList where queueNumber less than or equals to UPDATED_QUEUE_NUMBER
        defaultAppointmentShouldBeFound("queueNumber.lessThan=" + UPDATED_QUEUE_NUMBER);
    }


    @Test
    @Transactional
    public void getAllAppointmentsByVipIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where vip equals to DEFAULT_VIP
        defaultAppointmentShouldBeFound("vip.equals=" + DEFAULT_VIP);

        // Get all the appointmentList where vip equals to UPDATED_VIP
        defaultAppointmentShouldNotBeFound("vip.equals=" + UPDATED_VIP);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByVipIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where vip in DEFAULT_VIP or UPDATED_VIP
        defaultAppointmentShouldBeFound("vip.in=" + DEFAULT_VIP + "," + UPDATED_VIP);

        // Get all the appointmentList where vip equals to UPDATED_VIP
        defaultAppointmentShouldNotBeFound("vip.in=" + UPDATED_VIP);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByVipIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where vip is not null
        defaultAppointmentShouldBeFound("vip.specified=true");

        // Get all the appointmentList where vip is null
        defaultAppointmentShouldNotBeFound("vip.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByCreateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where createTime equals to DEFAULT_CREATE_TIME
        defaultAppointmentShouldBeFound("createTime.equals=" + DEFAULT_CREATE_TIME);

        // Get all the appointmentList where createTime equals to UPDATED_CREATE_TIME
        defaultAppointmentShouldNotBeFound("createTime.equals=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByCreateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where createTime in DEFAULT_CREATE_TIME or UPDATED_CREATE_TIME
        defaultAppointmentShouldBeFound("createTime.in=" + DEFAULT_CREATE_TIME + "," + UPDATED_CREATE_TIME);

        // Get all the appointmentList where createTime equals to UPDATED_CREATE_TIME
        defaultAppointmentShouldNotBeFound("createTime.in=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByCreateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where createTime is not null
        defaultAppointmentShouldBeFound("createTime.specified=true");

        // Get all the appointmentList where createTime is null
        defaultAppointmentShouldNotBeFound("createTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByCreateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where createTime greater than or equals to DEFAULT_CREATE_TIME
        defaultAppointmentShouldBeFound("createTime.greaterOrEqualThan=" + DEFAULT_CREATE_TIME);

        // Get all the appointmentList where createTime greater than or equals to UPDATED_CREATE_TIME
        defaultAppointmentShouldNotBeFound("createTime.greaterOrEqualThan=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByCreateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where createTime less than or equals to DEFAULT_CREATE_TIME
        defaultAppointmentShouldNotBeFound("createTime.lessThan=" + DEFAULT_CREATE_TIME);

        // Get all the appointmentList where createTime less than or equals to UPDATED_CREATE_TIME
        defaultAppointmentShouldBeFound("createTime.lessThan=" + UPDATED_CREATE_TIME);
    }


    @Test
    @Transactional
    public void getAllAppointmentsByUpdateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where updateTime equals to DEFAULT_UPDATE_TIME
        defaultAppointmentShouldBeFound("updateTime.equals=" + DEFAULT_UPDATE_TIME);

        // Get all the appointmentList where updateTime equals to UPDATED_UPDATE_TIME
        defaultAppointmentShouldNotBeFound("updateTime.equals=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByUpdateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where updateTime in DEFAULT_UPDATE_TIME or UPDATED_UPDATE_TIME
        defaultAppointmentShouldBeFound("updateTime.in=" + DEFAULT_UPDATE_TIME + "," + UPDATED_UPDATE_TIME);

        // Get all the appointmentList where updateTime equals to UPDATED_UPDATE_TIME
        defaultAppointmentShouldNotBeFound("updateTime.in=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByUpdateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where updateTime is not null
        defaultAppointmentShouldBeFound("updateTime.specified=true");

        // Get all the appointmentList where updateTime is null
        defaultAppointmentShouldNotBeFound("updateTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByUpdateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where updateTime greater than or equals to DEFAULT_UPDATE_TIME
        defaultAppointmentShouldBeFound("updateTime.greaterOrEqualThan=" + DEFAULT_UPDATE_TIME);

        // Get all the appointmentList where updateTime greater than or equals to UPDATED_UPDATE_TIME
        defaultAppointmentShouldNotBeFound("updateTime.greaterOrEqualThan=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByUpdateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where updateTime less than or equals to DEFAULT_UPDATE_TIME
        defaultAppointmentShouldNotBeFound("updateTime.lessThan=" + DEFAULT_UPDATE_TIME);

        // Get all the appointmentList where updateTime less than or equals to UPDATED_UPDATE_TIME
        defaultAppointmentShouldBeFound("updateTime.lessThan=" + UPDATED_UPDATE_TIME);
    }


    @Test
    @Transactional
    public void getAllAppointmentsByStartTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where startTime equals to DEFAULT_START_TIME
        defaultAppointmentShouldBeFound("startTime.equals=" + DEFAULT_START_TIME);

        // Get all the appointmentList where startTime equals to UPDATED_START_TIME
        defaultAppointmentShouldNotBeFound("startTime.equals=" + UPDATED_START_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByStartTimeIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where startTime in DEFAULT_START_TIME or UPDATED_START_TIME
        defaultAppointmentShouldBeFound("startTime.in=" + DEFAULT_START_TIME + "," + UPDATED_START_TIME);

        // Get all the appointmentList where startTime equals to UPDATED_START_TIME
        defaultAppointmentShouldNotBeFound("startTime.in=" + UPDATED_START_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByStartTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where startTime is not null
        defaultAppointmentShouldBeFound("startTime.specified=true");

        // Get all the appointmentList where startTime is null
        defaultAppointmentShouldNotBeFound("startTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByStartTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where startTime greater than or equals to DEFAULT_START_TIME
        defaultAppointmentShouldBeFound("startTime.greaterOrEqualThan=" + DEFAULT_START_TIME);

        // Get all the appointmentList where startTime greater than or equals to UPDATED_START_TIME
        defaultAppointmentShouldNotBeFound("startTime.greaterOrEqualThan=" + UPDATED_START_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByStartTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where startTime less than or equals to DEFAULT_START_TIME
        defaultAppointmentShouldNotBeFound("startTime.lessThan=" + DEFAULT_START_TIME);

        // Get all the appointmentList where startTime less than or equals to UPDATED_START_TIME
        defaultAppointmentShouldBeFound("startTime.lessThan=" + UPDATED_START_TIME);
    }


    @Test
    @Transactional
    public void getAllAppointmentsByEnterTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where enterTime equals to DEFAULT_ENTER_TIME
        defaultAppointmentShouldBeFound("enterTime.equals=" + DEFAULT_ENTER_TIME);

        // Get all the appointmentList where enterTime equals to UPDATED_ENTER_TIME
        defaultAppointmentShouldNotBeFound("enterTime.equals=" + UPDATED_ENTER_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByEnterTimeIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where enterTime in DEFAULT_ENTER_TIME or UPDATED_ENTER_TIME
        defaultAppointmentShouldBeFound("enterTime.in=" + DEFAULT_ENTER_TIME + "," + UPDATED_ENTER_TIME);

        // Get all the appointmentList where enterTime equals to UPDATED_ENTER_TIME
        defaultAppointmentShouldNotBeFound("enterTime.in=" + UPDATED_ENTER_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByEnterTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where enterTime is not null
        defaultAppointmentShouldBeFound("enterTime.specified=true");

        // Get all the appointmentList where enterTime is null
        defaultAppointmentShouldNotBeFound("enterTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByEnterTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where enterTime greater than or equals to DEFAULT_ENTER_TIME
        defaultAppointmentShouldBeFound("enterTime.greaterOrEqualThan=" + DEFAULT_ENTER_TIME);

        // Get all the appointmentList where enterTime greater than or equals to UPDATED_ENTER_TIME
        defaultAppointmentShouldNotBeFound("enterTime.greaterOrEqualThan=" + UPDATED_ENTER_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByEnterTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where enterTime less than or equals to DEFAULT_ENTER_TIME
        defaultAppointmentShouldNotBeFound("enterTime.lessThan=" + DEFAULT_ENTER_TIME);

        // Get all the appointmentList where enterTime less than or equals to UPDATED_ENTER_TIME
        defaultAppointmentShouldBeFound("enterTime.lessThan=" + UPDATED_ENTER_TIME);
    }


    @Test
    @Transactional
    public void getAllAppointmentsByLeaveTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where leaveTime equals to DEFAULT_LEAVE_TIME
        defaultAppointmentShouldBeFound("leaveTime.equals=" + DEFAULT_LEAVE_TIME);

        // Get all the appointmentList where leaveTime equals to UPDATED_LEAVE_TIME
        defaultAppointmentShouldNotBeFound("leaveTime.equals=" + UPDATED_LEAVE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByLeaveTimeIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where leaveTime in DEFAULT_LEAVE_TIME or UPDATED_LEAVE_TIME
        defaultAppointmentShouldBeFound("leaveTime.in=" + DEFAULT_LEAVE_TIME + "," + UPDATED_LEAVE_TIME);

        // Get all the appointmentList where leaveTime equals to UPDATED_LEAVE_TIME
        defaultAppointmentShouldNotBeFound("leaveTime.in=" + UPDATED_LEAVE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByLeaveTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where leaveTime is not null
        defaultAppointmentShouldBeFound("leaveTime.specified=true");

        // Get all the appointmentList where leaveTime is null
        defaultAppointmentShouldNotBeFound("leaveTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByLeaveTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where leaveTime greater than or equals to DEFAULT_LEAVE_TIME
        defaultAppointmentShouldBeFound("leaveTime.greaterOrEqualThan=" + DEFAULT_LEAVE_TIME);

        // Get all the appointmentList where leaveTime greater than or equals to UPDATED_LEAVE_TIME
        defaultAppointmentShouldNotBeFound("leaveTime.greaterOrEqualThan=" + UPDATED_LEAVE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByLeaveTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where leaveTime less than or equals to DEFAULT_LEAVE_TIME
        defaultAppointmentShouldNotBeFound("leaveTime.lessThan=" + DEFAULT_LEAVE_TIME);

        // Get all the appointmentList where leaveTime less than or equals to UPDATED_LEAVE_TIME
        defaultAppointmentShouldBeFound("leaveTime.lessThan=" + UPDATED_LEAVE_TIME);
    }


    @Test
    @Transactional
    public void getAllAppointmentsByExpireTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where expireTime equals to DEFAULT_EXPIRE_TIME
        defaultAppointmentShouldBeFound("expireTime.equals=" + DEFAULT_EXPIRE_TIME);

        // Get all the appointmentList where expireTime equals to UPDATED_EXPIRE_TIME
        defaultAppointmentShouldNotBeFound("expireTime.equals=" + UPDATED_EXPIRE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByExpireTimeIsInShouldWork() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where expireTime in DEFAULT_EXPIRE_TIME or UPDATED_EXPIRE_TIME
        defaultAppointmentShouldBeFound("expireTime.in=" + DEFAULT_EXPIRE_TIME + "," + UPDATED_EXPIRE_TIME);

        // Get all the appointmentList where expireTime equals to UPDATED_EXPIRE_TIME
        defaultAppointmentShouldNotBeFound("expireTime.in=" + UPDATED_EXPIRE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByExpireTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where expireTime is not null
        defaultAppointmentShouldBeFound("expireTime.specified=true");

        // Get all the appointmentList where expireTime is null
        defaultAppointmentShouldNotBeFound("expireTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAppointmentsByExpireTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where expireTime greater than or equals to DEFAULT_EXPIRE_TIME
        defaultAppointmentShouldBeFound("expireTime.greaterOrEqualThan=" + DEFAULT_EXPIRE_TIME);

        // Get all the appointmentList where expireTime greater than or equals to UPDATED_EXPIRE_TIME
        defaultAppointmentShouldNotBeFound("expireTime.greaterOrEqualThan=" + UPDATED_EXPIRE_TIME);
    }

    @Test
    @Transactional
    public void getAllAppointmentsByExpireTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        // Get all the appointmentList where expireTime less than or equals to DEFAULT_EXPIRE_TIME
        defaultAppointmentShouldNotBeFound("expireTime.lessThan=" + DEFAULT_EXPIRE_TIME);

        // Get all the appointmentList where expireTime less than or equals to UPDATED_EXPIRE_TIME
        defaultAppointmentShouldBeFound("expireTime.lessThan=" + UPDATED_EXPIRE_TIME);
    }


    @Test
    @Transactional
    public void getAllAppointmentsByRegionIsEqualToSomething() throws Exception {
        // Get already existing entity
        Region region = appointment.getRegion();
        appointmentRepository.saveAndFlush(appointment);
        Long regionId = region.getId();

        // Get all the appointmentList where region equals to regionId
        defaultAppointmentShouldBeFound("regionId.equals=" + regionId);

        // Get all the appointmentList where region equals to regionId + 1
        defaultAppointmentShouldNotBeFound("regionId.equals=" + (regionId + 1));
    }


    @Test
    @Transactional
    public void getAllAppointmentsByUserIsEqualToSomething() throws Exception {
        // Get already existing entity
        User user = appointment.getUser();
        appointmentRepository.saveAndFlush(appointment);
        Long userId = user.getId();

        // Get all the appointmentList where user equals to userId
        defaultAppointmentShouldBeFound("userId.equals=" + userId);

        // Get all the appointmentList where user equals to userId + 1
        defaultAppointmentShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAppointmentShouldBeFound(String filter) throws Exception {
        restAppointmentMockMvc.perform(get("/api/appointments?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appointment.getId().intValue())))
            .andExpect(jsonPath("$.[*].licensePlateNumber").value(hasItem(DEFAULT_LICENSE_PLATE_NUMBER)))
            .andExpect(jsonPath("$.[*].driver").value(hasItem(DEFAULT_DRIVER)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].number").value(hasItem(DEFAULT_NUMBER)))
            .andExpect(jsonPath("$.[*].valid").value(hasItem(DEFAULT_VALID.booleanValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].queueNumber").value(hasItem(DEFAULT_QUEUE_NUMBER)))
            .andExpect(jsonPath("$.[*].vip").value(hasItem(DEFAULT_VIP.booleanValue())))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(sameInstant(DEFAULT_START_TIME))))
            .andExpect(jsonPath("$.[*].enterTime").value(hasItem(sameInstant(DEFAULT_ENTER_TIME))))
            .andExpect(jsonPath("$.[*].leaveTime").value(hasItem(sameInstant(DEFAULT_LEAVE_TIME))))
            .andExpect(jsonPath("$.[*].expireTime").value(hasItem(sameInstant(DEFAULT_EXPIRE_TIME))));

        // Check, that the count call also returns 1
        restAppointmentMockMvc.perform(get("/api/appointments/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAppointmentShouldNotBeFound(String filter) throws Exception {
        restAppointmentMockMvc.perform(get("/api/appointments?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAppointmentMockMvc.perform(get("/api/appointments/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingAppointment() throws Exception {
        // Get the appointment
        restAppointmentMockMvc.perform(get("/api/appointments/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAppointment() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        int databaseSizeBeforeUpdate = appointmentRepository.findAll().size();

        // Update the appointment
        Appointment updatedAppointment = appointmentRepository.findById(appointment.getId()).get();
        // Disconnect from session so that the updates on updatedAppointment are not directly saved in db
        em.detach(updatedAppointment);
        updatedAppointment
            .licensePlateNumber(UPDATED_LICENSE_PLATE_NUMBER)
            .driver(UPDATED_DRIVER)
            .phone(UPDATED_PHONE)
            .number(UPDATED_NUMBER)
            .valid(UPDATED_VALID)
            .status(UPDATED_STATUS)
            .queueNumber(UPDATED_QUEUE_NUMBER)
            .vip(UPDATED_VIP)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME)
            .startTime(UPDATED_START_TIME)
            .enterTime(UPDATED_ENTER_TIME)
            .leaveTime(UPDATED_LEAVE_TIME)
            .expireTime(UPDATED_EXPIRE_TIME);
        AppointmentDTO appointmentDTO = appointmentMapper.toDto(updatedAppointment);

        restAppointmentMockMvc.perform(put("/api/appointments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(appointmentDTO)))
            .andExpect(status().isOk());

        // Validate the Appointment in the database
        List<Appointment> appointmentList = appointmentRepository.findAll();
        assertThat(appointmentList).hasSize(databaseSizeBeforeUpdate);
        Appointment testAppointment = appointmentList.get(appointmentList.size() - 1);
        assertThat(testAppointment.getLicensePlateNumber()).isEqualTo(UPDATED_LICENSE_PLATE_NUMBER);
        assertThat(testAppointment.getDriver()).isEqualTo(UPDATED_DRIVER);
        assertThat(testAppointment.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testAppointment.getNumber()).isEqualTo(UPDATED_NUMBER);
        assertThat(testAppointment.isValid()).isEqualTo(UPDATED_VALID);
        assertThat(testAppointment.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testAppointment.getQueueNumber()).isEqualTo(UPDATED_QUEUE_NUMBER);
        assertThat(testAppointment.isVip()).isEqualTo(UPDATED_VIP);
        assertThat(testAppointment.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testAppointment.getUpdateTime()).isEqualTo(UPDATED_UPDATE_TIME);
        assertThat(testAppointment.getStartTime()).isEqualTo(UPDATED_START_TIME);
        assertThat(testAppointment.getEnterTime()).isEqualTo(UPDATED_ENTER_TIME);
        assertThat(testAppointment.getLeaveTime()).isEqualTo(UPDATED_LEAVE_TIME);
        assertThat(testAppointment.getExpireTime()).isEqualTo(UPDATED_EXPIRE_TIME);
    }

    @Test
    @Transactional
    public void updateNonExistingAppointment() throws Exception {
        int databaseSizeBeforeUpdate = appointmentRepository.findAll().size();

        // Create the Appointment
        AppointmentDTO appointmentDTO = appointmentMapper.toDto(appointment);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppointmentMockMvc.perform(put("/api/appointments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(appointmentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Appointment in the database
        List<Appointment> appointmentList = appointmentRepository.findAll();
        assertThat(appointmentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAppointment() throws Exception {
        // Initialize the database
        appointmentRepository.saveAndFlush(appointment);

        int databaseSizeBeforeDelete = appointmentRepository.findAll().size();

        // Delete the appointment
        restAppointmentMockMvc.perform(delete("/api/appointments/{id}", appointment.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<Appointment> appointmentList = appointmentRepository.findAll();
        assertThat(appointmentList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Appointment.class);
        Appointment appointment1 = new Appointment();
        appointment1.setId(1L);
        Appointment appointment2 = new Appointment();
        appointment2.setId(appointment1.getId());
        assertThat(appointment1).isEqualTo(appointment2);
        appointment2.setId(2L);
        assertThat(appointment1).isNotEqualTo(appointment2);
        appointment1.setId(null);
        assertThat(appointment1).isNotEqualTo(appointment2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AppointmentDTO.class);
        AppointmentDTO appointmentDTO1 = new AppointmentDTO();
        appointmentDTO1.setId(1L);
        AppointmentDTO appointmentDTO2 = new AppointmentDTO();
        assertThat(appointmentDTO1).isNotEqualTo(appointmentDTO2);
        appointmentDTO2.setId(appointmentDTO1.getId());
        assertThat(appointmentDTO1).isEqualTo(appointmentDTO2);
        appointmentDTO2.setId(2L);
        assertThat(appointmentDTO1).isNotEqualTo(appointmentDTO2);
        appointmentDTO1.setId(null);
        assertThat(appointmentDTO1).isNotEqualTo(appointmentDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(appointmentMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(appointmentMapper.fromId(null)).isNull();
    }
}
