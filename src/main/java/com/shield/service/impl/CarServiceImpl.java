package com.shield.service.impl;

import com.shield.domain.User;
import com.shield.service.CarService;
import com.shield.domain.Car;
import com.shield.repository.CarRepository;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.CarDTO;
import com.shield.service.mapper.CarMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Car}.
 */
@Service
@Transactional
public class CarServiceImpl implements CarService {

    private final Logger log = LoggerFactory.getLogger(CarServiceImpl.class);

    private final CarRepository carRepository;

    private final CarMapper carMapper;

    public CarServiceImpl(CarRepository carRepository, CarMapper carMapper) {
        this.carRepository = carRepository;
        this.carMapper = carMapper;
    }

    @Override
    public List<CarDTO> getByUserId(Long userId) {
        return this.carRepository.findByUserId(userId).stream().map(carMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public CarDTO findOrCreateAppointmentCarInfo(AppointmentDTO appointmentDTO) {
        if (appointmentDTO.getUserId() != null) {
            List<Car> cars = this.carRepository.findByUserId(appointmentDTO.getUserId());
            cars = cars.stream()
                .filter(it -> it.getLicensePlateNumber() != null && it.getLicensePlateNumber().equals(appointmentDTO.getLicensePlateNumber())
                    && it.getDriver() != null && it.getDriver().equals(appointmentDTO.getDriver())
                    && it.getPhone() != null
                ).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(cars)) {
                Car car = new Car();
                User user = new User();
                user.setId(appointmentDTO.getUserId());
                car.setUser(user);
                car.setLicensePlateNumber(appointmentDTO.getLicensePlateNumber());
                car.setDriver(appointmentDTO.getDriver());
                car.setCreateTime(ZonedDateTime.now());
                car.setUpdateTime(ZonedDateTime.now());
                car = carRepository.save(car);
                return this.carMapper.toDto(car);
            } else {
                return this.carMapper.toDto(cars.get(0));
            }
        }
        return null;
    }

    /**
     * Save a car.
     *
     * @param carDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public CarDTO save(CarDTO carDTO) {
        log.debug("Request to save Car : {}", carDTO);
        Car car = carMapper.toEntity(carDTO);
        car = carRepository.save(car);
        return carMapper.toDto(car);
    }

    /**
     * Get all the cars.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CarDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Cars");
        return carRepository.findAll(pageable)
            .map(carMapper::toDto);
    }


    /**
     * Get one car by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CarDTO> findOne(Long id) {
        log.debug("Request to get Car : {}", id);
        return carRepository.findById(id)
            .map(carMapper::toDto);
    }

    /**
     * Delete the car by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Car : {}", id);
        carRepository.deleteById(id);
    }
}
