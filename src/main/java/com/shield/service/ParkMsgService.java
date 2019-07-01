package com.shield.service;

import com.shield.service.dto.ParkMsgDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link com.shield.domain.ParkMsg}.
 */
public interface ParkMsgService {

    /**
     * Save a parkMsg.
     *
     * @param parkMsgDTO the entity to save.
     * @return the persisted entity.
     */
    ParkMsgDTO save(ParkMsgDTO parkMsgDTO);

    /**
     * Get all the parkMsgs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ParkMsgDTO> findAll(Pageable pageable);


    /**
     * Get the "id" parkMsg.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ParkMsgDTO> findOne(Long id);

    /**
     * Delete the "id" parkMsg.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    Page<ParkMsgDTO> findAllByService(Pageable pageable, String service);
}
