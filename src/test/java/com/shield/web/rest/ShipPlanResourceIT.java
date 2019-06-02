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

import com.shield.domain.enumeration.ShipMethod;
/**
 * Integration tests for the {@Link ShipPlanResource} REST controller.
 */
@SpringBootTest(classes = ShieldApp.class)
public class ShipPlanResourceIT {

    private static final String DEFAULT_COMPANY = "AAAAAAAAAA";
    private static final String UPDATED_COMPANY = "BBBBBBBBBB";

    private static final Integer DEFAULT_DEMANDED_AMOUNT = 0;
    private static final Integer UPDATED_DEMANDED_AMOUNT = 1;

    private static final Integer DEFAULT_FINISH_AMOUNT = 1;
    private static final Integer UPDATED_FINISH_AMOUNT = 2;

    private static final Integer DEFAULT_REMAIN_AMOUNT = 1;
    private static final Integer UPDATED_REMAIN_AMOUNT = 2;

    private static final Integer DEFAULT_AVAILABLE_AMOUNT = 1;
    private static final Integer UPDATED_AVAILABLE_AMOUNT = 2;

    private static final ShipMethod DEFAULT_SHIP_METHOND = ShipMethod.LAND;
    private static final ShipMethod UPDATED_SHIP_METHOND = ShipMethod.AIR;

    private static final String DEFAULT_SHIP_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_SHIP_NUMBER = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_END_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_END_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_LICENSE_PLATE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_LICENSE_PLATE_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_DRIVER = "AAAAAAAAAA";
    private static final String UPDATED_DRIVER = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

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
            .demandedAmount(DEFAULT_DEMANDED_AMOUNT)
            .finishAmount(DEFAULT_FINISH_AMOUNT)
            .remainAmount(DEFAULT_REMAIN_AMOUNT)
            .availableAmount(DEFAULT_AVAILABLE_AMOUNT)
            .shipMethond(DEFAULT_SHIP_METHOND)
            .shipNumber(DEFAULT_SHIP_NUMBER)
            .endTime(DEFAULT_END_TIME)
            .createTime(DEFAULT_CREATE_TIME)
            .updateTime(DEFAULT_UPDATE_TIME)
            .licensePlateNumber(DEFAULT_LICENSE_PLATE_NUMBER)
            .driver(DEFAULT_DRIVER)
            .phone(DEFAULT_PHONE);
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
            .demandedAmount(UPDATED_DEMANDED_AMOUNT)
            .finishAmount(UPDATED_FINISH_AMOUNT)
            .remainAmount(UPDATED_REMAIN_AMOUNT)
            .availableAmount(UPDATED_AVAILABLE_AMOUNT)
            .shipMethond(UPDATED_SHIP_METHOND)
            .shipNumber(UPDATED_SHIP_NUMBER)
            .endTime(UPDATED_END_TIME)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME)
            .licensePlateNumber(UPDATED_LICENSE_PLATE_NUMBER)
            .driver(UPDATED_DRIVER)
            .phone(UPDATED_PHONE);
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
        assertThat(testShipPlan.getDemandedAmount()).isEqualTo(DEFAULT_DEMANDED_AMOUNT);
        assertThat(testShipPlan.getFinishAmount()).isEqualTo(DEFAULT_FINISH_AMOUNT);
        assertThat(testShipPlan.getRemainAmount()).isEqualTo(DEFAULT_REMAIN_AMOUNT);
        assertThat(testShipPlan.getAvailableAmount()).isEqualTo(DEFAULT_AVAILABLE_AMOUNT);
        assertThat(testShipPlan.getShipMethond()).isEqualTo(DEFAULT_SHIP_METHOND);
        assertThat(testShipPlan.getShipNumber()).isEqualTo(DEFAULT_SHIP_NUMBER);
        assertThat(testShipPlan.getEndTime()).isEqualTo(DEFAULT_END_TIME);
        assertThat(testShipPlan.getCreateTime()).isEqualTo(DEFAULT_CREATE_TIME);
        assertThat(testShipPlan.getUpdateTime()).isEqualTo(DEFAULT_UPDATE_TIME);
        assertThat(testShipPlan.getLicensePlateNumber()).isEqualTo(DEFAULT_LICENSE_PLATE_NUMBER);
        assertThat(testShipPlan.getDriver()).isEqualTo(DEFAULT_DRIVER);
        assertThat(testShipPlan.getPhone()).isEqualTo(DEFAULT_PHONE);
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
    public void checkDemandedAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setDemandedAmount(null);

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
    public void checkLicensePlateNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipPlanRepository.findAll().size();
        // set the field null
        shipPlan.setLicensePlateNumber(null);

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
            .andExpect(jsonPath("$.[*].demandedAmount").value(hasItem(DEFAULT_DEMANDED_AMOUNT)))
            .andExpect(jsonPath("$.[*].finishAmount").value(hasItem(DEFAULT_FINISH_AMOUNT)))
            .andExpect(jsonPath("$.[*].remainAmount").value(hasItem(DEFAULT_REMAIN_AMOUNT)))
            .andExpect(jsonPath("$.[*].availableAmount").value(hasItem(DEFAULT_AVAILABLE_AMOUNT)))
            .andExpect(jsonPath("$.[*].shipMethond").value(hasItem(DEFAULT_SHIP_METHOND.toString())))
            .andExpect(jsonPath("$.[*].shipNumber").value(hasItem(DEFAULT_SHIP_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(sameInstant(DEFAULT_END_TIME))))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))))
            .andExpect(jsonPath("$.[*].licensePlateNumber").value(hasItem(DEFAULT_LICENSE_PLATE_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].driver").value(hasItem(DEFAULT_DRIVER.toString())))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE.toString())));
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
            .andExpect(jsonPath("$.demandedAmount").value(DEFAULT_DEMANDED_AMOUNT))
            .andExpect(jsonPath("$.finishAmount").value(DEFAULT_FINISH_AMOUNT))
            .andExpect(jsonPath("$.remainAmount").value(DEFAULT_REMAIN_AMOUNT))
            .andExpect(jsonPath("$.availableAmount").value(DEFAULT_AVAILABLE_AMOUNT))
            .andExpect(jsonPath("$.shipMethond").value(DEFAULT_SHIP_METHOND.toString()))
            .andExpect(jsonPath("$.shipNumber").value(DEFAULT_SHIP_NUMBER.toString()))
            .andExpect(jsonPath("$.endTime").value(sameInstant(DEFAULT_END_TIME)))
            .andExpect(jsonPath("$.createTime").value(sameInstant(DEFAULT_CREATE_TIME)))
            .andExpect(jsonPath("$.updateTime").value(sameInstant(DEFAULT_UPDATE_TIME)))
            .andExpect(jsonPath("$.licensePlateNumber").value(DEFAULT_LICENSE_PLATE_NUMBER.toString()))
            .andExpect(jsonPath("$.driver").value(DEFAULT_DRIVER.toString()))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE.toString()));
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
    public void getAllShipPlansByDemandedAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where demandedAmount equals to DEFAULT_DEMANDED_AMOUNT
        defaultShipPlanShouldBeFound("demandedAmount.equals=" + DEFAULT_DEMANDED_AMOUNT);

        // Get all the shipPlanList where demandedAmount equals to UPDATED_DEMANDED_AMOUNT
        defaultShipPlanShouldNotBeFound("demandedAmount.equals=" + UPDATED_DEMANDED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByDemandedAmountIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where demandedAmount in DEFAULT_DEMANDED_AMOUNT or UPDATED_DEMANDED_AMOUNT
        defaultShipPlanShouldBeFound("demandedAmount.in=" + DEFAULT_DEMANDED_AMOUNT + "," + UPDATED_DEMANDED_AMOUNT);

        // Get all the shipPlanList where demandedAmount equals to UPDATED_DEMANDED_AMOUNT
        defaultShipPlanShouldNotBeFound("demandedAmount.in=" + UPDATED_DEMANDED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByDemandedAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where demandedAmount is not null
        defaultShipPlanShouldBeFound("demandedAmount.specified=true");

        // Get all the shipPlanList where demandedAmount is null
        defaultShipPlanShouldNotBeFound("demandedAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByDemandedAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where demandedAmount greater than or equals to DEFAULT_DEMANDED_AMOUNT
        defaultShipPlanShouldBeFound("demandedAmount.greaterOrEqualThan=" + DEFAULT_DEMANDED_AMOUNT);

        // Get all the shipPlanList where demandedAmount greater than or equals to UPDATED_DEMANDED_AMOUNT
        defaultShipPlanShouldNotBeFound("demandedAmount.greaterOrEqualThan=" + UPDATED_DEMANDED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByDemandedAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where demandedAmount less than or equals to DEFAULT_DEMANDED_AMOUNT
        defaultShipPlanShouldNotBeFound("demandedAmount.lessThan=" + DEFAULT_DEMANDED_AMOUNT);

        // Get all the shipPlanList where demandedAmount less than or equals to UPDATED_DEMANDED_AMOUNT
        defaultShipPlanShouldBeFound("demandedAmount.lessThan=" + UPDATED_DEMANDED_AMOUNT);
    }


    @Test
    @Transactional
    public void getAllShipPlansByFinishAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where finishAmount equals to DEFAULT_FINISH_AMOUNT
        defaultShipPlanShouldBeFound("finishAmount.equals=" + DEFAULT_FINISH_AMOUNT);

        // Get all the shipPlanList where finishAmount equals to UPDATED_FINISH_AMOUNT
        defaultShipPlanShouldNotBeFound("finishAmount.equals=" + UPDATED_FINISH_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByFinishAmountIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where finishAmount in DEFAULT_FINISH_AMOUNT or UPDATED_FINISH_AMOUNT
        defaultShipPlanShouldBeFound("finishAmount.in=" + DEFAULT_FINISH_AMOUNT + "," + UPDATED_FINISH_AMOUNT);

        // Get all the shipPlanList where finishAmount equals to UPDATED_FINISH_AMOUNT
        defaultShipPlanShouldNotBeFound("finishAmount.in=" + UPDATED_FINISH_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByFinishAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where finishAmount is not null
        defaultShipPlanShouldBeFound("finishAmount.specified=true");

        // Get all the shipPlanList where finishAmount is null
        defaultShipPlanShouldNotBeFound("finishAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByFinishAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where finishAmount greater than or equals to DEFAULT_FINISH_AMOUNT
        defaultShipPlanShouldBeFound("finishAmount.greaterOrEqualThan=" + DEFAULT_FINISH_AMOUNT);

        // Get all the shipPlanList where finishAmount greater than or equals to UPDATED_FINISH_AMOUNT
        defaultShipPlanShouldNotBeFound("finishAmount.greaterOrEqualThan=" + UPDATED_FINISH_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByFinishAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where finishAmount less than or equals to DEFAULT_FINISH_AMOUNT
        defaultShipPlanShouldNotBeFound("finishAmount.lessThan=" + DEFAULT_FINISH_AMOUNT);

        // Get all the shipPlanList where finishAmount less than or equals to UPDATED_FINISH_AMOUNT
        defaultShipPlanShouldBeFound("finishAmount.lessThan=" + UPDATED_FINISH_AMOUNT);
    }


    @Test
    @Transactional
    public void getAllShipPlansByRemainAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where remainAmount equals to DEFAULT_REMAIN_AMOUNT
        defaultShipPlanShouldBeFound("remainAmount.equals=" + DEFAULT_REMAIN_AMOUNT);

        // Get all the shipPlanList where remainAmount equals to UPDATED_REMAIN_AMOUNT
        defaultShipPlanShouldNotBeFound("remainAmount.equals=" + UPDATED_REMAIN_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByRemainAmountIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where remainAmount in DEFAULT_REMAIN_AMOUNT or UPDATED_REMAIN_AMOUNT
        defaultShipPlanShouldBeFound("remainAmount.in=" + DEFAULT_REMAIN_AMOUNT + "," + UPDATED_REMAIN_AMOUNT);

        // Get all the shipPlanList where remainAmount equals to UPDATED_REMAIN_AMOUNT
        defaultShipPlanShouldNotBeFound("remainAmount.in=" + UPDATED_REMAIN_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByRemainAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where remainAmount is not null
        defaultShipPlanShouldBeFound("remainAmount.specified=true");

        // Get all the shipPlanList where remainAmount is null
        defaultShipPlanShouldNotBeFound("remainAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByRemainAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where remainAmount greater than or equals to DEFAULT_REMAIN_AMOUNT
        defaultShipPlanShouldBeFound("remainAmount.greaterOrEqualThan=" + DEFAULT_REMAIN_AMOUNT);

        // Get all the shipPlanList where remainAmount greater than or equals to UPDATED_REMAIN_AMOUNT
        defaultShipPlanShouldNotBeFound("remainAmount.greaterOrEqualThan=" + UPDATED_REMAIN_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByRemainAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where remainAmount less than or equals to DEFAULT_REMAIN_AMOUNT
        defaultShipPlanShouldNotBeFound("remainAmount.lessThan=" + DEFAULT_REMAIN_AMOUNT);

        // Get all the shipPlanList where remainAmount less than or equals to UPDATED_REMAIN_AMOUNT
        defaultShipPlanShouldBeFound("remainAmount.lessThan=" + UPDATED_REMAIN_AMOUNT);
    }


    @Test
    @Transactional
    public void getAllShipPlansByAvailableAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where availableAmount equals to DEFAULT_AVAILABLE_AMOUNT
        defaultShipPlanShouldBeFound("availableAmount.equals=" + DEFAULT_AVAILABLE_AMOUNT);

        // Get all the shipPlanList where availableAmount equals to UPDATED_AVAILABLE_AMOUNT
        defaultShipPlanShouldNotBeFound("availableAmount.equals=" + UPDATED_AVAILABLE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByAvailableAmountIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where availableAmount in DEFAULT_AVAILABLE_AMOUNT or UPDATED_AVAILABLE_AMOUNT
        defaultShipPlanShouldBeFound("availableAmount.in=" + DEFAULT_AVAILABLE_AMOUNT + "," + UPDATED_AVAILABLE_AMOUNT);

        // Get all the shipPlanList where availableAmount equals to UPDATED_AVAILABLE_AMOUNT
        defaultShipPlanShouldNotBeFound("availableAmount.in=" + UPDATED_AVAILABLE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByAvailableAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where availableAmount is not null
        defaultShipPlanShouldBeFound("availableAmount.specified=true");

        // Get all the shipPlanList where availableAmount is null
        defaultShipPlanShouldNotBeFound("availableAmount.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByAvailableAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where availableAmount greater than or equals to DEFAULT_AVAILABLE_AMOUNT
        defaultShipPlanShouldBeFound("availableAmount.greaterOrEqualThan=" + DEFAULT_AVAILABLE_AMOUNT);

        // Get all the shipPlanList where availableAmount greater than or equals to UPDATED_AVAILABLE_AMOUNT
        defaultShipPlanShouldNotBeFound("availableAmount.greaterOrEqualThan=" + UPDATED_AVAILABLE_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllShipPlansByAvailableAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where availableAmount less than or equals to DEFAULT_AVAILABLE_AMOUNT
        defaultShipPlanShouldNotBeFound("availableAmount.lessThan=" + DEFAULT_AVAILABLE_AMOUNT);

        // Get all the shipPlanList where availableAmount less than or equals to UPDATED_AVAILABLE_AMOUNT
        defaultShipPlanShouldBeFound("availableAmount.lessThan=" + UPDATED_AVAILABLE_AMOUNT);
    }


    @Test
    @Transactional
    public void getAllShipPlansByShipMethondIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where shipMethond equals to DEFAULT_SHIP_METHOND
        defaultShipPlanShouldBeFound("shipMethond.equals=" + DEFAULT_SHIP_METHOND);

        // Get all the shipPlanList where shipMethond equals to UPDATED_SHIP_METHOND
        defaultShipPlanShouldNotBeFound("shipMethond.equals=" + UPDATED_SHIP_METHOND);
    }

    @Test
    @Transactional
    public void getAllShipPlansByShipMethondIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where shipMethond in DEFAULT_SHIP_METHOND or UPDATED_SHIP_METHOND
        defaultShipPlanShouldBeFound("shipMethond.in=" + DEFAULT_SHIP_METHOND + "," + UPDATED_SHIP_METHOND);

        // Get all the shipPlanList where shipMethond equals to UPDATED_SHIP_METHOND
        defaultShipPlanShouldNotBeFound("shipMethond.in=" + UPDATED_SHIP_METHOND);
    }

    @Test
    @Transactional
    public void getAllShipPlansByShipMethondIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where shipMethond is not null
        defaultShipPlanShouldBeFound("shipMethond.specified=true");

        // Get all the shipPlanList where shipMethond is null
        defaultShipPlanShouldNotBeFound("shipMethond.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByShipNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where shipNumber equals to DEFAULT_SHIP_NUMBER
        defaultShipPlanShouldBeFound("shipNumber.equals=" + DEFAULT_SHIP_NUMBER);

        // Get all the shipPlanList where shipNumber equals to UPDATED_SHIP_NUMBER
        defaultShipPlanShouldNotBeFound("shipNumber.equals=" + UPDATED_SHIP_NUMBER);
    }

    @Test
    @Transactional
    public void getAllShipPlansByShipNumberIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where shipNumber in DEFAULT_SHIP_NUMBER or UPDATED_SHIP_NUMBER
        defaultShipPlanShouldBeFound("shipNumber.in=" + DEFAULT_SHIP_NUMBER + "," + UPDATED_SHIP_NUMBER);

        // Get all the shipPlanList where shipNumber equals to UPDATED_SHIP_NUMBER
        defaultShipPlanShouldNotBeFound("shipNumber.in=" + UPDATED_SHIP_NUMBER);
    }

    @Test
    @Transactional
    public void getAllShipPlansByShipNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where shipNumber is not null
        defaultShipPlanShouldBeFound("shipNumber.specified=true");

        // Get all the shipPlanList where shipNumber is null
        defaultShipPlanShouldNotBeFound("shipNumber.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByEndTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where endTime equals to DEFAULT_END_TIME
        defaultShipPlanShouldBeFound("endTime.equals=" + DEFAULT_END_TIME);

        // Get all the shipPlanList where endTime equals to UPDATED_END_TIME
        defaultShipPlanShouldNotBeFound("endTime.equals=" + UPDATED_END_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByEndTimeIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where endTime in DEFAULT_END_TIME or UPDATED_END_TIME
        defaultShipPlanShouldBeFound("endTime.in=" + DEFAULT_END_TIME + "," + UPDATED_END_TIME);

        // Get all the shipPlanList where endTime equals to UPDATED_END_TIME
        defaultShipPlanShouldNotBeFound("endTime.in=" + UPDATED_END_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByEndTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where endTime is not null
        defaultShipPlanShouldBeFound("endTime.specified=true");

        // Get all the shipPlanList where endTime is null
        defaultShipPlanShouldNotBeFound("endTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByEndTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where endTime greater than or equals to DEFAULT_END_TIME
        defaultShipPlanShouldBeFound("endTime.greaterOrEqualThan=" + DEFAULT_END_TIME);

        // Get all the shipPlanList where endTime greater than or equals to UPDATED_END_TIME
        defaultShipPlanShouldNotBeFound("endTime.greaterOrEqualThan=" + UPDATED_END_TIME);
    }

    @Test
    @Transactional
    public void getAllShipPlansByEndTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where endTime less than or equals to DEFAULT_END_TIME
        defaultShipPlanShouldNotBeFound("endTime.lessThan=" + DEFAULT_END_TIME);

        // Get all the shipPlanList where endTime less than or equals to UPDATED_END_TIME
        defaultShipPlanShouldBeFound("endTime.lessThan=" + UPDATED_END_TIME);
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
    public void getAllShipPlansByLicensePlateNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where licensePlateNumber equals to DEFAULT_LICENSE_PLATE_NUMBER
        defaultShipPlanShouldBeFound("licensePlateNumber.equals=" + DEFAULT_LICENSE_PLATE_NUMBER);

        // Get all the shipPlanList where licensePlateNumber equals to UPDATED_LICENSE_PLATE_NUMBER
        defaultShipPlanShouldNotBeFound("licensePlateNumber.equals=" + UPDATED_LICENSE_PLATE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLicensePlateNumberIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where licensePlateNumber in DEFAULT_LICENSE_PLATE_NUMBER or UPDATED_LICENSE_PLATE_NUMBER
        defaultShipPlanShouldBeFound("licensePlateNumber.in=" + DEFAULT_LICENSE_PLATE_NUMBER + "," + UPDATED_LICENSE_PLATE_NUMBER);

        // Get all the shipPlanList where licensePlateNumber equals to UPDATED_LICENSE_PLATE_NUMBER
        defaultShipPlanShouldNotBeFound("licensePlateNumber.in=" + UPDATED_LICENSE_PLATE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllShipPlansByLicensePlateNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where licensePlateNumber is not null
        defaultShipPlanShouldBeFound("licensePlateNumber.specified=true");

        // Get all the shipPlanList where licensePlateNumber is null
        defaultShipPlanShouldNotBeFound("licensePlateNumber.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByDriverIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where driver equals to DEFAULT_DRIVER
        defaultShipPlanShouldBeFound("driver.equals=" + DEFAULT_DRIVER);

        // Get all the shipPlanList where driver equals to UPDATED_DRIVER
        defaultShipPlanShouldNotBeFound("driver.equals=" + UPDATED_DRIVER);
    }

    @Test
    @Transactional
    public void getAllShipPlansByDriverIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where driver in DEFAULT_DRIVER or UPDATED_DRIVER
        defaultShipPlanShouldBeFound("driver.in=" + DEFAULT_DRIVER + "," + UPDATED_DRIVER);

        // Get all the shipPlanList where driver equals to UPDATED_DRIVER
        defaultShipPlanShouldNotBeFound("driver.in=" + UPDATED_DRIVER);
    }

    @Test
    @Transactional
    public void getAllShipPlansByDriverIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where driver is not null
        defaultShipPlanShouldBeFound("driver.specified=true");

        // Get all the shipPlanList where driver is null
        defaultShipPlanShouldNotBeFound("driver.specified=false");
    }

    @Test
    @Transactional
    public void getAllShipPlansByPhoneIsEqualToSomething() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where phone equals to DEFAULT_PHONE
        defaultShipPlanShouldBeFound("phone.equals=" + DEFAULT_PHONE);

        // Get all the shipPlanList where phone equals to UPDATED_PHONE
        defaultShipPlanShouldNotBeFound("phone.equals=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    public void getAllShipPlansByPhoneIsInShouldWork() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where phone in DEFAULT_PHONE or UPDATED_PHONE
        defaultShipPlanShouldBeFound("phone.in=" + DEFAULT_PHONE + "," + UPDATED_PHONE);

        // Get all the shipPlanList where phone equals to UPDATED_PHONE
        defaultShipPlanShouldNotBeFound("phone.in=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    public void getAllShipPlansByPhoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        shipPlanRepository.saveAndFlush(shipPlan);

        // Get all the shipPlanList where phone is not null
        defaultShipPlanShouldBeFound("phone.specified=true");

        // Get all the shipPlanList where phone is null
        defaultShipPlanShouldNotBeFound("phone.specified=false");
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


    @Test
    @Transactional
    public void getAllShipPlansByToUserIsEqualToSomething() throws Exception {
        // Initialize the database
        User toUser = UserResourceIT.createEntity(em);
        em.persist(toUser);
        em.flush();
        shipPlan.setToUser(toUser);
        shipPlanRepository.saveAndFlush(shipPlan);
        Long toUserId = toUser.getId();

        // Get all the shipPlanList where toUser equals to toUserId
        defaultShipPlanShouldBeFound("toUserId.equals=" + toUserId);

        // Get all the shipPlanList where toUser equals to toUserId + 1
        defaultShipPlanShouldNotBeFound("toUserId.equals=" + (toUserId + 1));
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
            .andExpect(jsonPath("$.[*].demandedAmount").value(hasItem(DEFAULT_DEMANDED_AMOUNT)))
            .andExpect(jsonPath("$.[*].finishAmount").value(hasItem(DEFAULT_FINISH_AMOUNT)))
            .andExpect(jsonPath("$.[*].remainAmount").value(hasItem(DEFAULT_REMAIN_AMOUNT)))
            .andExpect(jsonPath("$.[*].availableAmount").value(hasItem(DEFAULT_AVAILABLE_AMOUNT)))
            .andExpect(jsonPath("$.[*].shipMethond").value(hasItem(DEFAULT_SHIP_METHOND.toString())))
            .andExpect(jsonPath("$.[*].shipNumber").value(hasItem(DEFAULT_SHIP_NUMBER)))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(sameInstant(DEFAULT_END_TIME))))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))))
            .andExpect(jsonPath("$.[*].licensePlateNumber").value(hasItem(DEFAULT_LICENSE_PLATE_NUMBER)))
            .andExpect(jsonPath("$.[*].driver").value(hasItem(DEFAULT_DRIVER)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)));

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
            .demandedAmount(UPDATED_DEMANDED_AMOUNT)
            .finishAmount(UPDATED_FINISH_AMOUNT)
            .remainAmount(UPDATED_REMAIN_AMOUNT)
            .availableAmount(UPDATED_AVAILABLE_AMOUNT)
            .shipMethond(UPDATED_SHIP_METHOND)
            .shipNumber(UPDATED_SHIP_NUMBER)
            .endTime(UPDATED_END_TIME)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME)
            .licensePlateNumber(UPDATED_LICENSE_PLATE_NUMBER)
            .driver(UPDATED_DRIVER)
            .phone(UPDATED_PHONE);
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
        assertThat(testShipPlan.getDemandedAmount()).isEqualTo(UPDATED_DEMANDED_AMOUNT);
        assertThat(testShipPlan.getFinishAmount()).isEqualTo(UPDATED_FINISH_AMOUNT);
        assertThat(testShipPlan.getRemainAmount()).isEqualTo(UPDATED_REMAIN_AMOUNT);
        assertThat(testShipPlan.getAvailableAmount()).isEqualTo(UPDATED_AVAILABLE_AMOUNT);
        assertThat(testShipPlan.getShipMethond()).isEqualTo(UPDATED_SHIP_METHOND);
        assertThat(testShipPlan.getShipNumber()).isEqualTo(UPDATED_SHIP_NUMBER);
        assertThat(testShipPlan.getEndTime()).isEqualTo(UPDATED_END_TIME);
        assertThat(testShipPlan.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testShipPlan.getUpdateTime()).isEqualTo(UPDATED_UPDATE_TIME);
        assertThat(testShipPlan.getLicensePlateNumber()).isEqualTo(UPDATED_LICENSE_PLATE_NUMBER);
        assertThat(testShipPlan.getDriver()).isEqualTo(UPDATED_DRIVER);
        assertThat(testShipPlan.getPhone()).isEqualTo(UPDATED_PHONE);
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
