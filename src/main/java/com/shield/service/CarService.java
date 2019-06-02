package com.shield.service;

import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.CarDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.shield.domain.Car}.
 */
public interface CarService {

    List<CarDTO> getByUserId(Long userId);

    CarDTO findOrCreateAppointmentCarInfo(AppointmentDTO appointmentDTO);

    /**
     * Save a car.
     *
     * @param carDTO the entity to save.
     * @return the persisted entity.
     */
    CarDTO save(CarDTO carDTO);

    /**
     * Get all the cars.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CarDTO> findAll(Pageable pageable);


    /**
     * Get the "id" car.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CarDTO> findOne(Long id);

    /**
     * Delete the "id" car.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
