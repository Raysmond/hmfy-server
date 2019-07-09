package com.shield.web.rest;

import com.shield.ShieldApp;
import com.shield.domain.WxMaUser;
import com.shield.domain.User;
import com.shield.repository.WxMaUserRepository;
import com.shield.service.WxMaUserService;
import com.shield.service.dto.WxMaUserDTO;
import com.shield.service.mapper.WxMaUserMapper;
import com.shield.web.rest.errors.ExceptionTranslator;
import com.shield.service.dto.WxMaUserCriteria;
import com.shield.service.WxMaUserQueryService;

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
 * Integration tests for the {@Link WxMaUserResource} REST controller.
 */
@SpringBootTest(classes = ShieldApp.class)
public class WxMaUserResourceIT {

    private static final String DEFAULT_OPEN_ID = "AAAAAAAAAA";
    private static final String UPDATED_OPEN_ID = "BBBBBBBBBB";

    private static final String DEFAULT_NICK_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NICK_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_GENDER = "AAAAAAAAAA";
    private static final String UPDATED_GENDER = "BBBBBBBBBB";

    private static final String DEFAULT_LANGUAGE = "AAAAAAAAAA";
    private static final String UPDATED_LANGUAGE = "BBBBBBBBBB";

    private static final String DEFAULT_CITY = "AAAAAAAAAA";
    private static final String UPDATED_CITY = "BBBBBBBBBB";

    private static final String DEFAULT_PROVINCE = "AAAAAAAAAA";
    private static final String UPDATED_PROVINCE = "BBBBBBBBBB";

    private static final String DEFAULT_COUNTRY = "AAAAAAAAAA";
    private static final String UPDATED_COUNTRY = "BBBBBBBBBB";

    private static final String DEFAULT_AVATAR_URL = "AAAAAAAAAA";
    private static final String UPDATED_AVATAR_URL = "BBBBBBBBBB";

    private static final String DEFAULT_UNION_ID = "AAAAAAAAAA";
    private static final String UPDATED_UNION_ID = "BBBBBBBBBB";

    private static final String DEFAULT_WATERMARK = "AAAAAAAAAA";
    private static final String UPDATED_WATERMARK = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_APP_ID = "AAAAAAAAAA";
    private static final String UPDATED_APP_ID = "BBBBBBBBBB";

    @Autowired
    private WxMaUserRepository wxMaUserRepository;

    @Autowired
    private WxMaUserMapper wxMaUserMapper;

    @Autowired
    private WxMaUserService wxMaUserService;

    @Autowired
    private WxMaUserQueryService wxMaUserQueryService;

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

    private MockMvc restWxMaUserMockMvc;

    private WxMaUser wxMaUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final WxMaUserResource wxMaUserResource = new WxMaUserResource(wxMaUserService, wxMaUserQueryService);
        this.restWxMaUserMockMvc = MockMvcBuilders.standaloneSetup(wxMaUserResource)
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
    public static WxMaUser createEntity(EntityManager em) {
        WxMaUser wxMaUser = new WxMaUser()
            .openId(DEFAULT_OPEN_ID)
            .nickName(DEFAULT_NICK_NAME)
            .gender(DEFAULT_GENDER)
            .language(DEFAULT_LANGUAGE)
            .city(DEFAULT_CITY)
            .province(DEFAULT_PROVINCE)
            .country(DEFAULT_COUNTRY)
            .avatarUrl(DEFAULT_AVATAR_URL)
            .unionId(DEFAULT_UNION_ID)
            .watermark(DEFAULT_WATERMARK)
            .createTime(DEFAULT_CREATE_TIME)
            .updateTime(DEFAULT_UPDATE_TIME)
            .phone(DEFAULT_PHONE)
            .appId(DEFAULT_APP_ID);
        return wxMaUser;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WxMaUser createUpdatedEntity(EntityManager em) {
        WxMaUser wxMaUser = new WxMaUser()
            .openId(UPDATED_OPEN_ID)
            .nickName(UPDATED_NICK_NAME)
            .gender(UPDATED_GENDER)
            .language(UPDATED_LANGUAGE)
            .city(UPDATED_CITY)
            .province(UPDATED_PROVINCE)
            .country(UPDATED_COUNTRY)
            .avatarUrl(UPDATED_AVATAR_URL)
            .unionId(UPDATED_UNION_ID)
            .watermark(UPDATED_WATERMARK)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME)
            .phone(UPDATED_PHONE)
            .appId(UPDATED_APP_ID);
        return wxMaUser;
    }

    @BeforeEach
    public void initTest() {
        wxMaUser = createEntity(em);
    }

    @Test
    @Transactional
    public void createWxMaUser() throws Exception {
        int databaseSizeBeforeCreate = wxMaUserRepository.findAll().size();

        // Create the WxMaUser
        WxMaUserDTO wxMaUserDTO = wxMaUserMapper.toDto(wxMaUser);
        restWxMaUserMockMvc.perform(post("/api/wx-ma-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wxMaUserDTO)))
            .andExpect(status().isCreated());

        // Validate the WxMaUser in the database
        List<WxMaUser> wxMaUserList = wxMaUserRepository.findAll();
        assertThat(wxMaUserList).hasSize(databaseSizeBeforeCreate + 1);
        WxMaUser testWxMaUser = wxMaUserList.get(wxMaUserList.size() - 1);
        assertThat(testWxMaUser.getOpenId()).isEqualTo(DEFAULT_OPEN_ID);
        assertThat(testWxMaUser.getNickName()).isEqualTo(DEFAULT_NICK_NAME);
        assertThat(testWxMaUser.getGender()).isEqualTo(DEFAULT_GENDER);
        assertThat(testWxMaUser.getLanguage()).isEqualTo(DEFAULT_LANGUAGE);
        assertThat(testWxMaUser.getCity()).isEqualTo(DEFAULT_CITY);
        assertThat(testWxMaUser.getProvince()).isEqualTo(DEFAULT_PROVINCE);
        assertThat(testWxMaUser.getCountry()).isEqualTo(DEFAULT_COUNTRY);
        assertThat(testWxMaUser.getAvatarUrl()).isEqualTo(DEFAULT_AVATAR_URL);
        assertThat(testWxMaUser.getUnionId()).isEqualTo(DEFAULT_UNION_ID);
        assertThat(testWxMaUser.getWatermark()).isEqualTo(DEFAULT_WATERMARK);
        assertThat(testWxMaUser.getCreateTime()).isEqualTo(DEFAULT_CREATE_TIME);
        assertThat(testWxMaUser.getUpdateTime()).isEqualTo(DEFAULT_UPDATE_TIME);
        assertThat(testWxMaUser.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(testWxMaUser.getAppId()).isEqualTo(DEFAULT_APP_ID);
    }

    @Test
    @Transactional
    public void createWxMaUserWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = wxMaUserRepository.findAll().size();

        // Create the WxMaUser with an existing ID
        wxMaUser.setId(1L);
        WxMaUserDTO wxMaUserDTO = wxMaUserMapper.toDto(wxMaUser);

        // An entity with an existing ID cannot be created, so this API call must fail
        restWxMaUserMockMvc.perform(post("/api/wx-ma-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wxMaUserDTO)))
            .andExpect(status().isBadRequest());

        // Validate the WxMaUser in the database
        List<WxMaUser> wxMaUserList = wxMaUserRepository.findAll();
        assertThat(wxMaUserList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkOpenIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = wxMaUserRepository.findAll().size();
        // set the field null
        wxMaUser.setOpenId(null);

        // Create the WxMaUser, which fails.
        WxMaUserDTO wxMaUserDTO = wxMaUserMapper.toDto(wxMaUser);

        restWxMaUserMockMvc.perform(post("/api/wx-ma-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wxMaUserDTO)))
            .andExpect(status().isBadRequest());

        List<WxMaUser> wxMaUserList = wxMaUserRepository.findAll();
        assertThat(wxMaUserList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCreateTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = wxMaUserRepository.findAll().size();
        // set the field null
        wxMaUser.setCreateTime(null);

        // Create the WxMaUser, which fails.
        WxMaUserDTO wxMaUserDTO = wxMaUserMapper.toDto(wxMaUser);

        restWxMaUserMockMvc.perform(post("/api/wx-ma-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wxMaUserDTO)))
            .andExpect(status().isBadRequest());

        List<WxMaUser> wxMaUserList = wxMaUserRepository.findAll();
        assertThat(wxMaUserList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUpdateTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = wxMaUserRepository.findAll().size();
        // set the field null
        wxMaUser.setUpdateTime(null);

        // Create the WxMaUser, which fails.
        WxMaUserDTO wxMaUserDTO = wxMaUserMapper.toDto(wxMaUser);

        restWxMaUserMockMvc.perform(post("/api/wx-ma-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wxMaUserDTO)))
            .andExpect(status().isBadRequest());

        List<WxMaUser> wxMaUserList = wxMaUserRepository.findAll();
        assertThat(wxMaUserList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAppIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = wxMaUserRepository.findAll().size();
        // set the field null
        wxMaUser.setAppId(null);

        // Create the WxMaUser, which fails.
        WxMaUserDTO wxMaUserDTO = wxMaUserMapper.toDto(wxMaUser);

        restWxMaUserMockMvc.perform(post("/api/wx-ma-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wxMaUserDTO)))
            .andExpect(status().isBadRequest());

        List<WxMaUser> wxMaUserList = wxMaUserRepository.findAll();
        assertThat(wxMaUserList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllWxMaUsers() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList
        restWxMaUserMockMvc.perform(get("/api/wx-ma-users?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(wxMaUser.getId().intValue())))
            .andExpect(jsonPath("$.[*].openId").value(hasItem(DEFAULT_OPEN_ID.toString())))
            .andExpect(jsonPath("$.[*].nickName").value(hasItem(DEFAULT_NICK_NAME.toString())))
            .andExpect(jsonPath("$.[*].gender").value(hasItem(DEFAULT_GENDER.toString())))
            .andExpect(jsonPath("$.[*].language").value(hasItem(DEFAULT_LANGUAGE.toString())))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY.toString())))
            .andExpect(jsonPath("$.[*].province").value(hasItem(DEFAULT_PROVINCE.toString())))
            .andExpect(jsonPath("$.[*].country").value(hasItem(DEFAULT_COUNTRY.toString())))
            .andExpect(jsonPath("$.[*].avatarUrl").value(hasItem(DEFAULT_AVATAR_URL.toString())))
            .andExpect(jsonPath("$.[*].unionId").value(hasItem(DEFAULT_UNION_ID.toString())))
            .andExpect(jsonPath("$.[*].watermark").value(hasItem(DEFAULT_WATERMARK.toString())))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE.toString())))
            .andExpect(jsonPath("$.[*].appId").value(hasItem(DEFAULT_APP_ID.toString())));
    }
    
    @Test
    @Transactional
    public void getWxMaUser() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get the wxMaUser
        restWxMaUserMockMvc.perform(get("/api/wx-ma-users/{id}", wxMaUser.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(wxMaUser.getId().intValue()))
            .andExpect(jsonPath("$.openId").value(DEFAULT_OPEN_ID.toString()))
            .andExpect(jsonPath("$.nickName").value(DEFAULT_NICK_NAME.toString()))
            .andExpect(jsonPath("$.gender").value(DEFAULT_GENDER.toString()))
            .andExpect(jsonPath("$.language").value(DEFAULT_LANGUAGE.toString()))
            .andExpect(jsonPath("$.city").value(DEFAULT_CITY.toString()))
            .andExpect(jsonPath("$.province").value(DEFAULT_PROVINCE.toString()))
            .andExpect(jsonPath("$.country").value(DEFAULT_COUNTRY.toString()))
            .andExpect(jsonPath("$.avatarUrl").value(DEFAULT_AVATAR_URL.toString()))
            .andExpect(jsonPath("$.unionId").value(DEFAULT_UNION_ID.toString()))
            .andExpect(jsonPath("$.watermark").value(DEFAULT_WATERMARK.toString()))
            .andExpect(jsonPath("$.createTime").value(sameInstant(DEFAULT_CREATE_TIME)))
            .andExpect(jsonPath("$.updateTime").value(sameInstant(DEFAULT_UPDATE_TIME)))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE.toString()))
            .andExpect(jsonPath("$.appId").value(DEFAULT_APP_ID.toString()));
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByOpenIdIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where openId equals to DEFAULT_OPEN_ID
        defaultWxMaUserShouldBeFound("openId.equals=" + DEFAULT_OPEN_ID);

        // Get all the wxMaUserList where openId equals to UPDATED_OPEN_ID
        defaultWxMaUserShouldNotBeFound("openId.equals=" + UPDATED_OPEN_ID);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByOpenIdIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where openId in DEFAULT_OPEN_ID or UPDATED_OPEN_ID
        defaultWxMaUserShouldBeFound("openId.in=" + DEFAULT_OPEN_ID + "," + UPDATED_OPEN_ID);

        // Get all the wxMaUserList where openId equals to UPDATED_OPEN_ID
        defaultWxMaUserShouldNotBeFound("openId.in=" + UPDATED_OPEN_ID);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByOpenIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where openId is not null
        defaultWxMaUserShouldBeFound("openId.specified=true");

        // Get all the wxMaUserList where openId is null
        defaultWxMaUserShouldNotBeFound("openId.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByNickNameIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where nickName equals to DEFAULT_NICK_NAME
        defaultWxMaUserShouldBeFound("nickName.equals=" + DEFAULT_NICK_NAME);

        // Get all the wxMaUserList where nickName equals to UPDATED_NICK_NAME
        defaultWxMaUserShouldNotBeFound("nickName.equals=" + UPDATED_NICK_NAME);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByNickNameIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where nickName in DEFAULT_NICK_NAME or UPDATED_NICK_NAME
        defaultWxMaUserShouldBeFound("nickName.in=" + DEFAULT_NICK_NAME + "," + UPDATED_NICK_NAME);

        // Get all the wxMaUserList where nickName equals to UPDATED_NICK_NAME
        defaultWxMaUserShouldNotBeFound("nickName.in=" + UPDATED_NICK_NAME);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByNickNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where nickName is not null
        defaultWxMaUserShouldBeFound("nickName.specified=true");

        // Get all the wxMaUserList where nickName is null
        defaultWxMaUserShouldNotBeFound("nickName.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByGenderIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where gender equals to DEFAULT_GENDER
        defaultWxMaUserShouldBeFound("gender.equals=" + DEFAULT_GENDER);

        // Get all the wxMaUserList where gender equals to UPDATED_GENDER
        defaultWxMaUserShouldNotBeFound("gender.equals=" + UPDATED_GENDER);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByGenderIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where gender in DEFAULT_GENDER or UPDATED_GENDER
        defaultWxMaUserShouldBeFound("gender.in=" + DEFAULT_GENDER + "," + UPDATED_GENDER);

        // Get all the wxMaUserList where gender equals to UPDATED_GENDER
        defaultWxMaUserShouldNotBeFound("gender.in=" + UPDATED_GENDER);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByGenderIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where gender is not null
        defaultWxMaUserShouldBeFound("gender.specified=true");

        // Get all the wxMaUserList where gender is null
        defaultWxMaUserShouldNotBeFound("gender.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByLanguageIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where language equals to DEFAULT_LANGUAGE
        defaultWxMaUserShouldBeFound("language.equals=" + DEFAULT_LANGUAGE);

        // Get all the wxMaUserList where language equals to UPDATED_LANGUAGE
        defaultWxMaUserShouldNotBeFound("language.equals=" + UPDATED_LANGUAGE);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByLanguageIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where language in DEFAULT_LANGUAGE or UPDATED_LANGUAGE
        defaultWxMaUserShouldBeFound("language.in=" + DEFAULT_LANGUAGE + "," + UPDATED_LANGUAGE);

        // Get all the wxMaUserList where language equals to UPDATED_LANGUAGE
        defaultWxMaUserShouldNotBeFound("language.in=" + UPDATED_LANGUAGE);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByLanguageIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where language is not null
        defaultWxMaUserShouldBeFound("language.specified=true");

        // Get all the wxMaUserList where language is null
        defaultWxMaUserShouldNotBeFound("language.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCityIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where city equals to DEFAULT_CITY
        defaultWxMaUserShouldBeFound("city.equals=" + DEFAULT_CITY);

        // Get all the wxMaUserList where city equals to UPDATED_CITY
        defaultWxMaUserShouldNotBeFound("city.equals=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCityIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where city in DEFAULT_CITY or UPDATED_CITY
        defaultWxMaUserShouldBeFound("city.in=" + DEFAULT_CITY + "," + UPDATED_CITY);

        // Get all the wxMaUserList where city equals to UPDATED_CITY
        defaultWxMaUserShouldNotBeFound("city.in=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCityIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where city is not null
        defaultWxMaUserShouldBeFound("city.specified=true");

        // Get all the wxMaUserList where city is null
        defaultWxMaUserShouldNotBeFound("city.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByProvinceIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where province equals to DEFAULT_PROVINCE
        defaultWxMaUserShouldBeFound("province.equals=" + DEFAULT_PROVINCE);

        // Get all the wxMaUserList where province equals to UPDATED_PROVINCE
        defaultWxMaUserShouldNotBeFound("province.equals=" + UPDATED_PROVINCE);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByProvinceIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where province in DEFAULT_PROVINCE or UPDATED_PROVINCE
        defaultWxMaUserShouldBeFound("province.in=" + DEFAULT_PROVINCE + "," + UPDATED_PROVINCE);

        // Get all the wxMaUserList where province equals to UPDATED_PROVINCE
        defaultWxMaUserShouldNotBeFound("province.in=" + UPDATED_PROVINCE);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByProvinceIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where province is not null
        defaultWxMaUserShouldBeFound("province.specified=true");

        // Get all the wxMaUserList where province is null
        defaultWxMaUserShouldNotBeFound("province.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCountryIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where country equals to DEFAULT_COUNTRY
        defaultWxMaUserShouldBeFound("country.equals=" + DEFAULT_COUNTRY);

        // Get all the wxMaUserList where country equals to UPDATED_COUNTRY
        defaultWxMaUserShouldNotBeFound("country.equals=" + UPDATED_COUNTRY);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCountryIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where country in DEFAULT_COUNTRY or UPDATED_COUNTRY
        defaultWxMaUserShouldBeFound("country.in=" + DEFAULT_COUNTRY + "," + UPDATED_COUNTRY);

        // Get all the wxMaUserList where country equals to UPDATED_COUNTRY
        defaultWxMaUserShouldNotBeFound("country.in=" + UPDATED_COUNTRY);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCountryIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where country is not null
        defaultWxMaUserShouldBeFound("country.specified=true");

        // Get all the wxMaUserList where country is null
        defaultWxMaUserShouldNotBeFound("country.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByAvatarUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where avatarUrl equals to DEFAULT_AVATAR_URL
        defaultWxMaUserShouldBeFound("avatarUrl.equals=" + DEFAULT_AVATAR_URL);

        // Get all the wxMaUserList where avatarUrl equals to UPDATED_AVATAR_URL
        defaultWxMaUserShouldNotBeFound("avatarUrl.equals=" + UPDATED_AVATAR_URL);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByAvatarUrlIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where avatarUrl in DEFAULT_AVATAR_URL or UPDATED_AVATAR_URL
        defaultWxMaUserShouldBeFound("avatarUrl.in=" + DEFAULT_AVATAR_URL + "," + UPDATED_AVATAR_URL);

        // Get all the wxMaUserList where avatarUrl equals to UPDATED_AVATAR_URL
        defaultWxMaUserShouldNotBeFound("avatarUrl.in=" + UPDATED_AVATAR_URL);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByAvatarUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where avatarUrl is not null
        defaultWxMaUserShouldBeFound("avatarUrl.specified=true");

        // Get all the wxMaUserList where avatarUrl is null
        defaultWxMaUserShouldNotBeFound("avatarUrl.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByUnionIdIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where unionId equals to DEFAULT_UNION_ID
        defaultWxMaUserShouldBeFound("unionId.equals=" + DEFAULT_UNION_ID);

        // Get all the wxMaUserList where unionId equals to UPDATED_UNION_ID
        defaultWxMaUserShouldNotBeFound("unionId.equals=" + UPDATED_UNION_ID);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByUnionIdIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where unionId in DEFAULT_UNION_ID or UPDATED_UNION_ID
        defaultWxMaUserShouldBeFound("unionId.in=" + DEFAULT_UNION_ID + "," + UPDATED_UNION_ID);

        // Get all the wxMaUserList where unionId equals to UPDATED_UNION_ID
        defaultWxMaUserShouldNotBeFound("unionId.in=" + UPDATED_UNION_ID);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByUnionIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where unionId is not null
        defaultWxMaUserShouldBeFound("unionId.specified=true");

        // Get all the wxMaUserList where unionId is null
        defaultWxMaUserShouldNotBeFound("unionId.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByWatermarkIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where watermark equals to DEFAULT_WATERMARK
        defaultWxMaUserShouldBeFound("watermark.equals=" + DEFAULT_WATERMARK);

        // Get all the wxMaUserList where watermark equals to UPDATED_WATERMARK
        defaultWxMaUserShouldNotBeFound("watermark.equals=" + UPDATED_WATERMARK);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByWatermarkIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where watermark in DEFAULT_WATERMARK or UPDATED_WATERMARK
        defaultWxMaUserShouldBeFound("watermark.in=" + DEFAULT_WATERMARK + "," + UPDATED_WATERMARK);

        // Get all the wxMaUserList where watermark equals to UPDATED_WATERMARK
        defaultWxMaUserShouldNotBeFound("watermark.in=" + UPDATED_WATERMARK);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByWatermarkIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where watermark is not null
        defaultWxMaUserShouldBeFound("watermark.specified=true");

        // Get all the wxMaUserList where watermark is null
        defaultWxMaUserShouldNotBeFound("watermark.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCreateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where createTime equals to DEFAULT_CREATE_TIME
        defaultWxMaUserShouldBeFound("createTime.equals=" + DEFAULT_CREATE_TIME);

        // Get all the wxMaUserList where createTime equals to UPDATED_CREATE_TIME
        defaultWxMaUserShouldNotBeFound("createTime.equals=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCreateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where createTime in DEFAULT_CREATE_TIME or UPDATED_CREATE_TIME
        defaultWxMaUserShouldBeFound("createTime.in=" + DEFAULT_CREATE_TIME + "," + UPDATED_CREATE_TIME);

        // Get all the wxMaUserList where createTime equals to UPDATED_CREATE_TIME
        defaultWxMaUserShouldNotBeFound("createTime.in=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCreateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where createTime is not null
        defaultWxMaUserShouldBeFound("createTime.specified=true");

        // Get all the wxMaUserList where createTime is null
        defaultWxMaUserShouldNotBeFound("createTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCreateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where createTime greater than or equals to DEFAULT_CREATE_TIME
        defaultWxMaUserShouldBeFound("createTime.greaterOrEqualThan=" + DEFAULT_CREATE_TIME);

        // Get all the wxMaUserList where createTime greater than or equals to UPDATED_CREATE_TIME
        defaultWxMaUserShouldNotBeFound("createTime.greaterOrEqualThan=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByCreateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where createTime less than or equals to DEFAULT_CREATE_TIME
        defaultWxMaUserShouldNotBeFound("createTime.lessThan=" + DEFAULT_CREATE_TIME);

        // Get all the wxMaUserList where createTime less than or equals to UPDATED_CREATE_TIME
        defaultWxMaUserShouldBeFound("createTime.lessThan=" + UPDATED_CREATE_TIME);
    }


    @Test
    @Transactional
    public void getAllWxMaUsersByUpdateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where updateTime equals to DEFAULT_UPDATE_TIME
        defaultWxMaUserShouldBeFound("updateTime.equals=" + DEFAULT_UPDATE_TIME);

        // Get all the wxMaUserList where updateTime equals to UPDATED_UPDATE_TIME
        defaultWxMaUserShouldNotBeFound("updateTime.equals=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByUpdateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where updateTime in DEFAULT_UPDATE_TIME or UPDATED_UPDATE_TIME
        defaultWxMaUserShouldBeFound("updateTime.in=" + DEFAULT_UPDATE_TIME + "," + UPDATED_UPDATE_TIME);

        // Get all the wxMaUserList where updateTime equals to UPDATED_UPDATE_TIME
        defaultWxMaUserShouldNotBeFound("updateTime.in=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByUpdateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where updateTime is not null
        defaultWxMaUserShouldBeFound("updateTime.specified=true");

        // Get all the wxMaUserList where updateTime is null
        defaultWxMaUserShouldNotBeFound("updateTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByUpdateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where updateTime greater than or equals to DEFAULT_UPDATE_TIME
        defaultWxMaUserShouldBeFound("updateTime.greaterOrEqualThan=" + DEFAULT_UPDATE_TIME);

        // Get all the wxMaUserList where updateTime greater than or equals to UPDATED_UPDATE_TIME
        defaultWxMaUserShouldNotBeFound("updateTime.greaterOrEqualThan=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByUpdateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where updateTime less than or equals to DEFAULT_UPDATE_TIME
        defaultWxMaUserShouldNotBeFound("updateTime.lessThan=" + DEFAULT_UPDATE_TIME);

        // Get all the wxMaUserList where updateTime less than or equals to UPDATED_UPDATE_TIME
        defaultWxMaUserShouldBeFound("updateTime.lessThan=" + UPDATED_UPDATE_TIME);
    }


    @Test
    @Transactional
    public void getAllWxMaUsersByPhoneIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where phone equals to DEFAULT_PHONE
        defaultWxMaUserShouldBeFound("phone.equals=" + DEFAULT_PHONE);

        // Get all the wxMaUserList where phone equals to UPDATED_PHONE
        defaultWxMaUserShouldNotBeFound("phone.equals=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByPhoneIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where phone in DEFAULT_PHONE or UPDATED_PHONE
        defaultWxMaUserShouldBeFound("phone.in=" + DEFAULT_PHONE + "," + UPDATED_PHONE);

        // Get all the wxMaUserList where phone equals to UPDATED_PHONE
        defaultWxMaUserShouldNotBeFound("phone.in=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByPhoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where phone is not null
        defaultWxMaUserShouldBeFound("phone.specified=true");

        // Get all the wxMaUserList where phone is null
        defaultWxMaUserShouldNotBeFound("phone.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByAppIdIsEqualToSomething() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where appId equals to DEFAULT_APP_ID
        defaultWxMaUserShouldBeFound("appId.equals=" + DEFAULT_APP_ID);

        // Get all the wxMaUserList where appId equals to UPDATED_APP_ID
        defaultWxMaUserShouldNotBeFound("appId.equals=" + UPDATED_APP_ID);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByAppIdIsInShouldWork() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where appId in DEFAULT_APP_ID or UPDATED_APP_ID
        defaultWxMaUserShouldBeFound("appId.in=" + DEFAULT_APP_ID + "," + UPDATED_APP_ID);

        // Get all the wxMaUserList where appId equals to UPDATED_APP_ID
        defaultWxMaUserShouldNotBeFound("appId.in=" + UPDATED_APP_ID);
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByAppIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        // Get all the wxMaUserList where appId is not null
        defaultWxMaUserShouldBeFound("appId.specified=true");

        // Get all the wxMaUserList where appId is null
        defaultWxMaUserShouldNotBeFound("appId.specified=false");
    }

    @Test
    @Transactional
    public void getAllWxMaUsersByUserIsEqualToSomething() throws Exception {
        // Initialize the database
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        wxMaUser.setUser(user);
        wxMaUserRepository.saveAndFlush(wxMaUser);
        Long userId = user.getId();

        // Get all the wxMaUserList where user equals to userId
        defaultWxMaUserShouldBeFound("userId.equals=" + userId);

        // Get all the wxMaUserList where user equals to userId + 1
        defaultWxMaUserShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultWxMaUserShouldBeFound(String filter) throws Exception {
        restWxMaUserMockMvc.perform(get("/api/wx-ma-users?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(wxMaUser.getId().intValue())))
            .andExpect(jsonPath("$.[*].openId").value(hasItem(DEFAULT_OPEN_ID)))
            .andExpect(jsonPath("$.[*].nickName").value(hasItem(DEFAULT_NICK_NAME)))
            .andExpect(jsonPath("$.[*].gender").value(hasItem(DEFAULT_GENDER)))
            .andExpect(jsonPath("$.[*].language").value(hasItem(DEFAULT_LANGUAGE)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].province").value(hasItem(DEFAULT_PROVINCE)))
            .andExpect(jsonPath("$.[*].country").value(hasItem(DEFAULT_COUNTRY)))
            .andExpect(jsonPath("$.[*].avatarUrl").value(hasItem(DEFAULT_AVATAR_URL)))
            .andExpect(jsonPath("$.[*].unionId").value(hasItem(DEFAULT_UNION_ID)))
            .andExpect(jsonPath("$.[*].watermark").value(hasItem(DEFAULT_WATERMARK)))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].appId").value(hasItem(DEFAULT_APP_ID)));

        // Check, that the count call also returns 1
        restWxMaUserMockMvc.perform(get("/api/wx-ma-users/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultWxMaUserShouldNotBeFound(String filter) throws Exception {
        restWxMaUserMockMvc.perform(get("/api/wx-ma-users?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restWxMaUserMockMvc.perform(get("/api/wx-ma-users/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingWxMaUser() throws Exception {
        // Get the wxMaUser
        restWxMaUserMockMvc.perform(get("/api/wx-ma-users/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateWxMaUser() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        int databaseSizeBeforeUpdate = wxMaUserRepository.findAll().size();

        // Update the wxMaUser
        WxMaUser updatedWxMaUser = wxMaUserRepository.findById(wxMaUser.getId()).get();
        // Disconnect from session so that the updates on updatedWxMaUser are not directly saved in db
        em.detach(updatedWxMaUser);
        updatedWxMaUser
            .openId(UPDATED_OPEN_ID)
            .nickName(UPDATED_NICK_NAME)
            .gender(UPDATED_GENDER)
            .language(UPDATED_LANGUAGE)
            .city(UPDATED_CITY)
            .province(UPDATED_PROVINCE)
            .country(UPDATED_COUNTRY)
            .avatarUrl(UPDATED_AVATAR_URL)
            .unionId(UPDATED_UNION_ID)
            .watermark(UPDATED_WATERMARK)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME)
            .phone(UPDATED_PHONE)
            .appId(UPDATED_APP_ID);
        WxMaUserDTO wxMaUserDTO = wxMaUserMapper.toDto(updatedWxMaUser);

        restWxMaUserMockMvc.perform(put("/api/wx-ma-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wxMaUserDTO)))
            .andExpect(status().isOk());

        // Validate the WxMaUser in the database
        List<WxMaUser> wxMaUserList = wxMaUserRepository.findAll();
        assertThat(wxMaUserList).hasSize(databaseSizeBeforeUpdate);
        WxMaUser testWxMaUser = wxMaUserList.get(wxMaUserList.size() - 1);
        assertThat(testWxMaUser.getOpenId()).isEqualTo(UPDATED_OPEN_ID);
        assertThat(testWxMaUser.getNickName()).isEqualTo(UPDATED_NICK_NAME);
        assertThat(testWxMaUser.getGender()).isEqualTo(UPDATED_GENDER);
        assertThat(testWxMaUser.getLanguage()).isEqualTo(UPDATED_LANGUAGE);
        assertThat(testWxMaUser.getCity()).isEqualTo(UPDATED_CITY);
        assertThat(testWxMaUser.getProvince()).isEqualTo(UPDATED_PROVINCE);
        assertThat(testWxMaUser.getCountry()).isEqualTo(UPDATED_COUNTRY);
        assertThat(testWxMaUser.getAvatarUrl()).isEqualTo(UPDATED_AVATAR_URL);
        assertThat(testWxMaUser.getUnionId()).isEqualTo(UPDATED_UNION_ID);
        assertThat(testWxMaUser.getWatermark()).isEqualTo(UPDATED_WATERMARK);
        assertThat(testWxMaUser.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testWxMaUser.getUpdateTime()).isEqualTo(UPDATED_UPDATE_TIME);
        assertThat(testWxMaUser.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testWxMaUser.getAppId()).isEqualTo(UPDATED_APP_ID);
    }

    @Test
    @Transactional
    public void updateNonExistingWxMaUser() throws Exception {
        int databaseSizeBeforeUpdate = wxMaUserRepository.findAll().size();

        // Create the WxMaUser
        WxMaUserDTO wxMaUserDTO = wxMaUserMapper.toDto(wxMaUser);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWxMaUserMockMvc.perform(put("/api/wx-ma-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wxMaUserDTO)))
            .andExpect(status().isBadRequest());

        // Validate the WxMaUser in the database
        List<WxMaUser> wxMaUserList = wxMaUserRepository.findAll();
        assertThat(wxMaUserList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteWxMaUser() throws Exception {
        // Initialize the database
        wxMaUserRepository.saveAndFlush(wxMaUser);

        int databaseSizeBeforeDelete = wxMaUserRepository.findAll().size();

        // Delete the wxMaUser
        restWxMaUserMockMvc.perform(delete("/api/wx-ma-users/{id}", wxMaUser.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<WxMaUser> wxMaUserList = wxMaUserRepository.findAll();
        assertThat(wxMaUserList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(WxMaUser.class);
        WxMaUser wxMaUser1 = new WxMaUser();
        wxMaUser1.setId(1L);
        WxMaUser wxMaUser2 = new WxMaUser();
        wxMaUser2.setId(wxMaUser1.getId());
        assertThat(wxMaUser1).isEqualTo(wxMaUser2);
        wxMaUser2.setId(2L);
        assertThat(wxMaUser1).isNotEqualTo(wxMaUser2);
        wxMaUser1.setId(null);
        assertThat(wxMaUser1).isNotEqualTo(wxMaUser2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(WxMaUserDTO.class);
        WxMaUserDTO wxMaUserDTO1 = new WxMaUserDTO();
        wxMaUserDTO1.setId(1L);
        WxMaUserDTO wxMaUserDTO2 = new WxMaUserDTO();
        assertThat(wxMaUserDTO1).isNotEqualTo(wxMaUserDTO2);
        wxMaUserDTO2.setId(wxMaUserDTO1.getId());
        assertThat(wxMaUserDTO1).isEqualTo(wxMaUserDTO2);
        wxMaUserDTO2.setId(2L);
        assertThat(wxMaUserDTO1).isNotEqualTo(wxMaUserDTO2);
        wxMaUserDTO1.setId(null);
        assertThat(wxMaUserDTO1).isNotEqualTo(wxMaUserDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(wxMaUserMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(wxMaUserMapper.fromId(null)).isNull();
    }
}
