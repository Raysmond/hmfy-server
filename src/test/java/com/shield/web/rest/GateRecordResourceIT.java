package com.shield.web.rest;

import com.shield.ShieldApp;
import com.shield.domain.GateRecord;
import com.shield.repository.GateRecordRepository;
import com.shield.service.GateRecordService;
import com.shield.service.dto.GateRecordDTO;
import com.shield.service.mapper.GateRecordMapper;
import com.shield.web.rest.errors.ExceptionTranslator;
import com.shield.service.dto.GateRecordCriteria;
import com.shield.service.GateRecordQueryService;

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
import org.springframework.util.Base64Utils;
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

import com.shield.domain.enumeration.RecordType;
/**
 * Integration tests for the {@Link GateRecordResource} REST controller.
 */
@SpringBootTest(classes = ShieldApp.class)
public class GateRecordResourceIT {

    private static final RecordType DEFAULT_RECORD_TYPE = RecordType.IN;
    private static final RecordType UPDATED_RECORD_TYPE = RecordType.OUT;

    private static final String DEFAULT_TRUCK_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_TRUCK_NUMBER = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_RECORD_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_RECORD_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_DATA = "AAAAAAAAAA";
    private static final String UPDATED_DATA = "BBBBBBBBBB";

    private static final String DEFAULT_RID = "AAAAAAAAAA";
    private static final String UPDATED_RID = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Long DEFAULT_REGION_ID = 1L;
    private static final Long UPDATED_REGION_ID = 2L;

    private static final String DEFAULT_DATA_MD_5 = "AAAAAAAAAA";
    private static final String UPDATED_DATA_MD_5 = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_MODIFY_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_MODIFY_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private GateRecordRepository gateRecordRepository;

    @Autowired
    private GateRecordMapper gateRecordMapper;

    @Autowired
    private GateRecordService gateRecordService;

    @Autowired
    private GateRecordQueryService gateRecordQueryService;

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

    private MockMvc restGateRecordMockMvc;

    private GateRecord gateRecord;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final GateRecordResource gateRecordResource = new GateRecordResource(gateRecordService, gateRecordQueryService);
        this.restGateRecordMockMvc = MockMvcBuilders.standaloneSetup(gateRecordResource)
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
    public static GateRecord createEntity(EntityManager em) {
        GateRecord gateRecord = new GateRecord()
            .recordType(DEFAULT_RECORD_TYPE)
            .truckNumber(DEFAULT_TRUCK_NUMBER)
            .recordTime(DEFAULT_RECORD_TIME)
            .data(DEFAULT_DATA)
            .rid(DEFAULT_RID)
            .createTime(DEFAULT_CREATE_TIME)
            .regionId(DEFAULT_REGION_ID)
            .dataMd5(DEFAULT_DATA_MD_5)
            .modifyTime(DEFAULT_MODIFY_TIME);
        return gateRecord;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GateRecord createUpdatedEntity(EntityManager em) {
        GateRecord gateRecord = new GateRecord()
            .recordType(UPDATED_RECORD_TYPE)
            .truckNumber(UPDATED_TRUCK_NUMBER)
            .recordTime(UPDATED_RECORD_TIME)
            .data(UPDATED_DATA)
            .rid(UPDATED_RID)
            .createTime(UPDATED_CREATE_TIME)
            .regionId(UPDATED_REGION_ID)
            .dataMd5(UPDATED_DATA_MD_5)
            .modifyTime(UPDATED_MODIFY_TIME);
        return gateRecord;
    }

    @BeforeEach
    public void initTest() {
        gateRecord = createEntity(em);
    }

    @Test
    @Transactional
    public void createGateRecord() throws Exception {
        int databaseSizeBeforeCreate = gateRecordRepository.findAll().size();

        // Create the GateRecord
        GateRecordDTO gateRecordDTO = gateRecordMapper.toDto(gateRecord);
        restGateRecordMockMvc.perform(post("/api/gate-records")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gateRecordDTO)))
            .andExpect(status().isCreated());

        // Validate the GateRecord in the database
        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeCreate + 1);
        GateRecord testGateRecord = gateRecordList.get(gateRecordList.size() - 1);
        assertThat(testGateRecord.getRecordType()).isEqualTo(DEFAULT_RECORD_TYPE);
        assertThat(testGateRecord.getTruckNumber()).isEqualTo(DEFAULT_TRUCK_NUMBER);
        assertThat(testGateRecord.getRecordTime()).isEqualTo(DEFAULT_RECORD_TIME);
        assertThat(testGateRecord.getData()).isEqualTo(DEFAULT_DATA);
        assertThat(testGateRecord.getRid()).isEqualTo(DEFAULT_RID);
        assertThat(testGateRecord.getCreateTime()).isEqualTo(DEFAULT_CREATE_TIME);
        assertThat(testGateRecord.getRegionId()).isEqualTo(DEFAULT_REGION_ID);
        assertThat(testGateRecord.getDataMd5()).isEqualTo(DEFAULT_DATA_MD_5);
        assertThat(testGateRecord.getModifyTime()).isEqualTo(DEFAULT_MODIFY_TIME);
    }

    @Test
    @Transactional
    public void createGateRecordWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = gateRecordRepository.findAll().size();

        // Create the GateRecord with an existing ID
        gateRecord.setId(1L);
        GateRecordDTO gateRecordDTO = gateRecordMapper.toDto(gateRecord);

        // An entity with an existing ID cannot be created, so this API call must fail
        restGateRecordMockMvc.perform(post("/api/gate-records")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gateRecordDTO)))
            .andExpect(status().isBadRequest());

        // Validate the GateRecord in the database
        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkRecordTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = gateRecordRepository.findAll().size();
        // set the field null
        gateRecord.setRecordType(null);

        // Create the GateRecord, which fails.
        GateRecordDTO gateRecordDTO = gateRecordMapper.toDto(gateRecord);

        restGateRecordMockMvc.perform(post("/api/gate-records")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gateRecordDTO)))
            .andExpect(status().isBadRequest());

        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTruckNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = gateRecordRepository.findAll().size();
        // set the field null
        gateRecord.setTruckNumber(null);

        // Create the GateRecord, which fails.
        GateRecordDTO gateRecordDTO = gateRecordMapper.toDto(gateRecord);

        restGateRecordMockMvc.perform(post("/api/gate-records")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gateRecordDTO)))
            .andExpect(status().isBadRequest());

        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRecordTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = gateRecordRepository.findAll().size();
        // set the field null
        gateRecord.setRecordTime(null);

        // Create the GateRecord, which fails.
        GateRecordDTO gateRecordDTO = gateRecordMapper.toDto(gateRecord);

        restGateRecordMockMvc.perform(post("/api/gate-records")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gateRecordDTO)))
            .andExpect(status().isBadRequest());

        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRidIsRequired() throws Exception {
        int databaseSizeBeforeTest = gateRecordRepository.findAll().size();
        // set the field null
        gateRecord.setRid(null);

        // Create the GateRecord, which fails.
        GateRecordDTO gateRecordDTO = gateRecordMapper.toDto(gateRecord);

        restGateRecordMockMvc.perform(post("/api/gate-records")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gateRecordDTO)))
            .andExpect(status().isBadRequest());

        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCreateTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = gateRecordRepository.findAll().size();
        // set the field null
        gateRecord.setCreateTime(null);

        // Create the GateRecord, which fails.
        GateRecordDTO gateRecordDTO = gateRecordMapper.toDto(gateRecord);

        restGateRecordMockMvc.perform(post("/api/gate-records")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gateRecordDTO)))
            .andExpect(status().isBadRequest());

        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRegionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = gateRecordRepository.findAll().size();
        // set the field null
        gateRecord.setRegionId(null);

        // Create the GateRecord, which fails.
        GateRecordDTO gateRecordDTO = gateRecordMapper.toDto(gateRecord);

        restGateRecordMockMvc.perform(post("/api/gate-records")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gateRecordDTO)))
            .andExpect(status().isBadRequest());

        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllGateRecords() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList
        restGateRecordMockMvc.perform(get("/api/gate-records?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gateRecord.getId().intValue())))
            .andExpect(jsonPath("$.[*].recordType").value(hasItem(DEFAULT_RECORD_TYPE.toString())))
            .andExpect(jsonPath("$.[*].truckNumber").value(hasItem(DEFAULT_TRUCK_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].recordTime").value(hasItem(sameInstant(DEFAULT_RECORD_TIME))))
            .andExpect(jsonPath("$.[*].data").value(hasItem(DEFAULT_DATA.toString())))
            .andExpect(jsonPath("$.[*].rid").value(hasItem(DEFAULT_RID.toString())))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].regionId").value(hasItem(DEFAULT_REGION_ID.intValue())))
            .andExpect(jsonPath("$.[*].dataMd5").value(hasItem(DEFAULT_DATA_MD_5.toString())))
            .andExpect(jsonPath("$.[*].modifyTime").value(hasItem(sameInstant(DEFAULT_MODIFY_TIME))));
    }
    
    @Test
    @Transactional
    public void getGateRecord() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get the gateRecord
        restGateRecordMockMvc.perform(get("/api/gate-records/{id}", gateRecord.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(gateRecord.getId().intValue()))
            .andExpect(jsonPath("$.recordType").value(DEFAULT_RECORD_TYPE.toString()))
            .andExpect(jsonPath("$.truckNumber").value(DEFAULT_TRUCK_NUMBER.toString()))
            .andExpect(jsonPath("$.recordTime").value(sameInstant(DEFAULT_RECORD_TIME)))
            .andExpect(jsonPath("$.data").value(DEFAULT_DATA.toString()))
            .andExpect(jsonPath("$.rid").value(DEFAULT_RID.toString()))
            .andExpect(jsonPath("$.createTime").value(sameInstant(DEFAULT_CREATE_TIME)))
            .andExpect(jsonPath("$.regionId").value(DEFAULT_REGION_ID.intValue()))
            .andExpect(jsonPath("$.dataMd5").value(DEFAULT_DATA_MD_5.toString()))
            .andExpect(jsonPath("$.modifyTime").value(sameInstant(DEFAULT_MODIFY_TIME)));
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRecordTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where recordType equals to DEFAULT_RECORD_TYPE
        defaultGateRecordShouldBeFound("recordType.equals=" + DEFAULT_RECORD_TYPE);

        // Get all the gateRecordList where recordType equals to UPDATED_RECORD_TYPE
        defaultGateRecordShouldNotBeFound("recordType.equals=" + UPDATED_RECORD_TYPE);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRecordTypeIsInShouldWork() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where recordType in DEFAULT_RECORD_TYPE or UPDATED_RECORD_TYPE
        defaultGateRecordShouldBeFound("recordType.in=" + DEFAULT_RECORD_TYPE + "," + UPDATED_RECORD_TYPE);

        // Get all the gateRecordList where recordType equals to UPDATED_RECORD_TYPE
        defaultGateRecordShouldNotBeFound("recordType.in=" + UPDATED_RECORD_TYPE);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRecordTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where recordType is not null
        defaultGateRecordShouldBeFound("recordType.specified=true");

        // Get all the gateRecordList where recordType is null
        defaultGateRecordShouldNotBeFound("recordType.specified=false");
    }

    @Test
    @Transactional
    public void getAllGateRecordsByTruckNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where truckNumber equals to DEFAULT_TRUCK_NUMBER
        defaultGateRecordShouldBeFound("truckNumber.equals=" + DEFAULT_TRUCK_NUMBER);

        // Get all the gateRecordList where truckNumber equals to UPDATED_TRUCK_NUMBER
        defaultGateRecordShouldNotBeFound("truckNumber.equals=" + UPDATED_TRUCK_NUMBER);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByTruckNumberIsInShouldWork() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where truckNumber in DEFAULT_TRUCK_NUMBER or UPDATED_TRUCK_NUMBER
        defaultGateRecordShouldBeFound("truckNumber.in=" + DEFAULT_TRUCK_NUMBER + "," + UPDATED_TRUCK_NUMBER);

        // Get all the gateRecordList where truckNumber equals to UPDATED_TRUCK_NUMBER
        defaultGateRecordShouldNotBeFound("truckNumber.in=" + UPDATED_TRUCK_NUMBER);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByTruckNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where truckNumber is not null
        defaultGateRecordShouldBeFound("truckNumber.specified=true");

        // Get all the gateRecordList where truckNumber is null
        defaultGateRecordShouldNotBeFound("truckNumber.specified=false");
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRecordTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where recordTime equals to DEFAULT_RECORD_TIME
        defaultGateRecordShouldBeFound("recordTime.equals=" + DEFAULT_RECORD_TIME);

        // Get all the gateRecordList where recordTime equals to UPDATED_RECORD_TIME
        defaultGateRecordShouldNotBeFound("recordTime.equals=" + UPDATED_RECORD_TIME);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRecordTimeIsInShouldWork() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where recordTime in DEFAULT_RECORD_TIME or UPDATED_RECORD_TIME
        defaultGateRecordShouldBeFound("recordTime.in=" + DEFAULT_RECORD_TIME + "," + UPDATED_RECORD_TIME);

        // Get all the gateRecordList where recordTime equals to UPDATED_RECORD_TIME
        defaultGateRecordShouldNotBeFound("recordTime.in=" + UPDATED_RECORD_TIME);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRecordTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where recordTime is not null
        defaultGateRecordShouldBeFound("recordTime.specified=true");

        // Get all the gateRecordList where recordTime is null
        defaultGateRecordShouldNotBeFound("recordTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRecordTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where recordTime greater than or equals to DEFAULT_RECORD_TIME
        defaultGateRecordShouldBeFound("recordTime.greaterOrEqualThan=" + DEFAULT_RECORD_TIME);

        // Get all the gateRecordList where recordTime greater than or equals to UPDATED_RECORD_TIME
        defaultGateRecordShouldNotBeFound("recordTime.greaterOrEqualThan=" + UPDATED_RECORD_TIME);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRecordTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where recordTime less than or equals to DEFAULT_RECORD_TIME
        defaultGateRecordShouldNotBeFound("recordTime.lessThan=" + DEFAULT_RECORD_TIME);

        // Get all the gateRecordList where recordTime less than or equals to UPDATED_RECORD_TIME
        defaultGateRecordShouldBeFound("recordTime.lessThan=" + UPDATED_RECORD_TIME);
    }


    @Test
    @Transactional
    public void getAllGateRecordsByRidIsEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where rid equals to DEFAULT_RID
        defaultGateRecordShouldBeFound("rid.equals=" + DEFAULT_RID);

        // Get all the gateRecordList where rid equals to UPDATED_RID
        defaultGateRecordShouldNotBeFound("rid.equals=" + UPDATED_RID);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRidIsInShouldWork() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where rid in DEFAULT_RID or UPDATED_RID
        defaultGateRecordShouldBeFound("rid.in=" + DEFAULT_RID + "," + UPDATED_RID);

        // Get all the gateRecordList where rid equals to UPDATED_RID
        defaultGateRecordShouldNotBeFound("rid.in=" + UPDATED_RID);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRidIsNullOrNotNull() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where rid is not null
        defaultGateRecordShouldBeFound("rid.specified=true");

        // Get all the gateRecordList where rid is null
        defaultGateRecordShouldNotBeFound("rid.specified=false");
    }

    @Test
    @Transactional
    public void getAllGateRecordsByCreateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where createTime equals to DEFAULT_CREATE_TIME
        defaultGateRecordShouldBeFound("createTime.equals=" + DEFAULT_CREATE_TIME);

        // Get all the gateRecordList where createTime equals to UPDATED_CREATE_TIME
        defaultGateRecordShouldNotBeFound("createTime.equals=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByCreateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where createTime in DEFAULT_CREATE_TIME or UPDATED_CREATE_TIME
        defaultGateRecordShouldBeFound("createTime.in=" + DEFAULT_CREATE_TIME + "," + UPDATED_CREATE_TIME);

        // Get all the gateRecordList where createTime equals to UPDATED_CREATE_TIME
        defaultGateRecordShouldNotBeFound("createTime.in=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByCreateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where createTime is not null
        defaultGateRecordShouldBeFound("createTime.specified=true");

        // Get all the gateRecordList where createTime is null
        defaultGateRecordShouldNotBeFound("createTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllGateRecordsByCreateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where createTime greater than or equals to DEFAULT_CREATE_TIME
        defaultGateRecordShouldBeFound("createTime.greaterOrEqualThan=" + DEFAULT_CREATE_TIME);

        // Get all the gateRecordList where createTime greater than or equals to UPDATED_CREATE_TIME
        defaultGateRecordShouldNotBeFound("createTime.greaterOrEqualThan=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByCreateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where createTime less than or equals to DEFAULT_CREATE_TIME
        defaultGateRecordShouldNotBeFound("createTime.lessThan=" + DEFAULT_CREATE_TIME);

        // Get all the gateRecordList where createTime less than or equals to UPDATED_CREATE_TIME
        defaultGateRecordShouldBeFound("createTime.lessThan=" + UPDATED_CREATE_TIME);
    }


    @Test
    @Transactional
    public void getAllGateRecordsByRegionIdIsEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where regionId equals to DEFAULT_REGION_ID
        defaultGateRecordShouldBeFound("regionId.equals=" + DEFAULT_REGION_ID);

        // Get all the gateRecordList where regionId equals to UPDATED_REGION_ID
        defaultGateRecordShouldNotBeFound("regionId.equals=" + UPDATED_REGION_ID);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRegionIdIsInShouldWork() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where regionId in DEFAULT_REGION_ID or UPDATED_REGION_ID
        defaultGateRecordShouldBeFound("regionId.in=" + DEFAULT_REGION_ID + "," + UPDATED_REGION_ID);

        // Get all the gateRecordList where regionId equals to UPDATED_REGION_ID
        defaultGateRecordShouldNotBeFound("regionId.in=" + UPDATED_REGION_ID);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRegionIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where regionId is not null
        defaultGateRecordShouldBeFound("regionId.specified=true");

        // Get all the gateRecordList where regionId is null
        defaultGateRecordShouldNotBeFound("regionId.specified=false");
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRegionIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where regionId greater than or equals to DEFAULT_REGION_ID
        defaultGateRecordShouldBeFound("regionId.greaterOrEqualThan=" + DEFAULT_REGION_ID);

        // Get all the gateRecordList where regionId greater than or equals to UPDATED_REGION_ID
        defaultGateRecordShouldNotBeFound("regionId.greaterOrEqualThan=" + UPDATED_REGION_ID);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByRegionIdIsLessThanSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where regionId less than or equals to DEFAULT_REGION_ID
        defaultGateRecordShouldNotBeFound("regionId.lessThan=" + DEFAULT_REGION_ID);

        // Get all the gateRecordList where regionId less than or equals to UPDATED_REGION_ID
        defaultGateRecordShouldBeFound("regionId.lessThan=" + UPDATED_REGION_ID);
    }


    @Test
    @Transactional
    public void getAllGateRecordsByDataMd5IsEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where dataMd5 equals to DEFAULT_DATA_MD_5
        defaultGateRecordShouldBeFound("dataMd5.equals=" + DEFAULT_DATA_MD_5);

        // Get all the gateRecordList where dataMd5 equals to UPDATED_DATA_MD_5
        defaultGateRecordShouldNotBeFound("dataMd5.equals=" + UPDATED_DATA_MD_5);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByDataMd5IsInShouldWork() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where dataMd5 in DEFAULT_DATA_MD_5 or UPDATED_DATA_MD_5
        defaultGateRecordShouldBeFound("dataMd5.in=" + DEFAULT_DATA_MD_5 + "," + UPDATED_DATA_MD_5);

        // Get all the gateRecordList where dataMd5 equals to UPDATED_DATA_MD_5
        defaultGateRecordShouldNotBeFound("dataMd5.in=" + UPDATED_DATA_MD_5);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByDataMd5IsNullOrNotNull() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where dataMd5 is not null
        defaultGateRecordShouldBeFound("dataMd5.specified=true");

        // Get all the gateRecordList where dataMd5 is null
        defaultGateRecordShouldNotBeFound("dataMd5.specified=false");
    }

    @Test
    @Transactional
    public void getAllGateRecordsByModifyTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where modifyTime equals to DEFAULT_MODIFY_TIME
        defaultGateRecordShouldBeFound("modifyTime.equals=" + DEFAULT_MODIFY_TIME);

        // Get all the gateRecordList where modifyTime equals to UPDATED_MODIFY_TIME
        defaultGateRecordShouldNotBeFound("modifyTime.equals=" + UPDATED_MODIFY_TIME);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByModifyTimeIsInShouldWork() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where modifyTime in DEFAULT_MODIFY_TIME or UPDATED_MODIFY_TIME
        defaultGateRecordShouldBeFound("modifyTime.in=" + DEFAULT_MODIFY_TIME + "," + UPDATED_MODIFY_TIME);

        // Get all the gateRecordList where modifyTime equals to UPDATED_MODIFY_TIME
        defaultGateRecordShouldNotBeFound("modifyTime.in=" + UPDATED_MODIFY_TIME);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByModifyTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where modifyTime is not null
        defaultGateRecordShouldBeFound("modifyTime.specified=true");

        // Get all the gateRecordList where modifyTime is null
        defaultGateRecordShouldNotBeFound("modifyTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllGateRecordsByModifyTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where modifyTime greater than or equals to DEFAULT_MODIFY_TIME
        defaultGateRecordShouldBeFound("modifyTime.greaterOrEqualThan=" + DEFAULT_MODIFY_TIME);

        // Get all the gateRecordList where modifyTime greater than or equals to UPDATED_MODIFY_TIME
        defaultGateRecordShouldNotBeFound("modifyTime.greaterOrEqualThan=" + UPDATED_MODIFY_TIME);
    }

    @Test
    @Transactional
    public void getAllGateRecordsByModifyTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        // Get all the gateRecordList where modifyTime less than or equals to DEFAULT_MODIFY_TIME
        defaultGateRecordShouldNotBeFound("modifyTime.lessThan=" + DEFAULT_MODIFY_TIME);

        // Get all the gateRecordList where modifyTime less than or equals to UPDATED_MODIFY_TIME
        defaultGateRecordShouldBeFound("modifyTime.lessThan=" + UPDATED_MODIFY_TIME);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultGateRecordShouldBeFound(String filter) throws Exception {
        restGateRecordMockMvc.perform(get("/api/gate-records?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gateRecord.getId().intValue())))
            .andExpect(jsonPath("$.[*].recordType").value(hasItem(DEFAULT_RECORD_TYPE.toString())))
            .andExpect(jsonPath("$.[*].truckNumber").value(hasItem(DEFAULT_TRUCK_NUMBER)))
            .andExpect(jsonPath("$.[*].recordTime").value(hasItem(sameInstant(DEFAULT_RECORD_TIME))))
            .andExpect(jsonPath("$.[*].data").value(hasItem(DEFAULT_DATA.toString())))
            .andExpect(jsonPath("$.[*].rid").value(hasItem(DEFAULT_RID)))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].regionId").value(hasItem(DEFAULT_REGION_ID.intValue())))
            .andExpect(jsonPath("$.[*].dataMd5").value(hasItem(DEFAULT_DATA_MD_5)))
            .andExpect(jsonPath("$.[*].modifyTime").value(hasItem(sameInstant(DEFAULT_MODIFY_TIME))));

        // Check, that the count call also returns 1
        restGateRecordMockMvc.perform(get("/api/gate-records/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultGateRecordShouldNotBeFound(String filter) throws Exception {
        restGateRecordMockMvc.perform(get("/api/gate-records?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restGateRecordMockMvc.perform(get("/api/gate-records/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingGateRecord() throws Exception {
        // Get the gateRecord
        restGateRecordMockMvc.perform(get("/api/gate-records/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateGateRecord() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        int databaseSizeBeforeUpdate = gateRecordRepository.findAll().size();

        // Update the gateRecord
        GateRecord updatedGateRecord = gateRecordRepository.findById(gateRecord.getId()).get();
        // Disconnect from session so that the updates on updatedGateRecord are not directly saved in db
        em.detach(updatedGateRecord);
        updatedGateRecord
            .recordType(UPDATED_RECORD_TYPE)
            .truckNumber(UPDATED_TRUCK_NUMBER)
            .recordTime(UPDATED_RECORD_TIME)
            .data(UPDATED_DATA)
            .rid(UPDATED_RID)
            .createTime(UPDATED_CREATE_TIME)
            .regionId(UPDATED_REGION_ID)
            .dataMd5(UPDATED_DATA_MD_5)
            .modifyTime(UPDATED_MODIFY_TIME);
        GateRecordDTO gateRecordDTO = gateRecordMapper.toDto(updatedGateRecord);

        restGateRecordMockMvc.perform(put("/api/gate-records")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gateRecordDTO)))
            .andExpect(status().isOk());

        // Validate the GateRecord in the database
        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeUpdate);
        GateRecord testGateRecord = gateRecordList.get(gateRecordList.size() - 1);
        assertThat(testGateRecord.getRecordType()).isEqualTo(UPDATED_RECORD_TYPE);
        assertThat(testGateRecord.getTruckNumber()).isEqualTo(UPDATED_TRUCK_NUMBER);
        assertThat(testGateRecord.getRecordTime()).isEqualTo(UPDATED_RECORD_TIME);
        assertThat(testGateRecord.getData()).isEqualTo(UPDATED_DATA);
        assertThat(testGateRecord.getRid()).isEqualTo(UPDATED_RID);
        assertThat(testGateRecord.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testGateRecord.getRegionId()).isEqualTo(UPDATED_REGION_ID);
        assertThat(testGateRecord.getDataMd5()).isEqualTo(UPDATED_DATA_MD_5);
        assertThat(testGateRecord.getModifyTime()).isEqualTo(UPDATED_MODIFY_TIME);
    }

    @Test
    @Transactional
    public void updateNonExistingGateRecord() throws Exception {
        int databaseSizeBeforeUpdate = gateRecordRepository.findAll().size();

        // Create the GateRecord
        GateRecordDTO gateRecordDTO = gateRecordMapper.toDto(gateRecord);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGateRecordMockMvc.perform(put("/api/gate-records")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gateRecordDTO)))
            .andExpect(status().isBadRequest());

        // Validate the GateRecord in the database
        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteGateRecord() throws Exception {
        // Initialize the database
        gateRecordRepository.saveAndFlush(gateRecord);

        int databaseSizeBeforeDelete = gateRecordRepository.findAll().size();

        // Delete the gateRecord
        restGateRecordMockMvc.perform(delete("/api/gate-records/{id}", gateRecord.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<GateRecord> gateRecordList = gateRecordRepository.findAll();
        assertThat(gateRecordList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(GateRecord.class);
        GateRecord gateRecord1 = new GateRecord();
        gateRecord1.setId(1L);
        GateRecord gateRecord2 = new GateRecord();
        gateRecord2.setId(gateRecord1.getId());
        assertThat(gateRecord1).isEqualTo(gateRecord2);
        gateRecord2.setId(2L);
        assertThat(gateRecord1).isNotEqualTo(gateRecord2);
        gateRecord1.setId(null);
        assertThat(gateRecord1).isNotEqualTo(gateRecord2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(GateRecordDTO.class);
        GateRecordDTO gateRecordDTO1 = new GateRecordDTO();
        gateRecordDTO1.setId(1L);
        GateRecordDTO gateRecordDTO2 = new GateRecordDTO();
        assertThat(gateRecordDTO1).isNotEqualTo(gateRecordDTO2);
        gateRecordDTO2.setId(gateRecordDTO1.getId());
        assertThat(gateRecordDTO1).isEqualTo(gateRecordDTO2);
        gateRecordDTO2.setId(2L);
        assertThat(gateRecordDTO1).isNotEqualTo(gateRecordDTO2);
        gateRecordDTO1.setId(null);
        assertThat(gateRecordDTO1).isNotEqualTo(gateRecordDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(gateRecordMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(gateRecordMapper.fromId(null)).isNull();
    }
}
