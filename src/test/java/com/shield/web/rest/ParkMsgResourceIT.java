package com.shield.web.rest;

import com.shield.ShieldApp;
import com.shield.domain.ParkMsg;
import com.shield.repository.ParkMsgRepository;
import com.shield.service.ParkMsgService;
import com.shield.service.dto.ParkMsgDTO;
import com.shield.service.mapper.ParkMsgMapper;
import com.shield.web.rest.errors.ExceptionTranslator;

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

import com.shield.domain.enumeration.ParkMsgType;
/**
 * Integration tests for the {@Link ParkMsgResource} REST controller.
 */
@SpringBootTest(classes = ShieldApp.class)
public class ParkMsgResourceIT {

    private static final String DEFAULT_PARKID = "AAAAAAAAAA";
    private static final String UPDATED_PARKID = "BBBBBBBBBB";

    private static final String DEFAULT_SERVICE = "AAAAAAAAAA";
    private static final String UPDATED_SERVICE = "BBBBBBBBBB";

    private static final String DEFAULT_TRUCK_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_TRUCK_NUMBER = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_SEND_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_SEND_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_BODY = "AAAAAAAAAA";
    private static final String UPDATED_BODY = "BBBBBBBBBB";

    private static final ParkMsgType DEFAULT_TYPE = ParkMsgType.IN;
    private static final ParkMsgType UPDATED_TYPE = ParkMsgType.OUT;

    private static final Integer DEFAULT_SEND_TIMES = 1;
    private static final Integer UPDATED_SEND_TIMES = 2;

    @Autowired
    private ParkMsgRepository parkMsgRepository;

    @Autowired
    private ParkMsgMapper parkMsgMapper;

    @Autowired
    private ParkMsgService parkMsgService;

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

    private MockMvc restParkMsgMockMvc;

    private ParkMsg parkMsg;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ParkMsgResource parkMsgResource = new ParkMsgResource(parkMsgService);
        this.restParkMsgMockMvc = MockMvcBuilders.standaloneSetup(parkMsgResource)
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
    public static ParkMsg createEntity(EntityManager em) {
        ParkMsg parkMsg = new ParkMsg()
            .parkid(DEFAULT_PARKID)
            .service(DEFAULT_SERVICE)
            .truckNumber(DEFAULT_TRUCK_NUMBER)
            .createTime(DEFAULT_CREATE_TIME)
            .sendTime(DEFAULT_SEND_TIME)
            .body(DEFAULT_BODY)
            .type(DEFAULT_TYPE)
            .sendTimes(DEFAULT_SEND_TIMES);
        return parkMsg;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ParkMsg createUpdatedEntity(EntityManager em) {
        ParkMsg parkMsg = new ParkMsg()
            .parkid(UPDATED_PARKID)
            .service(UPDATED_SERVICE)
            .truckNumber(UPDATED_TRUCK_NUMBER)
            .createTime(UPDATED_CREATE_TIME)
            .sendTime(UPDATED_SEND_TIME)
            .body(UPDATED_BODY)
            .type(UPDATED_TYPE)
            .sendTimes(UPDATED_SEND_TIMES);
        return parkMsg;
    }

    @BeforeEach
    public void initTest() {
        parkMsg = createEntity(em);
    }

    @Test
    @Transactional
    public void createParkMsg() throws Exception {
        int databaseSizeBeforeCreate = parkMsgRepository.findAll().size();

        // Create the ParkMsg
        ParkMsgDTO parkMsgDTO = parkMsgMapper.toDto(parkMsg);
        restParkMsgMockMvc.perform(post("/api/park-msgs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(parkMsgDTO)))
            .andExpect(status().isCreated());

        // Validate the ParkMsg in the database
        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeCreate + 1);
        ParkMsg testParkMsg = parkMsgList.get(parkMsgList.size() - 1);
        assertThat(testParkMsg.getParkid()).isEqualTo(DEFAULT_PARKID);
        assertThat(testParkMsg.getService()).isEqualTo(DEFAULT_SERVICE);
        assertThat(testParkMsg.getTruckNumber()).isEqualTo(DEFAULT_TRUCK_NUMBER);
        assertThat(testParkMsg.getCreateTime()).isEqualTo(DEFAULT_CREATE_TIME);
        assertThat(testParkMsg.getSendTime()).isEqualTo(DEFAULT_SEND_TIME);
        assertThat(testParkMsg.getBody()).isEqualTo(DEFAULT_BODY);
        assertThat(testParkMsg.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testParkMsg.getSendTimes()).isEqualTo(DEFAULT_SEND_TIMES);
    }

    @Test
    @Transactional
    public void createParkMsgWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = parkMsgRepository.findAll().size();

        // Create the ParkMsg with an existing ID
        parkMsg.setId(1L);
        ParkMsgDTO parkMsgDTO = parkMsgMapper.toDto(parkMsg);

        // An entity with an existing ID cannot be created, so this API call must fail
        restParkMsgMockMvc.perform(post("/api/park-msgs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(parkMsgDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ParkMsg in the database
        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkParkidIsRequired() throws Exception {
        int databaseSizeBeforeTest = parkMsgRepository.findAll().size();
        // set the field null
        parkMsg.setParkid(null);

        // Create the ParkMsg, which fails.
        ParkMsgDTO parkMsgDTO = parkMsgMapper.toDto(parkMsg);

        restParkMsgMockMvc.perform(post("/api/park-msgs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(parkMsgDTO)))
            .andExpect(status().isBadRequest());

        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkServiceIsRequired() throws Exception {
        int databaseSizeBeforeTest = parkMsgRepository.findAll().size();
        // set the field null
        parkMsg.setService(null);

        // Create the ParkMsg, which fails.
        ParkMsgDTO parkMsgDTO = parkMsgMapper.toDto(parkMsg);

        restParkMsgMockMvc.perform(post("/api/park-msgs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(parkMsgDTO)))
            .andExpect(status().isBadRequest());

        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCreateTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = parkMsgRepository.findAll().size();
        // set the field null
        parkMsg.setCreateTime(null);

        // Create the ParkMsg, which fails.
        ParkMsgDTO parkMsgDTO = parkMsgMapper.toDto(parkMsg);

        restParkMsgMockMvc.perform(post("/api/park-msgs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(parkMsgDTO)))
            .andExpect(status().isBadRequest());

        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSendTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = parkMsgRepository.findAll().size();
        // set the field null
        parkMsg.setSendTime(null);

        // Create the ParkMsg, which fails.
        ParkMsgDTO parkMsgDTO = parkMsgMapper.toDto(parkMsg);

        restParkMsgMockMvc.perform(post("/api/park-msgs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(parkMsgDTO)))
            .andExpect(status().isBadRequest());

        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkBodyIsRequired() throws Exception {
        int databaseSizeBeforeTest = parkMsgRepository.findAll().size();
        // set the field null
        parkMsg.setBody(null);

        // Create the ParkMsg, which fails.
        ParkMsgDTO parkMsgDTO = parkMsgMapper.toDto(parkMsg);

        restParkMsgMockMvc.perform(post("/api/park-msgs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(parkMsgDTO)))
            .andExpect(status().isBadRequest());

        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSendTimesIsRequired() throws Exception {
        int databaseSizeBeforeTest = parkMsgRepository.findAll().size();
        // set the field null
        parkMsg.setSendTimes(null);

        // Create the ParkMsg, which fails.
        ParkMsgDTO parkMsgDTO = parkMsgMapper.toDto(parkMsg);

        restParkMsgMockMvc.perform(post("/api/park-msgs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(parkMsgDTO)))
            .andExpect(status().isBadRequest());

        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllParkMsgs() throws Exception {
        // Initialize the database
        parkMsgRepository.saveAndFlush(parkMsg);

        // Get all the parkMsgList
        restParkMsgMockMvc.perform(get("/api/park-msgs?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(parkMsg.getId().intValue())))
            .andExpect(jsonPath("$.[*].parkid").value(hasItem(DEFAULT_PARKID.toString())))
            .andExpect(jsonPath("$.[*].service").value(hasItem(DEFAULT_SERVICE.toString())))
            .andExpect(jsonPath("$.[*].truckNumber").value(hasItem(DEFAULT_TRUCK_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].sendTime").value(hasItem(sameInstant(DEFAULT_SEND_TIME))))
            .andExpect(jsonPath("$.[*].body").value(hasItem(DEFAULT_BODY.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].sendTimes").value(hasItem(DEFAULT_SEND_TIMES)));
    }
    
    @Test
    @Transactional
    public void getParkMsg() throws Exception {
        // Initialize the database
        parkMsgRepository.saveAndFlush(parkMsg);

        // Get the parkMsg
        restParkMsgMockMvc.perform(get("/api/park-msgs/{id}", parkMsg.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(parkMsg.getId().intValue()))
            .andExpect(jsonPath("$.parkid").value(DEFAULT_PARKID.toString()))
            .andExpect(jsonPath("$.service").value(DEFAULT_SERVICE.toString()))
            .andExpect(jsonPath("$.truckNumber").value(DEFAULT_TRUCK_NUMBER.toString()))
            .andExpect(jsonPath("$.createTime").value(sameInstant(DEFAULT_CREATE_TIME)))
            .andExpect(jsonPath("$.sendTime").value(sameInstant(DEFAULT_SEND_TIME)))
            .andExpect(jsonPath("$.body").value(DEFAULT_BODY.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.sendTimes").value(DEFAULT_SEND_TIMES));
    }

    @Test
    @Transactional
    public void getNonExistingParkMsg() throws Exception {
        // Get the parkMsg
        restParkMsgMockMvc.perform(get("/api/park-msgs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateParkMsg() throws Exception {
        // Initialize the database
        parkMsgRepository.saveAndFlush(parkMsg);

        int databaseSizeBeforeUpdate = parkMsgRepository.findAll().size();

        // Update the parkMsg
        ParkMsg updatedParkMsg = parkMsgRepository.findById(parkMsg.getId()).get();
        // Disconnect from session so that the updates on updatedParkMsg are not directly saved in db
        em.detach(updatedParkMsg);
        updatedParkMsg
            .parkid(UPDATED_PARKID)
            .service(UPDATED_SERVICE)
            .truckNumber(UPDATED_TRUCK_NUMBER)
            .createTime(UPDATED_CREATE_TIME)
            .sendTime(UPDATED_SEND_TIME)
            .body(UPDATED_BODY)
            .type(UPDATED_TYPE)
            .sendTimes(UPDATED_SEND_TIMES);
        ParkMsgDTO parkMsgDTO = parkMsgMapper.toDto(updatedParkMsg);

        restParkMsgMockMvc.perform(put("/api/park-msgs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(parkMsgDTO)))
            .andExpect(status().isOk());

        // Validate the ParkMsg in the database
        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeUpdate);
        ParkMsg testParkMsg = parkMsgList.get(parkMsgList.size() - 1);
        assertThat(testParkMsg.getParkid()).isEqualTo(UPDATED_PARKID);
        assertThat(testParkMsg.getService()).isEqualTo(UPDATED_SERVICE);
        assertThat(testParkMsg.getTruckNumber()).isEqualTo(UPDATED_TRUCK_NUMBER);
        assertThat(testParkMsg.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testParkMsg.getSendTime()).isEqualTo(UPDATED_SEND_TIME);
        assertThat(testParkMsg.getBody()).isEqualTo(UPDATED_BODY);
        assertThat(testParkMsg.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testParkMsg.getSendTimes()).isEqualTo(UPDATED_SEND_TIMES);
    }

    @Test
    @Transactional
    public void updateNonExistingParkMsg() throws Exception {
        int databaseSizeBeforeUpdate = parkMsgRepository.findAll().size();

        // Create the ParkMsg
        ParkMsgDTO parkMsgDTO = parkMsgMapper.toDto(parkMsg);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restParkMsgMockMvc.perform(put("/api/park-msgs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(parkMsgDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ParkMsg in the database
        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteParkMsg() throws Exception {
        // Initialize the database
        parkMsgRepository.saveAndFlush(parkMsg);

        int databaseSizeBeforeDelete = parkMsgRepository.findAll().size();

        // Delete the parkMsg
        restParkMsgMockMvc.perform(delete("/api/park-msgs/{id}", parkMsg.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<ParkMsg> parkMsgList = parkMsgRepository.findAll();
        assertThat(parkMsgList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ParkMsg.class);
        ParkMsg parkMsg1 = new ParkMsg();
        parkMsg1.setId(1L);
        ParkMsg parkMsg2 = new ParkMsg();
        parkMsg2.setId(parkMsg1.getId());
        assertThat(parkMsg1).isEqualTo(parkMsg2);
        parkMsg2.setId(2L);
        assertThat(parkMsg1).isNotEqualTo(parkMsg2);
        parkMsg1.setId(null);
        assertThat(parkMsg1).isNotEqualTo(parkMsg2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ParkMsgDTO.class);
        ParkMsgDTO parkMsgDTO1 = new ParkMsgDTO();
        parkMsgDTO1.setId(1L);
        ParkMsgDTO parkMsgDTO2 = new ParkMsgDTO();
        assertThat(parkMsgDTO1).isNotEqualTo(parkMsgDTO2);
        parkMsgDTO2.setId(parkMsgDTO1.getId());
        assertThat(parkMsgDTO1).isEqualTo(parkMsgDTO2);
        parkMsgDTO2.setId(2L);
        assertThat(parkMsgDTO1).isNotEqualTo(parkMsgDTO2);
        parkMsgDTO1.setId(null);
        assertThat(parkMsgDTO1).isNotEqualTo(parkMsgDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(parkMsgMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(parkMsgMapper.fromId(null)).isNull();
    }
}
