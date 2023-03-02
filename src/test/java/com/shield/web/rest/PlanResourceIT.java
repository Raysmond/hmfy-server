package com.shield.web.rest;

import com.shield.ShieldApp;
import com.shield.domain.Plan;
import com.shield.repository.PlanRepository;
import com.shield.service.PlanService;
import com.shield.service.dto.PlanDTO;
import com.shield.service.mapper.PlanMapper;
import com.shield.web.rest.errors.ExceptionTranslator;
import com.shield.service.dto.PlanCriteria;
import com.shield.service.PlanQueryService;

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
import java.time.LocalDate;
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
 * Integration tests for the {@Link PlanResource} REST controller.
 */
@SpringBootTest(classes = ShieldApp.class)
public class PlanResourceIT {

    private static final String DEFAULT_PLAN_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PLAN_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_LOCATION = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_WORK_DAY = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_WORK_DAY = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_STOCK_NAME = "AAAAAAAAAA";
    private static final String UPDATED_STOCK_NAME = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_LOADING_START_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_LOADING_START_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_LOADING_END_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_LOADING_END_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Double DEFAULT_WEIGHT_SUM = 1D;
    private static final Double UPDATED_WEIGHT_SUM = 2D;

    private static final String DEFAULT_OPERATOR = "AAAAAAAAAA";
    private static final String UPDATED_OPERATOR = "BBBBBBBBBB";

    private static final String DEFAULT_OPERATION = "AAAAAAAAAA";
    private static final String UPDATED_OPERATION = "BBBBBBBBBB";

    private static final String DEFAULT_OP_POSITION = "AAAAAAAAAA";
    private static final String UPDATED_OP_POSITION = "BBBBBBBBBB";

    private static final String DEFAULT_CHANNEL = "AAAAAAAAAA";
    private static final String UPDATED_CHANNEL = "BBBBBBBBBB";

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanQueryService planQueryService;

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

    private MockMvc restPlanMockMvc;

    private Plan plan;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PlanResource planResource = new PlanResource(planService, planQueryService);
        this.restPlanMockMvc = MockMvcBuilders.standaloneSetup(planResource)
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
    public static Plan createEntity(EntityManager em) {
        Plan plan = new Plan()
            .planNumber(DEFAULT_PLAN_NUMBER)
            .location(DEFAULT_LOCATION)
            .workDay(DEFAULT_WORK_DAY)
            .stockName(DEFAULT_STOCK_NAME)
            .loadingStartTime(DEFAULT_LOADING_START_TIME)
            .loadingEndTime(DEFAULT_LOADING_END_TIME)
            .weightSum(DEFAULT_WEIGHT_SUM)
            .operator(DEFAULT_OPERATOR)
            .operation(DEFAULT_OPERATION)
            .opPosition(DEFAULT_OP_POSITION)
            .channel(DEFAULT_CHANNEL)
            .comment(DEFAULT_COMMENT)
            .createTime(DEFAULT_CREATE_TIME)
            .updateTime(DEFAULT_UPDATE_TIME);
        return plan;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Plan createUpdatedEntity(EntityManager em) {
        Plan plan = new Plan()
            .planNumber(UPDATED_PLAN_NUMBER)
            .location(UPDATED_LOCATION)
            .workDay(UPDATED_WORK_DAY)
            .stockName(UPDATED_STOCK_NAME)
            .loadingStartTime(UPDATED_LOADING_START_TIME)
            .loadingEndTime(UPDATED_LOADING_END_TIME)
            .weightSum(UPDATED_WEIGHT_SUM)
            .operator(UPDATED_OPERATOR)
            .operation(UPDATED_OPERATION)
            .opPosition(UPDATED_OP_POSITION)
            .channel(UPDATED_CHANNEL)
            .comment(UPDATED_COMMENT)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME);
        return plan;
    }

    @BeforeEach
    public void initTest() {
        plan = createEntity(em);
    }

    @Test
    @Transactional
    public void createPlan() throws Exception {
        int databaseSizeBeforeCreate = planRepository.findAll().size();

        // Create the Plan
        PlanDTO planDTO = planMapper.toDto(plan);
        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(planDTO)))
            .andExpect(status().isCreated());

        // Validate the Plan in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeCreate + 1);
        Plan testPlan = planList.get(planList.size() - 1);
        assertThat(testPlan.getPlanNumber()).isEqualTo(DEFAULT_PLAN_NUMBER);
        assertThat(testPlan.getLocation()).isEqualTo(DEFAULT_LOCATION);
        assertThat(testPlan.getWorkDay()).isEqualTo(DEFAULT_WORK_DAY);
        assertThat(testPlan.getStockName()).isEqualTo(DEFAULT_STOCK_NAME);
        assertThat(testPlan.getLoadingStartTime()).isEqualTo(DEFAULT_LOADING_START_TIME);
        assertThat(testPlan.getLoadingEndTime()).isEqualTo(DEFAULT_LOADING_END_TIME);
        assertThat(testPlan.getWeightSum()).isEqualTo(DEFAULT_WEIGHT_SUM);
        assertThat(testPlan.getOperator()).isEqualTo(DEFAULT_OPERATOR);
        assertThat(testPlan.getOperation()).isEqualTo(DEFAULT_OPERATION);
        assertThat(testPlan.getOpPosition()).isEqualTo(DEFAULT_OP_POSITION);
        assertThat(testPlan.getChannel()).isEqualTo(DEFAULT_CHANNEL);
        assertThat(testPlan.getComment()).isEqualTo(DEFAULT_COMMENT);
        assertThat(testPlan.getCreateTime()).isEqualTo(DEFAULT_CREATE_TIME);
        assertThat(testPlan.getUpdateTime()).isEqualTo(DEFAULT_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void createPlanWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = planRepository.findAll().size();

        // Create the Plan with an existing ID
        plan.setId(1L);
        PlanDTO planDTO = planMapper.toDto(plan);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(planDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Plan in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkPlanNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRepository.findAll().size();
        // set the field null
        plan.setPlanNumber(null);

        // Create the Plan, which fails.
        PlanDTO planDTO = planMapper.toDto(plan);

        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(planDTO)))
            .andExpect(status().isBadRequest());

        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkWorkDayIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRepository.findAll().size();
        // set the field null
        plan.setWorkDay(null);

        // Create the Plan, which fails.
        PlanDTO planDTO = planMapper.toDto(plan);

        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(planDTO)))
            .andExpect(status().isBadRequest());

        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkWeightSumIsRequired() throws Exception {
        int databaseSizeBeforeTest = planRepository.findAll().size();
        // set the field null
        plan.setWeightSum(null);

        // Create the Plan, which fails.
        PlanDTO planDTO = planMapper.toDto(plan);

        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(planDTO)))
            .andExpect(status().isBadRequest());

        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPlans() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList
        restPlanMockMvc.perform(get("/api/plans?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(plan.getId().intValue())))
            .andExpect(jsonPath("$.[*].planNumber").value(hasItem(DEFAULT_PLAN_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].location").value(hasItem(DEFAULT_LOCATION.toString())))
            .andExpect(jsonPath("$.[*].workDay").value(hasItem(DEFAULT_WORK_DAY.toString())))
            .andExpect(jsonPath("$.[*].stockName").value(hasItem(DEFAULT_STOCK_NAME.toString())))
            .andExpect(jsonPath("$.[*].loadingStartTime").value(hasItem(sameInstant(DEFAULT_LOADING_START_TIME))))
            .andExpect(jsonPath("$.[*].loadingEndTime").value(hasItem(sameInstant(DEFAULT_LOADING_END_TIME))))
            .andExpect(jsonPath("$.[*].weightSum").value(hasItem(DEFAULT_WEIGHT_SUM.doubleValue())))
            .andExpect(jsonPath("$.[*].operator").value(hasItem(DEFAULT_OPERATOR.toString())))
            .andExpect(jsonPath("$.[*].operation").value(hasItem(DEFAULT_OPERATION.toString())))
            .andExpect(jsonPath("$.[*].opPosition").value(hasItem(DEFAULT_OP_POSITION.toString())))
            .andExpect(jsonPath("$.[*].channel").value(hasItem(DEFAULT_CHANNEL.toString())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))));
    }
    
    @Test
    @Transactional
    public void getPlan() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get the plan
        restPlanMockMvc.perform(get("/api/plans/{id}", plan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(plan.getId().intValue()))
            .andExpect(jsonPath("$.planNumber").value(DEFAULT_PLAN_NUMBER.toString()))
            .andExpect(jsonPath("$.location").value(DEFAULT_LOCATION.toString()))
            .andExpect(jsonPath("$.workDay").value(DEFAULT_WORK_DAY.toString()))
            .andExpect(jsonPath("$.stockName").value(DEFAULT_STOCK_NAME.toString()))
            .andExpect(jsonPath("$.loadingStartTime").value(sameInstant(DEFAULT_LOADING_START_TIME)))
            .andExpect(jsonPath("$.loadingEndTime").value(sameInstant(DEFAULT_LOADING_END_TIME)))
            .andExpect(jsonPath("$.weightSum").value(DEFAULT_WEIGHT_SUM.doubleValue()))
            .andExpect(jsonPath("$.operator").value(DEFAULT_OPERATOR.toString()))
            .andExpect(jsonPath("$.operation").value(DEFAULT_OPERATION.toString()))
            .andExpect(jsonPath("$.opPosition").value(DEFAULT_OP_POSITION.toString()))
            .andExpect(jsonPath("$.channel").value(DEFAULT_CHANNEL.toString()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT.toString()))
            .andExpect(jsonPath("$.createTime").value(sameInstant(DEFAULT_CREATE_TIME)))
            .andExpect(jsonPath("$.updateTime").value(sameInstant(DEFAULT_UPDATE_TIME)));
    }

    @Test
    @Transactional
    public void getAllPlansByPlanNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where planNumber equals to DEFAULT_PLAN_NUMBER
        defaultPlanShouldBeFound("planNumber.equals=" + DEFAULT_PLAN_NUMBER);

        // Get all the planList where planNumber equals to UPDATED_PLAN_NUMBER
        defaultPlanShouldNotBeFound("planNumber.equals=" + UPDATED_PLAN_NUMBER);
    }

    @Test
    @Transactional
    public void getAllPlansByPlanNumberIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where planNumber in DEFAULT_PLAN_NUMBER or UPDATED_PLAN_NUMBER
        defaultPlanShouldBeFound("planNumber.in=" + DEFAULT_PLAN_NUMBER + "," + UPDATED_PLAN_NUMBER);

        // Get all the planList where planNumber equals to UPDATED_PLAN_NUMBER
        defaultPlanShouldNotBeFound("planNumber.in=" + UPDATED_PLAN_NUMBER);
    }

    @Test
    @Transactional
    public void getAllPlansByPlanNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where planNumber is not null
        defaultPlanShouldBeFound("planNumber.specified=true");

        // Get all the planList where planNumber is null
        defaultPlanShouldNotBeFound("planNumber.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByLocationIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where location equals to DEFAULT_LOCATION
        defaultPlanShouldBeFound("location.equals=" + DEFAULT_LOCATION);

        // Get all the planList where location equals to UPDATED_LOCATION
        defaultPlanShouldNotBeFound("location.equals=" + UPDATED_LOCATION);
    }

    @Test
    @Transactional
    public void getAllPlansByLocationIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where location in DEFAULT_LOCATION or UPDATED_LOCATION
        defaultPlanShouldBeFound("location.in=" + DEFAULT_LOCATION + "," + UPDATED_LOCATION);

        // Get all the planList where location equals to UPDATED_LOCATION
        defaultPlanShouldNotBeFound("location.in=" + UPDATED_LOCATION);
    }

    @Test
    @Transactional
    public void getAllPlansByLocationIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where location is not null
        defaultPlanShouldBeFound("location.specified=true");

        // Get all the planList where location is null
        defaultPlanShouldNotBeFound("location.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByWorkDayIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where workDay equals to DEFAULT_WORK_DAY
        defaultPlanShouldBeFound("workDay.equals=" + DEFAULT_WORK_DAY);

        // Get all the planList where workDay equals to UPDATED_WORK_DAY
        defaultPlanShouldNotBeFound("workDay.equals=" + UPDATED_WORK_DAY);
    }

    @Test
    @Transactional
    public void getAllPlansByWorkDayIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where workDay in DEFAULT_WORK_DAY or UPDATED_WORK_DAY
        defaultPlanShouldBeFound("workDay.in=" + DEFAULT_WORK_DAY + "," + UPDATED_WORK_DAY);

        // Get all the planList where workDay equals to UPDATED_WORK_DAY
        defaultPlanShouldNotBeFound("workDay.in=" + UPDATED_WORK_DAY);
    }

    @Test
    @Transactional
    public void getAllPlansByWorkDayIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where workDay is not null
        defaultPlanShouldBeFound("workDay.specified=true");

        // Get all the planList where workDay is null
        defaultPlanShouldNotBeFound("workDay.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByWorkDayIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where workDay greater than or equals to DEFAULT_WORK_DAY
        defaultPlanShouldBeFound("workDay.greaterOrEqualThan=" + DEFAULT_WORK_DAY);

        // Get all the planList where workDay greater than or equals to UPDATED_WORK_DAY
        defaultPlanShouldNotBeFound("workDay.greaterOrEqualThan=" + UPDATED_WORK_DAY);
    }

    @Test
    @Transactional
    public void getAllPlansByWorkDayIsLessThanSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where workDay less than or equals to DEFAULT_WORK_DAY
        defaultPlanShouldNotBeFound("workDay.lessThan=" + DEFAULT_WORK_DAY);

        // Get all the planList where workDay less than or equals to UPDATED_WORK_DAY
        defaultPlanShouldBeFound("workDay.lessThan=" + UPDATED_WORK_DAY);
    }


    @Test
    @Transactional
    public void getAllPlansByStockNameIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where stockName equals to DEFAULT_STOCK_NAME
        defaultPlanShouldBeFound("stockName.equals=" + DEFAULT_STOCK_NAME);

        // Get all the planList where stockName equals to UPDATED_STOCK_NAME
        defaultPlanShouldNotBeFound("stockName.equals=" + UPDATED_STOCK_NAME);
    }

    @Test
    @Transactional
    public void getAllPlansByStockNameIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where stockName in DEFAULT_STOCK_NAME or UPDATED_STOCK_NAME
        defaultPlanShouldBeFound("stockName.in=" + DEFAULT_STOCK_NAME + "," + UPDATED_STOCK_NAME);

        // Get all the planList where stockName equals to UPDATED_STOCK_NAME
        defaultPlanShouldNotBeFound("stockName.in=" + UPDATED_STOCK_NAME);
    }

    @Test
    @Transactional
    public void getAllPlansByStockNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where stockName is not null
        defaultPlanShouldBeFound("stockName.specified=true");

        // Get all the planList where stockName is null
        defaultPlanShouldNotBeFound("stockName.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByLoadingStartTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where loadingStartTime equals to DEFAULT_LOADING_START_TIME
        defaultPlanShouldBeFound("loadingStartTime.equals=" + DEFAULT_LOADING_START_TIME);

        // Get all the planList where loadingStartTime equals to UPDATED_LOADING_START_TIME
        defaultPlanShouldNotBeFound("loadingStartTime.equals=" + UPDATED_LOADING_START_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByLoadingStartTimeIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where loadingStartTime in DEFAULT_LOADING_START_TIME or UPDATED_LOADING_START_TIME
        defaultPlanShouldBeFound("loadingStartTime.in=" + DEFAULT_LOADING_START_TIME + "," + UPDATED_LOADING_START_TIME);

        // Get all the planList where loadingStartTime equals to UPDATED_LOADING_START_TIME
        defaultPlanShouldNotBeFound("loadingStartTime.in=" + UPDATED_LOADING_START_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByLoadingStartTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where loadingStartTime is not null
        defaultPlanShouldBeFound("loadingStartTime.specified=true");

        // Get all the planList where loadingStartTime is null
        defaultPlanShouldNotBeFound("loadingStartTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByLoadingStartTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where loadingStartTime greater than or equals to DEFAULT_LOADING_START_TIME
        defaultPlanShouldBeFound("loadingStartTime.greaterOrEqualThan=" + DEFAULT_LOADING_START_TIME);

        // Get all the planList where loadingStartTime greater than or equals to UPDATED_LOADING_START_TIME
        defaultPlanShouldNotBeFound("loadingStartTime.greaterOrEqualThan=" + UPDATED_LOADING_START_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByLoadingStartTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where loadingStartTime less than or equals to DEFAULT_LOADING_START_TIME
        defaultPlanShouldNotBeFound("loadingStartTime.lessThan=" + DEFAULT_LOADING_START_TIME);

        // Get all the planList where loadingStartTime less than or equals to UPDATED_LOADING_START_TIME
        defaultPlanShouldBeFound("loadingStartTime.lessThan=" + UPDATED_LOADING_START_TIME);
    }


    @Test
    @Transactional
    public void getAllPlansByLoadingEndTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where loadingEndTime equals to DEFAULT_LOADING_END_TIME
        defaultPlanShouldBeFound("loadingEndTime.equals=" + DEFAULT_LOADING_END_TIME);

        // Get all the planList where loadingEndTime equals to UPDATED_LOADING_END_TIME
        defaultPlanShouldNotBeFound("loadingEndTime.equals=" + UPDATED_LOADING_END_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByLoadingEndTimeIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where loadingEndTime in DEFAULT_LOADING_END_TIME or UPDATED_LOADING_END_TIME
        defaultPlanShouldBeFound("loadingEndTime.in=" + DEFAULT_LOADING_END_TIME + "," + UPDATED_LOADING_END_TIME);

        // Get all the planList where loadingEndTime equals to UPDATED_LOADING_END_TIME
        defaultPlanShouldNotBeFound("loadingEndTime.in=" + UPDATED_LOADING_END_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByLoadingEndTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where loadingEndTime is not null
        defaultPlanShouldBeFound("loadingEndTime.specified=true");

        // Get all the planList where loadingEndTime is null
        defaultPlanShouldNotBeFound("loadingEndTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByLoadingEndTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where loadingEndTime greater than or equals to DEFAULT_LOADING_END_TIME
        defaultPlanShouldBeFound("loadingEndTime.greaterOrEqualThan=" + DEFAULT_LOADING_END_TIME);

        // Get all the planList where loadingEndTime greater than or equals to UPDATED_LOADING_END_TIME
        defaultPlanShouldNotBeFound("loadingEndTime.greaterOrEqualThan=" + UPDATED_LOADING_END_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByLoadingEndTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where loadingEndTime less than or equals to DEFAULT_LOADING_END_TIME
        defaultPlanShouldNotBeFound("loadingEndTime.lessThan=" + DEFAULT_LOADING_END_TIME);

        // Get all the planList where loadingEndTime less than or equals to UPDATED_LOADING_END_TIME
        defaultPlanShouldBeFound("loadingEndTime.lessThan=" + UPDATED_LOADING_END_TIME);
    }


    @Test
    @Transactional
    public void getAllPlansByWeightSumIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where weightSum equals to DEFAULT_WEIGHT_SUM
        defaultPlanShouldBeFound("weightSum.equals=" + DEFAULT_WEIGHT_SUM);

        // Get all the planList where weightSum equals to UPDATED_WEIGHT_SUM
        defaultPlanShouldNotBeFound("weightSum.equals=" + UPDATED_WEIGHT_SUM);
    }

    @Test
    @Transactional
    public void getAllPlansByWeightSumIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where weightSum in DEFAULT_WEIGHT_SUM or UPDATED_WEIGHT_SUM
        defaultPlanShouldBeFound("weightSum.in=" + DEFAULT_WEIGHT_SUM + "," + UPDATED_WEIGHT_SUM);

        // Get all the planList where weightSum equals to UPDATED_WEIGHT_SUM
        defaultPlanShouldNotBeFound("weightSum.in=" + UPDATED_WEIGHT_SUM);
    }

    @Test
    @Transactional
    public void getAllPlansByWeightSumIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where weightSum is not null
        defaultPlanShouldBeFound("weightSum.specified=true");

        // Get all the planList where weightSum is null
        defaultPlanShouldNotBeFound("weightSum.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByOperatorIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where operator equals to DEFAULT_OPERATOR
        defaultPlanShouldBeFound("operator.equals=" + DEFAULT_OPERATOR);

        // Get all the planList where operator equals to UPDATED_OPERATOR
        defaultPlanShouldNotBeFound("operator.equals=" + UPDATED_OPERATOR);
    }

    @Test
    @Transactional
    public void getAllPlansByOperatorIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where operator in DEFAULT_OPERATOR or UPDATED_OPERATOR
        defaultPlanShouldBeFound("operator.in=" + DEFAULT_OPERATOR + "," + UPDATED_OPERATOR);

        // Get all the planList where operator equals to UPDATED_OPERATOR
        defaultPlanShouldNotBeFound("operator.in=" + UPDATED_OPERATOR);
    }

    @Test
    @Transactional
    public void getAllPlansByOperatorIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where operator is not null
        defaultPlanShouldBeFound("operator.specified=true");

        // Get all the planList where operator is null
        defaultPlanShouldNotBeFound("operator.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByOperationIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where operation equals to DEFAULT_OPERATION
        defaultPlanShouldBeFound("operation.equals=" + DEFAULT_OPERATION);

        // Get all the planList where operation equals to UPDATED_OPERATION
        defaultPlanShouldNotBeFound("operation.equals=" + UPDATED_OPERATION);
    }

    @Test
    @Transactional
    public void getAllPlansByOperationIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where operation in DEFAULT_OPERATION or UPDATED_OPERATION
        defaultPlanShouldBeFound("operation.in=" + DEFAULT_OPERATION + "," + UPDATED_OPERATION);

        // Get all the planList where operation equals to UPDATED_OPERATION
        defaultPlanShouldNotBeFound("operation.in=" + UPDATED_OPERATION);
    }

    @Test
    @Transactional
    public void getAllPlansByOperationIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where operation is not null
        defaultPlanShouldBeFound("operation.specified=true");

        // Get all the planList where operation is null
        defaultPlanShouldNotBeFound("operation.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByOpPositionIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where opPosition equals to DEFAULT_OP_POSITION
        defaultPlanShouldBeFound("opPosition.equals=" + DEFAULT_OP_POSITION);

        // Get all the planList where opPosition equals to UPDATED_OP_POSITION
        defaultPlanShouldNotBeFound("opPosition.equals=" + UPDATED_OP_POSITION);
    }

    @Test
    @Transactional
    public void getAllPlansByOpPositionIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where opPosition in DEFAULT_OP_POSITION or UPDATED_OP_POSITION
        defaultPlanShouldBeFound("opPosition.in=" + DEFAULT_OP_POSITION + "," + UPDATED_OP_POSITION);

        // Get all the planList where opPosition equals to UPDATED_OP_POSITION
        defaultPlanShouldNotBeFound("opPosition.in=" + UPDATED_OP_POSITION);
    }

    @Test
    @Transactional
    public void getAllPlansByOpPositionIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where opPosition is not null
        defaultPlanShouldBeFound("opPosition.specified=true");

        // Get all the planList where opPosition is null
        defaultPlanShouldNotBeFound("opPosition.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByChannelIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where channel equals to DEFAULT_CHANNEL
        defaultPlanShouldBeFound("channel.equals=" + DEFAULT_CHANNEL);

        // Get all the planList where channel equals to UPDATED_CHANNEL
        defaultPlanShouldNotBeFound("channel.equals=" + UPDATED_CHANNEL);
    }

    @Test
    @Transactional
    public void getAllPlansByChannelIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where channel in DEFAULT_CHANNEL or UPDATED_CHANNEL
        defaultPlanShouldBeFound("channel.in=" + DEFAULT_CHANNEL + "," + UPDATED_CHANNEL);

        // Get all the planList where channel equals to UPDATED_CHANNEL
        defaultPlanShouldNotBeFound("channel.in=" + UPDATED_CHANNEL);
    }

    @Test
    @Transactional
    public void getAllPlansByChannelIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where channel is not null
        defaultPlanShouldBeFound("channel.specified=true");

        // Get all the planList where channel is null
        defaultPlanShouldNotBeFound("channel.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByCommentIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where comment equals to DEFAULT_COMMENT
        defaultPlanShouldBeFound("comment.equals=" + DEFAULT_COMMENT);

        // Get all the planList where comment equals to UPDATED_COMMENT
        defaultPlanShouldNotBeFound("comment.equals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllPlansByCommentIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where comment in DEFAULT_COMMENT or UPDATED_COMMENT
        defaultPlanShouldBeFound("comment.in=" + DEFAULT_COMMENT + "," + UPDATED_COMMENT);

        // Get all the planList where comment equals to UPDATED_COMMENT
        defaultPlanShouldNotBeFound("comment.in=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllPlansByCommentIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where comment is not null
        defaultPlanShouldBeFound("comment.specified=true");

        // Get all the planList where comment is null
        defaultPlanShouldNotBeFound("comment.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByCreateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where createTime equals to DEFAULT_CREATE_TIME
        defaultPlanShouldBeFound("createTime.equals=" + DEFAULT_CREATE_TIME);

        // Get all the planList where createTime equals to UPDATED_CREATE_TIME
        defaultPlanShouldNotBeFound("createTime.equals=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByCreateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where createTime in DEFAULT_CREATE_TIME or UPDATED_CREATE_TIME
        defaultPlanShouldBeFound("createTime.in=" + DEFAULT_CREATE_TIME + "," + UPDATED_CREATE_TIME);

        // Get all the planList where createTime equals to UPDATED_CREATE_TIME
        defaultPlanShouldNotBeFound("createTime.in=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByCreateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where createTime is not null
        defaultPlanShouldBeFound("createTime.specified=true");

        // Get all the planList where createTime is null
        defaultPlanShouldNotBeFound("createTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByCreateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where createTime greater than or equals to DEFAULT_CREATE_TIME
        defaultPlanShouldBeFound("createTime.greaterOrEqualThan=" + DEFAULT_CREATE_TIME);

        // Get all the planList where createTime greater than or equals to UPDATED_CREATE_TIME
        defaultPlanShouldNotBeFound("createTime.greaterOrEqualThan=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByCreateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where createTime less than or equals to DEFAULT_CREATE_TIME
        defaultPlanShouldNotBeFound("createTime.lessThan=" + DEFAULT_CREATE_TIME);

        // Get all the planList where createTime less than or equals to UPDATED_CREATE_TIME
        defaultPlanShouldBeFound("createTime.lessThan=" + UPDATED_CREATE_TIME);
    }


    @Test
    @Transactional
    public void getAllPlansByUpdateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where updateTime equals to DEFAULT_UPDATE_TIME
        defaultPlanShouldBeFound("updateTime.equals=" + DEFAULT_UPDATE_TIME);

        // Get all the planList where updateTime equals to UPDATED_UPDATE_TIME
        defaultPlanShouldNotBeFound("updateTime.equals=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByUpdateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where updateTime in DEFAULT_UPDATE_TIME or UPDATED_UPDATE_TIME
        defaultPlanShouldBeFound("updateTime.in=" + DEFAULT_UPDATE_TIME + "," + UPDATED_UPDATE_TIME);

        // Get all the planList where updateTime equals to UPDATED_UPDATE_TIME
        defaultPlanShouldNotBeFound("updateTime.in=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByUpdateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where updateTime is not null
        defaultPlanShouldBeFound("updateTime.specified=true");

        // Get all the planList where updateTime is null
        defaultPlanShouldNotBeFound("updateTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByUpdateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where updateTime greater than or equals to DEFAULT_UPDATE_TIME
        defaultPlanShouldBeFound("updateTime.greaterOrEqualThan=" + DEFAULT_UPDATE_TIME);

        // Get all the planList where updateTime greater than or equals to UPDATED_UPDATE_TIME
        defaultPlanShouldNotBeFound("updateTime.greaterOrEqualThan=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllPlansByUpdateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where updateTime less than or equals to DEFAULT_UPDATE_TIME
        defaultPlanShouldNotBeFound("updateTime.lessThan=" + DEFAULT_UPDATE_TIME);

        // Get all the planList where updateTime less than or equals to UPDATED_UPDATE_TIME
        defaultPlanShouldBeFound("updateTime.lessThan=" + UPDATED_UPDATE_TIME);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPlanShouldBeFound(String filter) throws Exception {
        restPlanMockMvc.perform(get("/api/plans?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(plan.getId().intValue())))
            .andExpect(jsonPath("$.[*].planNumber").value(hasItem(DEFAULT_PLAN_NUMBER)))
            .andExpect(jsonPath("$.[*].location").value(hasItem(DEFAULT_LOCATION)))
            .andExpect(jsonPath("$.[*].workDay").value(hasItem(DEFAULT_WORK_DAY.toString())))
            .andExpect(jsonPath("$.[*].stockName").value(hasItem(DEFAULT_STOCK_NAME)))
            .andExpect(jsonPath("$.[*].loadingStartTime").value(hasItem(sameInstant(DEFAULT_LOADING_START_TIME))))
            .andExpect(jsonPath("$.[*].loadingEndTime").value(hasItem(sameInstant(DEFAULT_LOADING_END_TIME))))
            .andExpect(jsonPath("$.[*].weightSum").value(hasItem(DEFAULT_WEIGHT_SUM.doubleValue())))
            .andExpect(jsonPath("$.[*].operator").value(hasItem(DEFAULT_OPERATOR)))
            .andExpect(jsonPath("$.[*].operation").value(hasItem(DEFAULT_OPERATION)))
            .andExpect(jsonPath("$.[*].opPosition").value(hasItem(DEFAULT_OP_POSITION)))
            .andExpect(jsonPath("$.[*].channel").value(hasItem(DEFAULT_CHANNEL)))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))));

        // Check, that the count call also returns 1
        restPlanMockMvc.perform(get("/api/plans/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPlanShouldNotBeFound(String filter) throws Exception {
        restPlanMockMvc.perform(get("/api/plans?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPlanMockMvc.perform(get("/api/plans/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingPlan() throws Exception {
        // Get the plan
        restPlanMockMvc.perform(get("/api/plans/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePlan() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        int databaseSizeBeforeUpdate = planRepository.findAll().size();

        // Update the plan
        Plan updatedPlan = planRepository.findById(plan.getId()).get();
        // Disconnect from session so that the updates on updatedPlan are not directly saved in db
        em.detach(updatedPlan);
        updatedPlan
            .planNumber(UPDATED_PLAN_NUMBER)
            .location(UPDATED_LOCATION)
            .workDay(UPDATED_WORK_DAY)
            .stockName(UPDATED_STOCK_NAME)
            .loadingStartTime(UPDATED_LOADING_START_TIME)
            .loadingEndTime(UPDATED_LOADING_END_TIME)
            .weightSum(UPDATED_WEIGHT_SUM)
            .operator(UPDATED_OPERATOR)
            .operation(UPDATED_OPERATION)
            .opPosition(UPDATED_OP_POSITION)
            .channel(UPDATED_CHANNEL)
            .comment(UPDATED_COMMENT)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME);
        PlanDTO planDTO = planMapper.toDto(updatedPlan);

        restPlanMockMvc.perform(put("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(planDTO)))
            .andExpect(status().isOk());

        // Validate the Plan in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeUpdate);
        Plan testPlan = planList.get(planList.size() - 1);
        assertThat(testPlan.getPlanNumber()).isEqualTo(UPDATED_PLAN_NUMBER);
        assertThat(testPlan.getLocation()).isEqualTo(UPDATED_LOCATION);
        assertThat(testPlan.getWorkDay()).isEqualTo(UPDATED_WORK_DAY);
        assertThat(testPlan.getStockName()).isEqualTo(UPDATED_STOCK_NAME);
        assertThat(testPlan.getLoadingStartTime()).isEqualTo(UPDATED_LOADING_START_TIME);
        assertThat(testPlan.getLoadingEndTime()).isEqualTo(UPDATED_LOADING_END_TIME);
        assertThat(testPlan.getWeightSum()).isEqualTo(UPDATED_WEIGHT_SUM);
        assertThat(testPlan.getOperator()).isEqualTo(UPDATED_OPERATOR);
        assertThat(testPlan.getOperation()).isEqualTo(UPDATED_OPERATION);
        assertThat(testPlan.getOpPosition()).isEqualTo(UPDATED_OP_POSITION);
        assertThat(testPlan.getChannel()).isEqualTo(UPDATED_CHANNEL);
        assertThat(testPlan.getComment()).isEqualTo(UPDATED_COMMENT);
        assertThat(testPlan.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testPlan.getUpdateTime()).isEqualTo(UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void updateNonExistingPlan() throws Exception {
        int databaseSizeBeforeUpdate = planRepository.findAll().size();

        // Create the Plan
        PlanDTO planDTO = planMapper.toDto(plan);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPlanMockMvc.perform(put("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(planDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Plan in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deletePlan() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        int databaseSizeBeforeDelete = planRepository.findAll().size();

        // Delete the plan
        restPlanMockMvc.perform(delete("/api/plans/{id}", plan.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Plan.class);
        Plan plan1 = new Plan();
        plan1.setId(1L);
        Plan plan2 = new Plan();
        plan2.setId(plan1.getId());
        assertThat(plan1).isEqualTo(plan2);
        plan2.setId(2L);
        assertThat(plan1).isNotEqualTo(plan2);
        plan1.setId(null);
        assertThat(plan1).isNotEqualTo(plan2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PlanDTO.class);
        PlanDTO planDTO1 = new PlanDTO();
        planDTO1.setId(1L);
        PlanDTO planDTO2 = new PlanDTO();
        assertThat(planDTO1).isNotEqualTo(planDTO2);
        planDTO2.setId(planDTO1.getId());
        assertThat(planDTO1).isEqualTo(planDTO2);
        planDTO2.setId(2L);
        assertThat(planDTO1).isNotEqualTo(planDTO2);
        planDTO1.setId(null);
        assertThat(planDTO1).isNotEqualTo(planDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(planMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(planMapper.fromId(null)).isNull();
    }
}
