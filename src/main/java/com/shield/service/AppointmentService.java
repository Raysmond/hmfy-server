package com.shield.service;

import com.shield.service.dto.AppointmentDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link com.shield.domain.Appointment}.
 */
public interface AppointmentService {

    /**
     * Save a appointment.
     *
     * @param appointmentDTO the entity to save.
     * @return the persisted entity.
     */
    AppointmentDTO save(AppointmentDTO appointmentDTO);

    /**
     * Get all the appointments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AppointmentDTO> findAll(Pageable pageable);


    /**
     * Get the "id" appointment.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AppointmentDTO> findOne(Long id);

    /**
     * Delete the "id" appointment.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
