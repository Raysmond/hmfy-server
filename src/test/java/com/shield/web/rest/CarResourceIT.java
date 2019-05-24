package com.shield.web.rest;

import com.shield.ShieldApp;
import com.shield.domain.Car;
import com.shield.domain.User;
import com.shield.repository.CarRepository;
import com.shield.service.CarService;
import com.shield.service.dto.CarDTO;
import com.shield.service.mapper.CarMapper;
import com.shield.web.rest.errors.ExceptionTranslator;
import com.shield.service.dto.CarCriteria;
import com.shield.service.CarQueryService;

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
 * Integration tests for the {@Link CarResource} REST controller.
 */
@SpringBootTest(classes = ShieldApp.class)
public class CarResourceIT {

    private static final String DEFAULT_LICENSE_PLATE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_LICENSE_PLATE_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_DRIVER = "AAAAAAAAAA";
    private static final String UPDATED_DRIVER = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private CarService carService;

    @Autowired
    private CarQueryService carQueryService;

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

    private MockMvc restCarMockMvc;

    private Car car;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CarResource carResource = new CarResource(carService, carQueryService);
        this.restCarMockMvc = MockMvcBuilders.standaloneSetup(carResource)
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
    public static Car createEntity(EntityManager em) {
        Car car = new Car()
            .licensePlateNumber(DEFAULT_LICENSE_PLATE_NUMBER)
            .driver(DEFAULT_DRIVER)
            .createTime(DEFAULT_CREATE_TIME)
            .updateTime(DEFAULT_UPDATE_TIME);
        // Add required entity
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        car.setUser(user);
        return car;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Car createUpdatedEntity(EntityManager em) {
        Car car = new Car()
            .licensePlateNumber(UPDATED_LICENSE_PLATE_NUMBER)
            .driver(UPDATED_DRIVER)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME);
        // Add required entity
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        car.setUser(user);
        return car;
    }

    @BeforeEach
    public void initTest() {
        car = createEntity(em);
    }

    @Test
    @Transactional
    public void createCar() throws Exception {
        int databaseSizeBeforeCreate = carRepository.findAll().size();

        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);
        restCarMockMvc.perform(post("/api/cars")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isCreated());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeCreate + 1);
        Car testCar = carList.get(carList.size() - 1);
        assertThat(testCar.getLicensePlateNumber()).isEqualTo(DEFAULT_LICENSE_PLATE_NUMBER);
        assertThat(testCar.getDriver()).isEqualTo(DEFAULT_DRIVER);
        assertThat(testCar.getCreateTime()).isEqualTo(DEFAULT_CREATE_TIME);
        assertThat(testCar.getUpdateTime()).isEqualTo(DEFAULT_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void createCarWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = carRepository.findAll().size();

        // Create the Car with an existing ID
        car.setId(1L);
        CarDTO carDTO = carMapper.toDto(car);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCarMockMvc.perform(post("/api/cars")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkLicensePlateNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = carRepository.findAll().size();
        // set the field null
        car.setLicensePlateNumber(null);

        // Create the Car, which fails.
        CarDTO carDTO = carMapper.toDto(car);

        restCarMockMvc.perform(post("/api/cars")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isBadRequest());

        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDriverIsRequired() throws Exception {
        int databaseSizeBeforeTest = carRepository.findAll().size();
        // set the field null
        car.setDriver(null);

        // Create the Car, which fails.
        CarDTO carDTO = carMapper.toDto(car);

        restCarMockMvc.perform(post("/api/cars")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isBadRequest());

        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCars() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList
        restCarMockMvc.perform(get("/api/cars?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].licensePlateNumber").value(hasItem(DEFAULT_LICENSE_PLATE_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].driver").value(hasItem(DEFAULT_DRIVER.toString())))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))));
    }
    
    @Test
    @Transactional
    public void getCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get the car
        restCarMockMvc.perform(get("/api/cars/{id}", car.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(car.getId().intValue()))
            .andExpect(jsonPath("$.licensePlateNumber").value(DEFAULT_LICENSE_PLATE_NUMBER.toString()))
            .andExpect(jsonPath("$.driver").value(DEFAULT_DRIVER.toString()))
            .andExpect(jsonPath("$.createTime").value(sameInstant(DEFAULT_CREATE_TIME)))
            .andExpect(jsonPath("$.updateTime").value(sameInstant(DEFAULT_UPDATE_TIME)));
    }

    @Test
    @Transactional
    public void getAllCarsByLicensePlateNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where licensePlateNumber equals to DEFAULT_LICENSE_PLATE_NUMBER
        defaultCarShouldBeFound("licensePlateNumber.equals=" + DEFAULT_LICENSE_PLATE_NUMBER);

        // Get all the carList where licensePlateNumber equals to UPDATED_LICENSE_PLATE_NUMBER
        defaultCarShouldNotBeFound("licensePlateNumber.equals=" + UPDATED_LICENSE_PLATE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllCarsByLicensePlateNumberIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where licensePlateNumber in DEFAULT_LICENSE_PLATE_NUMBER or UPDATED_LICENSE_PLATE_NUMBER
        defaultCarShouldBeFound("licensePlateNumber.in=" + DEFAULT_LICENSE_PLATE_NUMBER + "," + UPDATED_LICENSE_PLATE_NUMBER);

        // Get all the carList where licensePlateNumber equals to UPDATED_LICENSE_PLATE_NUMBER
        defaultCarShouldNotBeFound("licensePlateNumber.in=" + UPDATED_LICENSE_PLATE_NUMBER);
    }

    @Test
    @Transactional
    public void getAllCarsByLicensePlateNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where licensePlateNumber is not null
        defaultCarShouldBeFound("licensePlateNumber.specified=true");

        // Get all the carList where licensePlateNumber is null
        defaultCarShouldNotBeFound("licensePlateNumber.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarsByDriverIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where driver equals to DEFAULT_DRIVER
        defaultCarShouldBeFound("driver.equals=" + DEFAULT_DRIVER);

        // Get all the carList where driver equals to UPDATED_DRIVER
        defaultCarShouldNotBeFound("driver.equals=" + UPDATED_DRIVER);
    }

    @Test
    @Transactional
    public void getAllCarsByDriverIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where driver in DEFAULT_DRIVER or UPDATED_DRIVER
        defaultCarShouldBeFound("driver.in=" + DEFAULT_DRIVER + "," + UPDATED_DRIVER);

        // Get all the carList where driver equals to UPDATED_DRIVER
        defaultCarShouldNotBeFound("driver.in=" + UPDATED_DRIVER);
    }

    @Test
    @Transactional
    public void getAllCarsByDriverIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where driver is not null
        defaultCarShouldBeFound("driver.specified=true");

        // Get all the carList where driver is null
        defaultCarShouldNotBeFound("driver.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarsByCreateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where createTime equals to DEFAULT_CREATE_TIME
        defaultCarShouldBeFound("createTime.equals=" + DEFAULT_CREATE_TIME);

        // Get all the carList where createTime equals to UPDATED_CREATE_TIME
        defaultCarShouldNotBeFound("createTime.equals=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllCarsByCreateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where createTime in DEFAULT_CREATE_TIME or UPDATED_CREATE_TIME
        defaultCarShouldBeFound("createTime.in=" + DEFAULT_CREATE_TIME + "," + UPDATED_CREATE_TIME);

        // Get all the carList where createTime equals to UPDATED_CREATE_TIME
        defaultCarShouldNotBeFound("createTime.in=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllCarsByCreateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where createTime is not null
        defaultCarShouldBeFound("createTime.specified=true");

        // Get all the carList where createTime is null
        defaultCarShouldNotBeFound("createTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarsByCreateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where createTime greater than or equals to DEFAULT_CREATE_TIME
        defaultCarShouldBeFound("createTime.greaterOrEqualThan=" + DEFAULT_CREATE_TIME);

        // Get all the carList where createTime greater than or equals to UPDATED_CREATE_TIME
        defaultCarShouldNotBeFound("createTime.greaterOrEqualThan=" + UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllCarsByCreateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where createTime less than or equals to DEFAULT_CREATE_TIME
        defaultCarShouldNotBeFound("createTime.lessThan=" + DEFAULT_CREATE_TIME);

        // Get all the carList where createTime less than or equals to UPDATED_CREATE_TIME
        defaultCarShouldBeFound("createTime.lessThan=" + UPDATED_CREATE_TIME);
    }


    @Test
    @Transactional
    public void getAllCarsByUpdateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where updateTime equals to DEFAULT_UPDATE_TIME
        defaultCarShouldBeFound("updateTime.equals=" + DEFAULT_UPDATE_TIME);

        // Get all the carList where updateTime equals to UPDATED_UPDATE_TIME
        defaultCarShouldNotBeFound("updateTime.equals=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllCarsByUpdateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where updateTime in DEFAULT_UPDATE_TIME or UPDATED_UPDATE_TIME
        defaultCarShouldBeFound("updateTime.in=" + DEFAULT_UPDATE_TIME + "," + UPDATED_UPDATE_TIME);

        // Get all the carList where updateTime equals to UPDATED_UPDATE_TIME
        defaultCarShouldNotBeFound("updateTime.in=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllCarsByUpdateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where updateTime is not null
        defaultCarShouldBeFound("updateTime.specified=true");

        // Get all the carList where updateTime is null
        defaultCarShouldNotBeFound("updateTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarsByUpdateTimeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where updateTime greater than or equals to DEFAULT_UPDATE_TIME
        defaultCarShouldBeFound("updateTime.greaterOrEqualThan=" + DEFAULT_UPDATE_TIME);

        // Get all the carList where updateTime greater than or equals to UPDATED_UPDATE_TIME
        defaultCarShouldNotBeFound("updateTime.greaterOrEqualThan=" + UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void getAllCarsByUpdateTimeIsLessThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where updateTime less than or equals to DEFAULT_UPDATE_TIME
        defaultCarShouldNotBeFound("updateTime.lessThan=" + DEFAULT_UPDATE_TIME);

        // Get all the carList where updateTime less than or equals to UPDATED_UPDATE_TIME
        defaultCarShouldBeFound("updateTime.lessThan=" + UPDATED_UPDATE_TIME);
    }


    @Test
    @Transactional
    public void getAllCarsByUserIsEqualToSomething() throws Exception {
        // Get already existing entity
        User user = car.getUser();
        carRepository.saveAndFlush(car);
        Long userId = user.getId();

        // Get all the carList where user equals to userId
        defaultCarShouldBeFound("userId.equals=" + userId);

        // Get all the carList where user equals to userId + 1
        defaultCarShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCarShouldBeFound(String filter) throws Exception {
        restCarMockMvc.perform(get("/api/cars?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].licensePlateNumber").value(hasItem(DEFAULT_LICENSE_PLATE_NUMBER)))
            .andExpect(jsonPath("$.[*].driver").value(hasItem(DEFAULT_DRIVER)))
            .andExpect(jsonPath("$.[*].createTime").value(hasItem(sameInstant(DEFAULT_CREATE_TIME))))
            .andExpect(jsonPath("$.[*].updateTime").value(hasItem(sameInstant(DEFAULT_UPDATE_TIME))));

        // Check, that the count call also returns 1
        restCarMockMvc.perform(get("/api/cars/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCarShouldNotBeFound(String filter) throws Exception {
        restCarMockMvc.perform(get("/api/cars?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCarMockMvc.perform(get("/api/cars/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingCar() throws Exception {
        // Get the car
        restCarMockMvc.perform(get("/api/cars/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        int databaseSizeBeforeUpdate = carRepository.findAll().size();

        // Update the car
        Car updatedCar = carRepository.findById(car.getId()).get();
        // Disconnect from session so that the updates on updatedCar are not directly saved in db
        em.detach(updatedCar);
        updatedCar
            .licensePlateNumber(UPDATED_LICENSE_PLATE_NUMBER)
            .driver(UPDATED_DRIVER)
            .createTime(UPDATED_CREATE_TIME)
            .updateTime(UPDATED_UPDATE_TIME);
        CarDTO carDTO = carMapper.toDto(updatedCar);

        restCarMockMvc.perform(put("/api/cars")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isOk());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);
        Car testCar = carList.get(carList.size() - 1);
        assertThat(testCar.getLicensePlateNumber()).isEqualTo(UPDATED_LICENSE_PLATE_NUMBER);
        assertThat(testCar.getDriver()).isEqualTo(UPDATED_DRIVER);
        assertThat(testCar.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
        assertThat(testCar.getUpdateTime()).isEqualTo(UPDATED_UPDATE_TIME);
    }

    @Test
    @Transactional
    public void updateNonExistingCar() throws Exception {
        int databaseSizeBeforeUpdate = carRepository.findAll().size();

        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarMockMvc.perform(put("/api/cars")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        int databaseSizeBeforeDelete = carRepository.findAll().size();

        // Delete the car
        restCarMockMvc.perform(delete("/api/cars/{id}", car.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Car.class);
        Car car1 = new Car();
        car1.setId(1L);
        Car car2 = new Car();
        car2.setId(car1.getId());
        assertThat(car1).isEqualTo(car2);
        car2.setId(2L);
        assertThat(car1).isNotEqualTo(car2);
        car1.setId(null);
        assertThat(car1).isNotEqualTo(car2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CarDTO.class);
        CarDTO carDTO1 = new CarDTO();
        carDTO1.setId(1L);
        CarDTO carDTO2 = new CarDTO();
        assertThat(carDTO1).isNotEqualTo(carDTO2);
        carDTO2.setId(carDTO1.getId());
        assertThat(carDTO1).isEqualTo(carDTO2);
        carDTO2.setId(2L);
        assertThat(carDTO1).isNotEqualTo(carDTO2);
        carDTO1.setId(null);
        assertThat(carDTO1).isNotEqualTo(carDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(carMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(carMapper.fromId(null)).isNull();
    }
}
