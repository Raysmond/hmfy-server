package com.shield.web.rest;

import com.shield.ShieldApp;
import com.shield.domain.ShipPlan;
import com.shield.domain.User;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.ShipPlanService;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.mapper.ShipPlanMapper;
import com.shield.web.rest.errors.ExceptionTranslator;
import com.shield.service.dto.ShipPlanCriteria;
import com.shield.service.ShipPlanQueryService;

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

/**
 * Integration tests for the {@Link ShipPlanResource} REST controller.
 */
@SpringBootTest(classes = ShieldApp.class)
public class ShipPlanResourceIT {

    private static final String DEFAULT_COMPANY = "AAAAAAAAAA";
    private static final String UPDATED_COMPANY = "BBBBBBBBBB";

    private static final Long DEFAULT_APPLY_ID = 1L;
    private static final Long UPDATED_APPLY_ID = 2L;

    private static final String DEFAULT_APPLY_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_APPLY_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_TRUCK_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_TRUCK_NUMBER = "BBBBBBBBBB";

    private static final Integer DEFAULT_AUDIT_STATUS = 1;
    private static final Integer UPDATED_AUDIT_STATUS = 2;

    private static final String DEFAULT_PRODUCT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_PRODUCT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DELIVER_POSITION = "AAAAAAAAAA";
    private static final String UPDATED_DELIVER_POSITION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_VALID = false;
    private static final Boolean UPDATED_VALID = true;

    private static final ZonedDateTime DEFAULT_GATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_GATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_LEAVE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_LEAVE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_DELIVER_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_DELIVER_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_ALLOW_IN_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_ALLOW_IN_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_LOADING_START_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_LOADING_START_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_LOADING_END_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_LOADING_END_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_SYNC_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_SYNC_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Boolean DEFAULT_TARE_ALERT = false;
    private static final Boolean UPDATED_TARE_ALERT = true;

    private static final Boolean DEFAULT_LEAVE_ALERT = false;
    private static final Boolean UPDATED_LEAVE_ALERT = true;

    private static final Double DEFAULT_NET_WEIGHT = 1D;
    private static final Double UPDATED_NET_WEIGHT = 2D;

    private static final String DEFAULT_WEIGHER_NO = "AAAAAAAAAA";
    private static final String UPDATED_WEIGHER_NO = "BBBBBBBBBB";

    private static final Boolean DEFAULT_VIP = false;
    private static final Boolean UPDATED_VIP = true;

    @Autowired
    private ShipPlanRepository shipPlanRepository;

    @Autowired
    private ShipPlanMapper shipPlanMapper;

    @Autowired
    private ShipPlanService shipPlanService;

    @Autowired
    private ShipPlanQueryService shipPlanQueryService;

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

    private MockMvc restShipPlanMockMvc;

    private ShipPlan shipPlan;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ShipPlanResource shipPlanResource = new ShipPlanResource(shipPlanService, shipPlanQueryService);
        this.restShipPlanMockMvc = MockMvcBuilders.standaloneSetup(shipPlanResource)
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
    public static ShipPlan createEntity(EntityManager em) {
        ShipPlan shipPlan = new ShipPlan()
            .company(DEFAULT_COMPANY)
            .applyId(DEFAULT_APPLY_ID)
            .applyNumber(DEFAULT_APPLY_NUMBER)
            .truckNumber(DEFAULT_TRUCK_NUMBER)
            .auditStatus(DEFAULT_AUDIT_STATUS)
            .productName(DEFAULT_PRODUCT_NAME)
            .deliverPosition(DEFAULT_DELIVER_POSITION)
            .valid(DEFAULT_VALID)
            .gateTime(DEFAULT_GATE_TIME)
            .leaveTime(DEFAULT_LEAVE_TIME)
            .deliverTime(DEFAULT_DELIVER_TIME)
            .allowInTime(DEFAULT_ALLOW_IN_TIME)
            .loadingStartTime(DEFAULT_LOADING_START_TIME)
            .loadingEndTime(DEFAULT_LOADING_END_TIME)
            .createTime(DEFAULT_CREATE_TIME)
            .updateTime(DEFAULT_UPDATE_TIME)
            .syncTime(DEFAULT_SYNC_TIME)
            .tareAlert(DEFAULT_TARE_ALERT)
            .leaveAlert(DEFAULT_LEAVE_ALERT)
            .netWeight(DEFAULT_NET_WEIGHT)
            .weigherNo(DEFAULT_WEIGHER_NO)
            .vip(DEFAULT_VIP);
        return shipPlan;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ShipPlan createUpdatedEntity(EntityManager em) {
        ShipPlan shipPlan = new ShipPlan()
            .company(UPDATED_COMPANY)
            .applyId(UPDATED_APPLY_ID)
            .applyNumber(UPDATED_APPLY_NUMBER)
            .truckNumber(UPDATED_TRUCK_NUMBER)
            .auditStatus(UPDATED_AUDIT_STATUS)
            .productName(UPDATED_PRODUCT_NAME)
            .deliverPosition(UPDATED_DELIVER_POSITION)
            .valid(UPDATED_VALID)
            .gateTime(UPDATED_GATE_TIME)
            .leaveTime(UPDATED_LEAVE_TIME)
            .deliverTime(UPDATED_DELIVER_TIME)
            .allowInTime(UPDATED_ALLOW_IN_TIME)
            .loadingStartTime(UPDATED_LOADING_START_TIME)
            .loadingEndTime(UPDATED_LOADING_END_TIME)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME)
            .syncTime(UPDATED_SYNC_TIME)
            .tareAlert(UPDATED_TARE_ALERT)
            .leaveAlert(UPDATED_LEAVE_ALERT)
            .netWeight(UPDATED_NET_WEIGHT)
            .weigherNo(UPDATED_WEIGHER_NO)
            .vip(UPDATED_VIP);
        return shipPlan;
    }

    @BeforeEach
    public void initTest() {
        shipPlan = createEntity(em);
    }

    @Test
    @Transactional
    public void createShipPlan() throws Exception {
        int databaseSizeBeforeCreate = shipPlanRepository.findAll().size();

        // Create the ShipPlan
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);
        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isCreated());

        // Validate the ShipPlan in the database
        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeCreate + 1);
        ShipPlan testShipPlan = shipPlanList.get(shipPlanList.size() - 1);
        assertThat(testShipPlan.getCompany()).isEqualTo(DEFAULT_COMPANY);
        assertThat(testShipPlan.getApplyId()).isEqualTo(DEFAULT_APPLY_ID);
        assertThat(testShipPlan.getApplyNumber()).isEqualTo(DEFAULT_APPLY_NUMBER);
        assertThat(testShipPlan.getTruckNumber()).isEqualTo(DEFAULT_TRUCK_NUMBER);
        assertThat(testShipPlan.getAuditStatus()).isEqualTo(DEFAULT_AUDIT_STATUS);
        assertThat(testShipPlan.getProductName()).isEqualTo(DEFAULT_PRODUCT_NAME);
        assertThat(testShipPlan.getDeliverPosition()).isEqualTo(DEFAULT_DELIVER_POSITION);
        assertThat(testShipPlan.isValid()).isEqualTo(DEFAULT_VALID);
        assertThat(testShipPlan.getGateTime()).isEqualTo(DEFAULT_GATE_TIME);
        assertThat(testShipPlan.getLeaveTime()).isEqualTo(DEFAULT_LEAVE_TIME);
        assertThat(testShipPlan.getDeliverTime()).isEqualTo(DEFAULT_DELIVER_TIME);
        assertThat(testShipPlan.getAllowInTime()).isEqualTo(DEFAULT_ALLOW_IN_TIME);
        assertThat(testShipPlan.getLoadingStartTime()).isEqualTo(DEFAULT_LOADING_START_TIME);
        assertThat(testShipPlan.getLoadingEndTime()).isEqualTo(DEFAULT_LOADING_END_TIME);
        assertThat(testShipPlan.getCreateTime()).isEqualTo(DEFAULT_CREATE_TIME);
        assertThat(testShipPlan.getUpdateTime()).isEqualTo(DEFAULT_UPDATE_TIME);
        assertThat(testShipPlan.getSyncTime()).isEqualTo(DEFAULT_SYNC_TIME);
        assertThat(testShipPlan.isTareAlert()).isEqualTo(DEFAULT_TARE_ALERT);
        assertThat(testShipPlan.isLeaveAlert()).isEqualTo(DEFAULT_LEAVE_ALERT);
        assertThat(testShipPlan.getNetWeight()).isEqualTo(DEFAULT_NET_WEIGHT);
        assertThat(testShipPlan.getWeigherNo()).isEqualTo(DEFAULT_WEIGHER_NO);
        assertThat(testShipPlan.isVip()).isEqualTo(DEFAULT_VIP);
    }

    @Test
    @Transactional
    public void createShipPlanWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = shipPlanRepository.findAll().size();

        // Create the ShipPlan with an existing ID
        shipPlan.setId(1L);
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        // An entity with an existing ID cannot be created, so this API call must fail
        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ShipPlan in the database
        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkApplyIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setApplyId(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTruckNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setTruckNumber(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAuditStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setAuditStatus(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkProductNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setProductName(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDeliverPositionIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setDeliverPosition(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValidIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setValid(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCreateTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setCreateTime(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUpdateTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setUpdateTime(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTareAlertIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setTareAlert(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLeaveAlertIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setLeaveAlert(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVipIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setVip(null);

        // Create the ShipPlan, which fails.
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        restShipPlanMockMvc.perform(post("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllShipPlans() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList
        restShipPlanMockMvc.perform(get("/api/ship-plans?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(shipPlan.getId().intValue())))
            .andExpect(jsonPath("$.[*].company").value(hasItem(DEFAULT_COMPANY.toString())))
            .andExpect(jsonPath("$.[*].applyId").value(hasItem(DEFAULT_APPLY_ID.intValue())))
            .andExpect(jsonPath("$.[*].applyNumber").value(hasItem(DEFAULT_APPLY_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].truckNumber").value(hasItem(DEFAULT_TRUCK_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].auditStatus").value(hasItem(DEFAULT_AUDIT_STATUS)))
            .andExpect(jsonPath("$.[*].productName").value(hasItem(DEFAULT_PRODUCT_NAME.toString())))
            .andExpect(jsonPath("$.[*].deliverPosition").value(hasItem(DEFAULT_DELIVER_POSITION.toString())))
            .andExpect(jsonPath("$.[*].valid").value(hasItem(DEFAULT_VALID.booleanValue())))
            .andExpect(jsonPath("$.[*].gateTime").value(hasItem(sameInstant(DEFAULT_GATE_TIME))))
            .andExpect(jsonPath("$.[*].leaveTime").value(hasItem(sameInstant(DEFAULT_LEAVE_TIME))))
            .andExpect(jsonPath("$.[*].deliverTime").value(hasItem(sameInstant(DEFAULT_DELIVER_TIME))))
            .andExpect(jsonPath("$.[*].allowInTime").value(hasItem(sameInstant(DEFAULT_ALLOW_IN_TIME))))
            .andExpect(jsonPath("$.[*].loadingStartTime").value(hasItem(sameInstant(DEFAULT_LOADING_START_TIME))))
            .andExpect(jsonPath("$.[*].loadingEndTime").value(hasItem(sameInstant(DEFAULT_LOADING_END_TIME))))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))))
            .andExpect(jsonPath("$.[*].syncTime").value(hasItem(sameInstant(DEFAULT_SYNC_TIME))))
            .andExpect(jsonPath("$.[*].tareAlert").value(hasItem(DEFAULT_TARE_ALERT.booleanValue())))
            .andExpect(jsonPath("$.[*].leaveAlert").value(hasItem(DEFAULT_LEAVE_ALERT.booleanValue())))
            .andExpect(jsonPath("$.[*].netWeight").value(hasItem(DEFAULT_NET_WEIGHT.doubleValue())))
            .andExpect(jsonPath("$.[*].weigherNo").value(hasItem(DEFAULT_WEIGHER_NO.toString())))
            .andExpect(jsonPath("$.[*].vip").value(hasItem(DEFAULT_VIP.booleanValue())));
    }
    
    @Test
    @Transactional
    public void getShipPlan() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get the shipPlan
        restShipPlanMockMvc.perform(get("/api/ship-plans/{id}", shipPlan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(shipPlan.getId().intValue()))
            .andExpect(jsonPath("$.company").value(DEFAULT_COMPANY.toString()))
            .andExpect(jsonPath("$.applyId").value(DEFAULT_APPLY_ID.intValue()))
            .andExpect(jsonPath("$.applyNumber").value(DEFAULT_APPLY_NUMBER.toString()))
            .andExpect(jsonPath("$.truckNumber").value(DEFAULT_TRUCK_NUMBER.toString()))
            .andExpect(jsonPath("$.auditStatus").value(DEFAULT_AUDIT_STATUS))
            .andExpect(jsonPath("$.productName").value(DEFAULT_PRODUCT_NAME.toString()))
            .andExpect(jsonPath("$.deliverPosition").value(DEFAULT_DELIVER_POSITION.toString()))
            .andExpect(jsonPath("$.valid").value(DEFAULT_VALID.booleanValue()))
            .andExpect(jsonPath("$.gateTime").value(sameInstant(DEFAULT_GATE_TIME)))
            .andExpect(jsonPath("$.leaveTime").value(sameInstant(DEFAULT_LEAVE_TIME)))
            .andExpect(jsonPath("$.deliverTime").value(sameInstant(DEFAULT_DELIVER_TIME)))
            .andExpect(jsonPath("$.allowInTime").value(sameInstant(DEFAULT_ALLOW_IN_TIME)))
            .andExpect(jsonPath("$.loadingStartTime").value(sameInstant(DEFAULT_LOADING_START_TIME)))
            .andExpect(jsonPath("$.loadingEndTime").value(sameInstant(DEFAULT_LOADING_END_TIME)))
            .andExpect(jsonPath("$.createTime").value(sameInstant(DEFAULT_CREATE_TIME)))
            .andExpect(jsonPath("$.updateTime").value(sameInstant(DEFAULT_UPDATE_TIME)))
            .andExpect(jsonPath("$.syncTime").value(sameInstant(DEFAULT_SYNC_TIME)))
            .andExpect(jsonPath("$.tareAlert").value(DEFAULT_TARE_ALERT.booleanValue()))
            .andExpect(jsonPath("$.leaveAlert").value(DEFAULT_LEAVE_ALERT.booleanValue()))
            .andExpect(jsonPath("$.netWeight").value(DEFAULT_NET_WEIGHT.doubleValue()))
            .andExpect(jsonPath("$.weigherNo").value(DEFAULT_WEIGHER_NO.toString()))
            .andExpect(jsonPath("$.vip").value(DEFAULT_VIP.booleanValue()));
    }

    @Test
    @Transactional
    public void getAllShipPlansByCompanyIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where company equals to DEFAULT_COMPANY
        defaultShipPlanShouldBeFound("company.equals=" + DEFAULT_COMPANY);

        // Get all the shipPlanList where company equals to UPDATED_COMPANY
        defaultShipPlanShouldNotBeFound("company.equals=" + UPDATED_COMPANY);
    }

    @Test
    @Transactional
    public void getAllShipPlansByCompanyIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where company in DEFAULT_COMPANY or UPDATED_COMPANY
        defaultShipPlanShouldBeFound("company.in=" + DEFAULT_COMPANY + "," + UPDATED_COMPANY);

        // Get all the shipPlanList where company equals to UPDATED_COMPANY
        defaultShipPlanShouldNotBeFound("company.in=" + UPDATED_COMPANY);
    }

    @Test
    @Transactional
    public void getAllShipPlansByCompanyIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where company is not null
        defaultShipPlanShouldBeFound("company.specified=true");

        // Get all the shipPlanList where company is null
        defaultShipPlanShouldNotBeFound("company.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByApplyIdIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where applyId equals to DEFAULT_APPLY_ID
        defaultShipPlanShouldBeFound("applyId.equals=" + DEFAULT_APPLY_ID);

        // Get all the shipPlanList where applyId equals to UPDATED_APPLY_ID
        defaultShipPlanShouldNotBeFound("applyId.equals=" + UPDATED_APPLY_ID);
    }

    @Test
    @Transactional
    public void getAllShipPlansByApplyIdIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where applyId in DEFAULT_APPLY_ID or UPDATED_APPLY_ID
        defaultShipPlanShouldBeFound("applyId.in=" + DEFAULT_APPLY_ID + "," + UPDATED_APPLY_ID);

        // Get all the shipPlanList where applyId equals to UPDATED_APPLY_ID
        defaultShipPlanShouldNotBeFound("applyId.in=" + UPDATED_APPLY_ID);
    }

    @Test
    @Transactional
    public void getAllShipPlansByApplyIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where applyId is not null
        defaultShipPlanShouldBeFound("applyId.specified=true");

        // Get all the shipPlanList where applyId is null
        defaultShipPlanShouldNotBeFound("applyId.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByApplyIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where applyId greater than or equals to DEFAULT_APPLY_ID
        defaultShipPlanShouldBeFound("applyId.greaterOrEqualThan=" + DEFAULT_APPLY_ID);

        // Get all the shipPlanList where applyId greater than or equals to UPDATED_APPLY_ID
        defaultShipPlanShouldNotBeFound("applyId.greaterOrEqualThan=" + UPDATED_APPLY_ID);
    }

    @Test
    @Transactional
    public void getAllShipPlansByApplyIdIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where applyId less than or equals to DEFAULT_APPLY_ID
        defaultShipPlanShouldNotBeFound("applyId.lessThan=" + DEFAULT_APPLY_ID);

        // Get all the shipPlanList where applyId less than or equals to UPDATED_APPLY_ID
        defaultShipPlanShouldBeFound("applyId.lessThan=" + UPDATED_APPLY_ID);
    }


    @Test
    @Transactional
    public void getAllShipPlansByApplyNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where applyNumber equals to DEFAULT_APPLY_NUMBER
        defaultShipPlanShouldBeFound("applyNumber.equals=" + DEFAULT_APPLY_NUMBER);

        // Get all the shipPlanList where applyNumber equals to UPDATED_APPLY_NUMBER
        defaultShipPlanShouldNotBeFound("applyNumber.equals=" + UPDATED_APPLY_NUMBER);
    }

    @Test
    @Transactional
    public void getAllShipPlansByApplyNumberIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where applyNumber in DEFAULT_APPLY_NUMBER or UPDATED_APPLY_NUMBER
        defaultShipPlanShouldBeFound("applyNumber.in=" + DEFAULT_APPLY_NUMBER + "," + UPDATED_APPLY_NUMBER);

        // Get all the shipPlanList where applyNumber equals to UPDATED_APPLY_NUMBER
        defaultShipPlanShouldNotBeFound("applyNumber.in=" + UPDATED_APPLY_NUMBER);
    }

    @Test
    @Transactional
    public void getAllShipPlansByApplyNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where applyNumber is not null
        defaultShipPlanShouldBeFound("applyNumber.specified=true");

        // Get all the shipPlanList where applyNumber is null
        defaultShipPlanShouldNotBeFound("applyNumber.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByTruckNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where truckNumber equals to DEFAULT_TRUCK_NUMBER
        defaultShipPlanShouldBeFound("truckNumber.equals=" + DEFAULT_TRUCK_NUMBER);

        // Get all the shipPlanList where truckNumber equals to UPDATED_TRUCK_NUMBER
        defaultShipPlanShouldNotBeFound("truckNumber.equals=" + UPDATED_TRUCK_NUMBER);
    }

    @Test
    @Transactional
    public void getAllShipPlansByTruckNumberIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where truckNumber in DEFAULT_TRUCK_NUMBER or UPDATED_TRUCK_NUMBER
        defaultShipPlanShouldBeFound("truckNumber.in=" + DEFAULT_TRUCK_NUMBER + "," + UPDATED_TRUCK_NUMBER);

        // Get all the shipPlanList where truckNumber equals to UPDATED_TRUCK_NUMBER
        defaultShipPlanShouldNotBeFound("truckNumber.in=" + UPDATED_TRUCK_NUMBER);
    }

    @Test
    @Transactional
    public void getAllShipPlansByTruckNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where truckNumber is not null
        defaultShipPlanShouldBeFound("truckNumber.specified=true");

        // Get all the shipPlanList where truckNumber is null
        defaultShipPlanShouldNotBeFound("truckNumber.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByAuditStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where auditStatus equals to DEFAULT_AUDIT_STATUS
        defaultShipPlanShouldBeFound("auditStatus.equals=" + DEFAULT_AUDIT_STATUS);

        // Get all the shipPlanList where auditStatus equals to UPDATED_AUDIT_STATUS
        defaultShipPlanShouldNotBeFound("auditStatus.equals=" + UPDATED_AUDIT_STATUS);
    }

    @Test
    @Transactional
    public void getAllShipPlansByAuditStatusIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where auditStatus in DEFAULT_AUDIT_STATUS or UPDATED_AUDIT_STATUS
        defaultShipPlanShouldBeFound("auditStatus.in=" + DEFAULT_AUDIT_STATUS + "," + UPDATED_AUDIT_STATUS);

        // Get all the shipPlanList where auditStatus equals to UPDATED_AUDIT_STATUS
        defaultShipPlanShouldNotBeFound("auditStatus.in=" + UPDATED_AUDIT_STATUS);
    }

    @Test
    @Transactional
    public void getAllShipPlansByAuditStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where auditStatus is not null
        defaultShipPlanShouldBeFound("auditStatus.specified=true");

        // Get all the shipPlanList where auditStatus is null
        defaultShipPlanShouldNotBeFound("auditStatus.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByAuditStatusIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where auditStatus greater than or equals to DEFAULT_AUDIT_STATUS
        defaultShipPlanShouldBeFound("auditStatus.greaterOrEqualThan=" + DEFAULT_AUDIT_STATUS);

        // Get all the shipPlanList where auditStatus greater than or equals to UPDATED_AUDIT_STATUS
        defaultShipPlanShouldNotBeFound("auditStatus.greaterOrEqualThan=" + UPDATED_AUDIT_STATUS);
    }

    @Test
    @Transactional
    public void getAllShipPlansByAuditStatusIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where auditStatus less than or equals to DEFAULT_AUDIT_STATUS
        defaultShipPlanShouldNotBeFound("auditStatus.lessThan=" + DEFAULT_AUDIT_STATUS);

        // Get all the shipPlanList where auditStatus less than or equals to UPDATED_AUDIT_STATUS
        defaultShipPlanShouldBeFound("auditStatus.lessThan=" + UPDATED_AUDIT_STATUS);
    }


    @Test
    @Transactional
    public void getAllShipPlansByProductNameIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where productName equals to DEFAULT_PRODUCT_NAME
        defaultShipPlanShouldBeFound("productName.equals=" + DEFAULT_PRODUCT_NAME);

        // Get all the shipPlanList where productName equals to UPDATED_PRODUCT_NAME
        defaultShipPlanShouldNotBeFound("productName.equals=" + UPDATED_PRODUCT_NAME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByProductNameIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where productName in DEFAULT_PRODUCT_NAME or UPDATED_PRODUCT_NAME
        defaultShipPlanShouldBeFound("productName.in=" + DEFAULT_PRODUCT_NAME + "," + UPDATED_PRODUCT_NAME);

        // Get all the shipPlanList where productName equals to UPDATED_PRODUCT_NAME
        defaultShipPlanShouldNotBeFound("productName.in=" + UPDATED_PRODUCT_NAME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByProductNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where productName is not null
        defaultShipPlanShouldBeFound("productName.specified=true");

        // Get all the shipPlanList where productName is null
        defaultShipPlanShouldNotBeFound("productName.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByDeliverPositionIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where deliverPosition equals to DEFAULT_DELIVER_POSITION
        defaultShipPlanShouldBeFound("deliverPosition.equals=" + DEFAULT_DELIVER_POSITION);

        // Get all the shipPlanList where deliverPosition equals to UPDATED_DELIVER_POSITION
        defaultShipPlanShouldNotBeFound("deliverPosition.equals=" + UPDATED_DELIVER_POSITION);
    }

    @Test
    @Transactional
    public void getAllShipPlansByDeliverPositionIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where deliverPosition in DEFAULT_DELIVER_POSITION or UPDATED_DELIVER_POSITION
        defaultShipPlanShouldBeFound("deliverPosition.in=" + DEFAULT_DELIVER_POSITION + "," + UPDATED_DELIVER_POSITION);

        // Get all the shipPlanList where deliverPosition equals to UPDATED_DELIVER_POSITION
        defaultShipPlanShouldNotBeFound("deliverPosition.in=" + UPDATED_DELIVER_POSITION);
    }

    @Test
    @Transactional
    public void getAllShipPlansByDeliverPositionIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where deliverPosition is not null
        defaultShipPlanShouldBeFound("deliverPosition.specified=true");

        // Get all the shipPlanList where deliverPosition is null
        defaultShipPlanShouldNotBeFound("deliverPosition.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByValidIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where valid equals to DEFAULT_VALID
        defaultShipPlanShouldBeFound("valid.equals=" + DEFAULT_VALID);

        // Get all the shipPlanList where valid equals to UPDATED_VALID
        defaultShipPlanShouldNotBeFound("valid.equals=" + UPDATED_VALID);
    }

    @Test
    @Transactional
    public void getAllShipPlansByValidIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where valid in DEFAULT_VALID or UPDATED_VALID
        defaultShipPlanShouldBeFound("valid.in=" + DEFAULT_VALID + "," + UPDATED_VALID);

        // Get all the shipPlanList where valid equals to UPDATED_VALID
        defaultShipPlanShouldNotBeFound("valid.in=" + UPDATED_VALID);
    }

    @Test
    @Transactional
    public void getAllShipPlansByValidIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where valid is not null
        defaultShipPlanShouldBeFound("valid.specified=true");

        // Get all the shipPlanList where valid is null
        defaultShipPlanShouldNotBeFound("valid.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByGateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where gateTime equals to DEFAULT_GATE_TIME
        defaultShipPlanShouldBeFound("gateTime.equals=" + DEFAULT_GATE_TIME);

        // Get all the shipPlanList where gateTime equals to UPDATED_GATE_TIME
        defaultShipPlanShouldNotBeFound("gateTime.equals=" + UPDATED_GATE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByGateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where gateTime in DEFAULT_GATE_TIME or UPDATED_GATE_TIME
        defaultShipPlanShouldBeFound("gateTime.in=" + DEFAULT_GATE_TIME + "," + UPDATED_GATE_TIME);

        // Get all the shipPlanList where gateTime equals to UPDATED_GATE_TIME
        defaultShipPlanShouldNotBeFound("gateTime.in=" + UPDATED_GATE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByGateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where gateTime is not null
        defaultShipPlanShouldBeFound("gateTime.specified=true");

        // Get all the shipPlanList where gateTime is null
        defaultShipPlanShouldNotBeFound("gateTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByGateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where gateTime greater than or equals to DEFAULT_GATE_TIME
        defaultShipPlanShouldBeFound("gateTime.greaterOrEqualThan=" + DEFAULT_GATE_TIME);

        // Get all the shipPlanList where gateTime greater than or equals to UPDATED_GATE_TIME
        defaultShipPlanShouldNotBeFound("gateTime.greaterOrEqualThan=" + UPDATED_GATE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByGateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where gateTime less than or equals to DEFAULT_GATE_TIME
        defaultShipPlanShouldNotBeFound("gateTime.lessThan=" + DEFAULT_GATE_TIME);

        // Get all the shipPlanList where gateTime less than or equals to UPDATED_GATE_TIME
        defaultShipPlanShouldBeFound("gateTime.lessThan=" + UPDATED_GATE_TIME);
    }


    @Test
    @Transactional
    public void getAllShipPlansByLeaveTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where leaveTime equals to DEFAULT_LEAVE_TIME
        defaultShipPlanShouldBeFound("leaveTime.equals=" + DEFAULT_LEAVE_TIME);

        // Get all the shipPlanList where leaveTime equals to UPDATED_LEAVE_TIME
        defaultShipPlanShouldNotBeFound("leaveTime.equals=" + UPDATED_LEAVE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLeaveTimeIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where leaveTime in DEFAULT_LEAVE_TIME or UPDATED_LEAVE_TIME
        defaultShipPlanShouldBeFound("leaveTime.in=" + DEFAULT_LEAVE_TIME + "," + UPDATED_LEAVE_TIME);

        // Get all the shipPlanList where leaveTime equals to UPDATED_LEAVE_TIME
        defaultShipPlanShouldNotBeFound("leaveTime.in=" + UPDATED_LEAVE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLeaveTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where leaveTime is not null
        defaultShipPlanShouldBeFound("leaveTime.specified=true");

        // Get all the shipPlanList where leaveTime is null
        defaultShipPlanShouldNotBeFound("leaveTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByLeaveTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where leaveTime greater than or equals to DEFAULT_LEAVE_TIME
        defaultShipPlanShouldBeFound("leaveTime.greaterOrEqualThan=" + DEFAULT_LEAVE_TIME);

        // Get all the shipPlanList where leaveTime greater than or equals to UPDATED_LEAVE_TIME
        defaultShipPlanShouldNotBeFound("leaveTime.greaterOrEqualThan=" + UPDATED_LEAVE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLeaveTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where leaveTime less than or equals to DEFAULT_LEAVE_TIME
        defaultShipPlanShouldNotBeFound("leaveTime.lessThan=" + DEFAULT_LEAVE_TIME);

        // Get all the shipPlanList where leaveTime less than or equals to UPDATED_LEAVE_TIME
        defaultShipPlanShouldBeFound("leaveTime.lessThan=" + UPDATED_LEAVE_TIME);
    }


    @Test
    @Transactional
    public void getAllShipPlansByDeliverTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where deliverTime equals to DEFAULT_DELIVER_TIME
        defaultShipPlanShouldBeFound("deliverTime.equals=" + DEFAULT_DELIVER_TIME);

        // Get all the shipPlanList where deliverTime equals to UPDATED_DELIVER_TIME
        defaultShipPlanShouldNotBeFound("deliverTime.equals=" + UPDATED_DELIVER_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByDeliverTimeIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where deliverTime in DEFAULT_DELIVER_TIME or UPDATED_DELIVER_TIME
        defaultShipPlanShouldBeFound("deliverTime.in=" + DEFAULT_DELIVER_TIME + "," + UPDATED_DELIVER_TIME);

        // Get all the shipPlanList where deliverTime equals to UPDATED_DELIVER_TIME
        defaultShipPlanShouldNotBeFound("deliverTime.in=" + UPDATED_DELIVER_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByDeliverTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where deliverTime is not null
        defaultShipPlanShouldBeFound("deliverTime.specified=true");

        // Get all the shipPlanList where deliverTime is null
        defaultShipPlanShouldNotBeFound("deliverTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByDeliverTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where deliverTime greater than or equals to DEFAULT_DELIVER_TIME
        defaultShipPlanShouldBeFound("deliverTime.greaterOrEqualThan=" + DEFAULT_DELIVER_TIME);

        // Get all the shipPlanList where deliverTime greater than or equals to UPDATED_DELIVER_TIME
        defaultShipPlanShouldNotBeFound("deliverTime.greaterOrEqualThan=" + UPDATED_DELIVER_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByDeliverTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where deliverTime less than or equals to DEFAULT_DELIVER_TIME
        defaultShipPlanShouldNotBeFound("deliverTime.lessThan=" + DEFAULT_DELIVER_TIME);

        // Get all the shipPlanList where deliverTime less than or equals to UPDATED_DELIVER_TIME
        defaultShipPlanShouldBeFound("deliverTime.lessThan=" + UPDATED_DELIVER_TIME);
    }


    @Test
    @Transactional
    public void getAllShipPlansByAllowInTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where allowInTime equals to DEFAULT_ALLOW_IN_TIME
        defaultShipPlanShouldBeFound("allowInTime.equals=" + DEFAULT_ALLOW_IN_TIME);

        // Get all the shipPlanList where allowInTime equals to UPDATED_ALLOW_IN_TIME
        defaultShipPlanShouldNotBeFound("allowInTime.equals=" + UPDATED_ALLOW_IN_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByAllowInTimeIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where allowInTime in DEFAULT_ALLOW_IN_TIME or UPDATED_ALLOW_IN_TIME
        defaultShipPlanShouldBeFound("allowInTime.in=" + DEFAULT_ALLOW_IN_TIME + "," + UPDATED_ALLOW_IN_TIME);

        // Get all the shipPlanList where allowInTime equals to UPDATED_ALLOW_IN_TIME
        defaultShipPlanShouldNotBeFound("allowInTime.in=" + UPDATED_ALLOW_IN_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByAllowInTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where allowInTime is not null
        defaultShipPlanShouldBeFound("allowInTime.specified=true");

        // Get all the shipPlanList where allowInTime is null
        defaultShipPlanShouldNotBeFound("allowInTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByAllowInTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where allowInTime greater than or equals to DEFAULT_ALLOW_IN_TIME
        defaultShipPlanShouldBeFound("allowInTime.greaterOrEqualThan=" + DEFAULT_ALLOW_IN_TIME);

        // Get all the shipPlanList where allowInTime greater than or equals to UPDATED_ALLOW_IN_TIME
        defaultShipPlanShouldNotBeFound("allowInTime.greaterOrEqualThan=" + UPDATED_ALLOW_IN_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByAllowInTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where allowInTime less than or equals to DEFAULT_ALLOW_IN_TIME
        defaultShipPlanShouldNotBeFound("allowInTime.lessThan=" + DEFAULT_ALLOW_IN_TIME);

        // Get all the shipPlanList where allowInTime less than or equals to UPDATED_ALLOW_IN_TIME
        defaultShipPlanShouldBeFound("allowInTime.lessThan=" + UPDATED_ALLOW_IN_TIME);
    }


    @Test
    @Transactional
    public void getAllShipPlansByLoadingStartTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where loadingStartTime equals to DEFAULT_LOADING_START_TIME
        defaultShipPlanShouldBeFound("loadingStartTime.equals=" + DEFAULT_LOADING_START_TIME);

        // Get all the shipPlanList where loadingStartTime equals to UPDATED_LOADING_START_TIME
        defaultShipPlanShouldNotBeFound("loadingStartTime.equals=" + UPDATED_LOADING_START_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLoadingStartTimeIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where loadingStartTime in DEFAULT_LOADING_START_TIME or UPDATED_LOADING_START_TIME
        defaultShipPlanShouldBeFound("loadingStartTime.in=" + DEFAULT_LOADING_START_TIME + "," + UPDATED_LOADING_START_TIME);

        // Get all the shipPlanList where loadingStartTime equals to UPDATED_LOADING_START_TIME
        defaultShipPlanShouldNotBeFound("loadingStartTime.in=" + UPDATED_LOADING_START_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLoadingStartTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where loadingStartTime is not null
        defaultShipPlanShouldBeFound("loadingStartTime.specified=true");

        // Get all the shipPlanList where loadingStartTime is null
        defaultShipPlanShouldNotBeFound("loadingStartTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByLoadingStartTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where loadingStartTime greater than or equals to DEFAULT_LOADING_START_TIME
        defaultShipPlanShouldBeFound("loadingStartTime.greaterOrEqualThan=" + DEFAULT_LOADING_START_TIME);

        // Get all the shipPlanList where loadingStartTime greater than or equals to UPDATED_LOADING_START_TIME
        defaultShipPlanShouldNotBeFound("loadingStartTime.greaterOrEqualThan=" + UPDATED_LOADING_START_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLoadingStartTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where loadingStartTime less than or equals to DEFAULT_LOADING_START_TIME
        defaultShipPlanShouldNotBeFound("loadingStartTime.lessThan=" + DEFAULT_LOADING_START_TIME);

        // Get all the shipPlanList where loadingStartTime less than or equals to UPDATED_LOADING_START_TIME
        defaultShipPlanShouldBeFound("loadingStartTime.lessThan=" + UPDATED_LOADING_START_TIME);
    }


    @Test
    @Transactional
    public void getAllShipPlansByLoadingEndTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where loadingEndTime equals to DEFAULT_LOADING_END_TIME
        defaultShipPlanShouldBeFound("loadingEndTime.equals=" + DEFAULT_LOADING_END_TIME);

        // Get all the shipPlanList where loadingEndTime equals to UPDATED_LOADING_END_TIME
        defaultShipPlanShouldNotBeFound("loadingEndTime.equals=" + UPDATED_LOADING_END_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLoadingEndTimeIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where loadingEndTime in DEFAULT_LOADING_END_TIME or UPDATED_LOADING_END_TIME
        defaultShipPlanShouldBeFound("loadingEndTime.in=" + DEFAULT_LOADING_END_TIME + "," + UPDATED_LOADING_END_TIME);

        // Get all the shipPlanList where loadingEndTime equals to UPDATED_LOADING_END_TIME
        defaultShipPlanShouldNotBeFound("loadingEndTime.in=" + UPDATED_LOADING_END_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLoadingEndTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where loadingEndTime is not null
        defaultShipPlanShouldBeFound("loadingEndTime.specified=true");

        // Get all the shipPlanList where loadingEndTime is null
        defaultShipPlanShouldNotBeFound("loadingEndTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByLoadingEndTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where loadingEndTime greater than or equals to DEFAULT_LOADING_END_TIME
        defaultShipPlanShouldBeFound("loadingEndTime.greaterOrEqualThan=" + DEFAULT_LOADING_END_TIME);

        // Get all the shipPlanList where loadingEndTime greater than or equals to UPDATED_LOADING_END_TIME
        defaultShipPlanShouldNotBeFound("loadingEndTime.greaterOrEqualThan=" + UPDATED_LOADING_END_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLoadingEndTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where loadingEndTime less than or equals to DEFAULT_LOADING_END_TIME
        defaultShipPlanShouldNotBeFound("loadingEndTime.lessThan=" + DEFAULT_LOADING_END_TIME);

        // Get all the shipPlanList where loadingEndTime less than or equals to UPDATED_LOADING_END_TIME
        defaultShipPlanShouldBeFound("loadingEndTime.lessThan=" + UPDATED_LOADING_END_TIME);
    }


    @Test
    @Transactional
    public void getAllShipPlansByCreateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where createTime equals to DEFAULT_CREATE_TIME
        defaultShipPlanShouldBeFound("createTime.equals=" + DEFAULT_CREATE_TIME);

        // Get all the shipPlanList where createTime equals to UPDATED_CREATE_TIME
        defaultShipPlanShouldNotBeFound("createTime.equals=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByCreateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where createTime in DEFAULT_CREATE_TIME or UPDATED_CREATE_TIME
        defaultShipPlanShouldBeFound("createTime.in=" + DEFAULT_CREATE_TIME + "," + UPDATED_CREATE_TIME);

        // Get all the shipPlanList where createTime equals to UPDATED_CREATE_TIME
        defaultShipPlanShouldNotBeFound("createTime.in=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByCreateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where createTime is not null
        defaultShipPlanShouldBeFound("createTime.specified=true");

        // Get all the shipPlanList where createTime is null
        defaultShipPlanShouldNotBeFound("createTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByCreateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where createTime greater than or equals to DEFAULT_CREATE_TIME
        defaultShipPlanShouldBeFound("createTime.greaterOrEqualThan=" + DEFAULT_CREATE_TIME);

        // Get all the shipPlanList where createTime greater than or equals to UPDATED_CREATE_TIME
        defaultShipPlanShouldNotBeFound("createTime.greaterOrEqualThan=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByCreateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where createTime less than or equals to DEFAULT_CREATE_TIME
        defaultShipPlanShouldNotBeFound("createTime.lessThan=" + DEFAULT_CREATE_TIME);

        // Get all the shipPlanList where createTime less than or equals to UPDATED_CREATE_TIME
        defaultShipPlanShouldBeFound("createTime.lessThan=" + UPDATED_CREATE_TIME);
    }


    @Test
    @Transactional
    public void getAllShipPlansByUpdateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where updateTime equals to DEFAULT_UPDATE_TIME
        defaultShipPlanShouldBeFound("updateTime.equals=" + DEFAULT_UPDATE_TIME);

        // Get all the shipPlanList where updateTime equals to UPDATED_UPDATE_TIME
        defaultShipPlanShouldNotBeFound("updateTime.equals=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByUpdateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where updateTime in DEFAULT_UPDATE_TIME or UPDATED_UPDATE_TIME
        defaultShipPlanShouldBeFound("updateTime.in=" + DEFAULT_UPDATE_TIME + "," + UPDATED_UPDATE_TIME);

        // Get all the shipPlanList where updateTime equals to UPDATED_UPDATE_TIME
        defaultShipPlanShouldNotBeFound("updateTime.in=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByUpdateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where updateTime is not null
        defaultShipPlanShouldBeFound("updateTime.specified=true");

        // Get all the shipPlanList where updateTime is null
        defaultShipPlanShouldNotBeFound("updateTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByUpdateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where updateTime greater than or equals to DEFAULT_UPDATE_TIME
        defaultShipPlanShouldBeFound("updateTime.greaterOrEqualThan=" + DEFAULT_UPDATE_TIME);

        // Get all the shipPlanList where updateTime greater than or equals to UPDATED_UPDATE_TIME
        defaultShipPlanShouldNotBeFound("updateTime.greaterOrEqualThan=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByUpdateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where updateTime less than or equals to DEFAULT_UPDATE_TIME
        defaultShipPlanShouldNotBeFound("updateTime.lessThan=" + DEFAULT_UPDATE_TIME);

        // Get all the shipPlanList where updateTime less than or equals to UPDATED_UPDATE_TIME
        defaultShipPlanShouldBeFound("updateTime.lessThan=" + UPDATED_UPDATE_TIME);
    }


    @Test
    @Transactional
    public void getAllShipPlansBySyncTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where syncTime equals to DEFAULT_SYNC_TIME
        defaultShipPlanShouldBeFound("syncTime.equals=" + DEFAULT_SYNC_TIME);

        // Get all the shipPlanList where syncTime equals to UPDATED_SYNC_TIME
        defaultShipPlanShouldNotBeFound("syncTime.equals=" + UPDATED_SYNC_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansBySyncTimeIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where syncTime in DEFAULT_SYNC_TIME or UPDATED_SYNC_TIME
        defaultShipPlanShouldBeFound("syncTime.in=" + DEFAULT_SYNC_TIME + "," + UPDATED_SYNC_TIME);

        // Get all the shipPlanList where syncTime equals to UPDATED_SYNC_TIME
        defaultShipPlanShouldNotBeFound("syncTime.in=" + UPDATED_SYNC_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansBySyncTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where syncTime is not null
        defaultShipPlanShouldBeFound("syncTime.specified=true");

        // Get all the shipPlanList where syncTime is null
        defaultShipPlanShouldNotBeFound("syncTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansBySyncTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where syncTime greater than or equals to DEFAULT_SYNC_TIME
        defaultShipPlanShouldBeFound("syncTime.greaterOrEqualThan=" + DEFAULT_SYNC_TIME);

        // Get all the shipPlanList where syncTime greater than or equals to UPDATED_SYNC_TIME
        defaultShipPlanShouldNotBeFound("syncTime.greaterOrEqualThan=" + UPDATED_SYNC_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansBySyncTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where syncTime less than or equals to DEFAULT_SYNC_TIME
        defaultShipPlanShouldNotBeFound("syncTime.lessThan=" + DEFAULT_SYNC_TIME);

        // Get all the shipPlanList where syncTime less than or equals to UPDATED_SYNC_TIME
        defaultShipPlanShouldBeFound("syncTime.lessThan=" + UPDATED_SYNC_TIME);
    }


    @Test
    @Transactional
    public void getAllShipPlansByTareAlertIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where tareAlert equals to DEFAULT_TARE_ALERT
        defaultShipPlanShouldBeFound("tareAlert.equals=" + DEFAULT_TARE_ALERT);

        // Get all the shipPlanList where tareAlert equals to UPDATED_TARE_ALERT
        defaultShipPlanShouldNotBeFound("tareAlert.equals=" + UPDATED_TARE_ALERT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByTareAlertIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where tareAlert in DEFAULT_TARE_ALERT or UPDATED_TARE_ALERT
        defaultShipPlanShouldBeFound("tareAlert.in=" + DEFAULT_TARE_ALERT + "," + UPDATED_TARE_ALERT);

        // Get all the shipPlanList where tareAlert equals to UPDATED_TARE_ALERT
        defaultShipPlanShouldNotBeFound("tareAlert.in=" + UPDATED_TARE_ALERT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByTareAlertIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where tareAlert is not null
        defaultShipPlanShouldBeFound("tareAlert.specified=true");

        // Get all the shipPlanList where tareAlert is null
        defaultShipPlanShouldNotBeFound("tareAlert.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByLeaveAlertIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where leaveAlert equals to DEFAULT_LEAVE_ALERT
        defaultShipPlanShouldBeFound("leaveAlert.equals=" + DEFAULT_LEAVE_ALERT);

        // Get all the shipPlanList where leaveAlert equals to UPDATED_LEAVE_ALERT
        defaultShipPlanShouldNotBeFound("leaveAlert.equals=" + UPDATED_LEAVE_ALERT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLeaveAlertIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where leaveAlert in DEFAULT_LEAVE_ALERT or UPDATED_LEAVE_ALERT
        defaultShipPlanShouldBeFound("leaveAlert.in=" + DEFAULT_LEAVE_ALERT + "," + UPDATED_LEAVE_ALERT);

        // Get all the shipPlanList where leaveAlert equals to UPDATED_LEAVE_ALERT
        defaultShipPlanShouldNotBeFound("leaveAlert.in=" + UPDATED_LEAVE_ALERT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLeaveAlertIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where leaveAlert is not null
        defaultShipPlanShouldBeFound("leaveAlert.specified=true");

        // Get all the shipPlanList where leaveAlert is null
        defaultShipPlanShouldNotBeFound("leaveAlert.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByNetWeightIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where netWeight equals to DEFAULT_NET_WEIGHT
        defaultShipPlanShouldBeFound("netWeight.equals=" + DEFAULT_NET_WEIGHT);

        // Get all the shipPlanList where netWeight equals to UPDATED_NET_WEIGHT
        defaultShipPlanShouldNotBeFound("netWeight.equals=" + UPDATED_NET_WEIGHT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByNetWeightIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where netWeight in DEFAULT_NET_WEIGHT or UPDATED_NET_WEIGHT
        defaultShipPlanShouldBeFound("netWeight.in=" + DEFAULT_NET_WEIGHT + "," + UPDATED_NET_WEIGHT);

        // Get all the shipPlanList where netWeight equals to UPDATED_NET_WEIGHT
        defaultShipPlanShouldNotBeFound("netWeight.in=" + UPDATED_NET_WEIGHT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByNetWeightIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where netWeight is not null
        defaultShipPlanShouldBeFound("netWeight.specified=true");

        // Get all the shipPlanList where netWeight is null
        defaultShipPlanShouldNotBeFound("netWeight.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByWeigherNoIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where weigherNo equals to DEFAULT_WEIGHER_NO
        defaultShipPlanShouldBeFound("weigherNo.equals=" + DEFAULT_WEIGHER_NO);

        // Get all the shipPlanList where weigherNo equals to UPDATED_WEIGHER_NO
        defaultShipPlanShouldNotBeFound("weigherNo.equals=" + UPDATED_WEIGHER_NO);
    }

    @Test
    @Transactional
    public void getAllShipPlansByWeigherNoIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where weigherNo in DEFAULT_WEIGHER_NO or UPDATED_WEIGHER_NO
        defaultShipPlanShouldBeFound("weigherNo.in=" + DEFAULT_WEIGHER_NO + "," + UPDATED_WEIGHER_NO);

        // Get all the shipPlanList where weigherNo equals to UPDATED_WEIGHER_NO
        defaultShipPlanShouldNotBeFound("weigherNo.in=" + UPDATED_WEIGHER_NO);
    }

    @Test
    @Transactional
    public void getAllShipPlansByWeigherNoIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where weigherNo is not null
        defaultShipPlanShouldBeFound("weigherNo.specified=true");

        // Get all the shipPlanList where weigherNo is null
        defaultShipPlanShouldNotBeFound("weigherNo.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByVipIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where vip equals to DEFAULT_VIP
        defaultShipPlanShouldBeFound("vip.equals=" + DEFAULT_VIP);

        // Get all the shipPlanList where vip equals to UPDATED_VIP
        defaultShipPlanShouldNotBeFound("vip.equals=" + UPDATED_VIP);
    }

    @Test
    @Transactional
    public void getAllShipPlansByVipIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where vip in DEFAULT_VIP or UPDATED_VIP
        defaultShipPlanShouldBeFound("vip.in=" + DEFAULT_VIP + "," + UPDATED_VIP);

        // Get all the shipPlanList where vip equals to UPDATED_VIP
        defaultShipPlanShouldNotBeFound("vip.in=" + UPDATED_VIP);
    }

    @Test
    @Transactional
    public void getAllShipPlansByVipIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where vip is not null
        defaultShipPlanShouldBeFound("vip.specified=true");

        // Get all the shipPlanList where vip is null
        defaultShipPlanShouldNotBeFound("vip.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByUserIsEqualToSomething() throws Exception {
        // Initialize the database
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        shipPlan.setUser(user);
        shipPlanRepository.saveAndFlush(shipPlan);
        Long userId = user.getId();

        // Get all the shipPlanList where user equals to userId
        defaultShipPlanShouldBeFound("userId.equals=" + userId);

        // Get all the shipPlanList where user equals to userId + 1
        defaultShipPlanShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultShipPlanShouldBeFound(String filter) throws Exception {
        restShipPlanMockMvc.perform(get("/api/ship-plans?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(shipPlan.getId().intValue())))
            .andExpect(jsonPath("$.[*].company").value(hasItem(DEFAULT_COMPANY)))
            .andExpect(jsonPath("$.[*].applyId").value(hasItem(DEFAULT_APPLY_ID.intValue())))
            .andExpect(jsonPath("$.[*].applyNumber").value(hasItem(DEFAULT_APPLY_NUMBER)))
            .andExpect(jsonPath("$.[*].truckNumber").value(hasItem(DEFAULT_TRUCK_NUMBER)))
            .andExpect(jsonPath("$.[*].auditStatus").value(hasItem(DEFAULT_AUDIT_STATUS)))
            .andExpect(jsonPath("$.[*].productName").value(hasItem(DEFAULT_PRODUCT_NAME)))
            .andExpect(jsonPath("$.[*].deliverPosition").value(hasItem(DEFAULT_DELIVER_POSITION)))
            .andExpect(jsonPath("$.[*].valid").value(hasItem(DEFAULT_VALID.booleanValue())))
            .andExpect(jsonPath("$.[*].gateTime").value(hasItem(sameInstant(DEFAULT_GATE_TIME))))
            .andExpect(jsonPath("$.[*].leaveTime").value(hasItem(sameInstant(DEFAULT_LEAVE_TIME))))
            .andExpect(jsonPath("$.[*].deliverTime").value(hasItem(sameInstant(DEFAULT_DELIVER_TIME))))
            .andExpect(jsonPath("$.[*].allowInTime").value(hasItem(sameInstant(DEFAULT_ALLOW_IN_TIME))))
            .andExpect(jsonPath("$.[*].loadingStartTime").value(hasItem(sameInstant(DEFAULT_LOADING_START_TIME))))
            .andExpect(jsonPath("$.[*].loadingEndTime").value(hasItem(sameInstant(DEFAULT_LOADING_END_TIME))))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))))
            .andExpect(jsonPath("$.[*].syncTime").value(hasItem(sameInstant(DEFAULT_SYNC_TIME))))
            .andExpect(jsonPath("$.[*].tareAlert").value(hasItem(DEFAULT_TARE_ALERT.booleanValue())))
            .andExpect(jsonPath("$.[*].leaveAlert").value(hasItem(DEFAULT_LEAVE_ALERT.booleanValue())))
            .andExpect(jsonPath("$.[*].netWeight").value(hasItem(DEFAULT_NET_WEIGHT.doubleValue())))
            .andExpect(jsonPath("$.[*].weigherNo").value(hasItem(DEFAULT_WEIGHER_NO)))
            .andExpect(jsonPath("$.[*].vip").value(hasItem(DEFAULT_VIP.booleanValue())));

        // Check, that the count call also returns 1
        restShipPlanMockMvc.perform(get("/api/ship-plans/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultShipPlanShouldNotBeFound(String filter) throws Exception {
        restShipPlanMockMvc.perform(get("/api/ship-plans?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restShipPlanMockMvc.perform(get("/api/ship-plans/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingShipPlan() throws Exception {
        // Get the shipPlan
        restShipPlanMockMvc.perform(get("/api/ship-plans/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateShipPlan() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        int databaseSizeBeforeUpdate = shipPlanRepository.findAll().size();

        // Update the shipPlan
        ShipPlan updatedShipPlan = shipPlanRepository.findById(shipPlan.getId()).get();
        // Disconnect from session so that the updates on updatedShipPlan are not directly saved in db
        em.detach(updatedShipPlan);
        updatedShipPlan
            .company(UPDATED_COMPANY)
            .applyId(UPDATED_APPLY_ID)
            .applyNumber(UPDATED_APPLY_NUMBER)
            .truckNumber(UPDATED_TRUCK_NUMBER)
            .auditStatus(UPDATED_AUDIT_STATUS)
            .productName(UPDATED_PRODUCT_NAME)
            .deliverPosition(UPDATED_DELIVER_POSITION)
            .valid(UPDATED_VALID)
            .gateTime(UPDATED_GATE_TIME)
            .leaveTime(UPDATED_LEAVE_TIME)
            .deliverTime(UPDATED_DELIVER_TIME)
            .allowInTime(UPDATED_ALLOW_IN_TIME)
            .loadingStartTime(UPDATED_LOADING_START_TIME)
            .loadingEndTime(UPDATED_LOADING_END_TIME)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME)
            .syncTime(UPDATED_SYNC_TIME)
            .tareAlert(UPDATED_TARE_ALERT)
            .leaveAlert(UPDATED_LEAVE_ALERT)
            .netWeight(UPDATED_NET_WEIGHT)
            .weigherNo(UPDATED_WEIGHER_NO)
            .vip(UPDATED_VIP);
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(updatedShipPlan);

        restShipPlanMockMvc.perform(put("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isOk());

        // Validate the ShipPlan in the database
        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeUpdate);
        ShipPlan testShipPlan = shipPlanList.get(shipPlanList.size() - 1);
        assertThat(testShipPlan.getCompany()).isEqualTo(UPDATED_COMPANY);
        assertThat(testShipPlan.getApplyId()).isEqualTo(UPDATED_APPLY_ID);
        assertThat(testShipPlan.getApplyNumber()).isEqualTo(UPDATED_APPLY_NUMBER);
        assertThat(testShipPlan.getTruckNumber()).isEqualTo(UPDATED_TRUCK_NUMBER);
        assertThat(testShipPlan.getAuditStatus()).isEqualTo(UPDATED_AUDIT_STATUS);
        assertThat(testShipPlan.getProductName()).isEqualTo(UPDATED_PRODUCT_NAME);
        assertThat(testShipPlan.getDeliverPosition()).isEqualTo(UPDATED_DELIVER_POSITION);
        assertThat(testShipPlan.isValid()).isEqualTo(UPDATED_VALID);
        assertThat(testShipPlan.getGateTime()).isEqualTo(UPDATED_GATE_TIME);
        assertThat(testShipPlan.getLeaveTime()).isEqualTo(UPDATED_LEAVE_TIME);
        assertThat(testShipPlan.getDeliverTime()).isEqualTo(UPDATED_DELIVER_TIME);
        assertThat(testShipPlan.getAllowInTime()).isEqualTo(UPDATED_ALLOW_IN_TIME);
        assertThat(testShipPlan.getLoadingStartTime()).isEqualTo(UPDATED_LOADING_START_TIME);
        assertThat(testShipPlan.getLoadingEndTime()).isEqualTo(UPDATED_LOADING_END_TIME);
        assertThat(testShipPlan.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testShipPlan.getUpdateTime()).isEqualTo(UPDATED_UPDATE_TIME);
        assertThat(testShipPlan.getSyncTime()).isEqualTo(UPDATED_SYNC_TIME);
        assertThat(testShipPlan.isTareAlert()).isEqualTo(UPDATED_TARE_ALERT);
        assertThat(testShipPlan.isLeaveAlert()).isEqualTo(UPDATED_LEAVE_ALERT);
        assertThat(testShipPlan.getNetWeight()).isEqualTo(UPDATED_NET_WEIGHT);
        assertThat(testShipPlan.getWeigherNo()).isEqualTo(UPDATED_WEIGHER_NO);
        assertThat(testShipPlan.isVip()).isEqualTo(UPDATED_VIP);
    }

    @Test
    @Transactional
    public void updateNonExistingShipPlan() throws Exception {
        int databaseSizeBeforeUpdate = shipPlanRepository.findAll().size();

        // Create the ShipPlan
        ShipPlanDTO shipPlanDTO = shipPlanMapper.toDto(shipPlan);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShipPlanMockMvc.perform(put("/api/ship-plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shipPlanDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ShipPlan in the database
        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteShipPlan() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        int databaseSizeBeforeDelete = shipPlanRepository.findAll().size();

        // Delete the shipPlan
        restShipPlanMockMvc.perform(delete("/api/ship-plans/{id}", shipPlan.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<ShipPlan> shipPlanList = shipPlanRepository.findAll();
        assertThat(shipPlanList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ShipPlan.class);
        ShipPlan shipPlan1 = new ShipPlan();
        shipPlan1.setId(1L);
        ShipPlan shipPlan2 = new ShipPlan();
        shipPlan2.setId(shipPlan1.getId());
        assertThat(shipPlan1).isEqualTo(shipPlan2);
        shipPlan2.setId(2L);
        assertThat(shipPlan1).isNotEqualTo(shipPlan2);
        shipPlan1.setId(null);
        assertThat(shipPlan1).isNotEqualTo(shipPlan2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ShipPlanDTO.class);
        ShipPlanDTO shipPlanDTO1 = new ShipPlanDTO();
        shipPlanDTO1.setId(1L);
        ShipPlanDTO shipPlanDTO2 = new ShipPlanDTO();
        assertThat(shipPlanDTO1).isNotEqualTo(shipPlanDTO2);
        shipPlanDTO2.setId(shipPlanDTO1.getId());
        assertThat(shipPlanDTO1).isEqualTo(shipPlanDTO2);
        shipPlanDTO2.setId(2L);
        assertThat(shipPlanDTO1).isNotEqualTo(shipPlanDTO2);
        shipPlanDTO1.setId(null);
        assertThat(shipPlanDTO1).isNotEqualTo(shipPlanDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(shipPlanMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(shipPlanMapper.fromId(null)).isNull();
    }
}
