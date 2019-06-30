package com.shield.service.impl;

import com.shield.service.ParkMsgService;
import com.shield.domain.ParkMsg;
import com.shield.repository.ParkMsgRepository;
import com.shield.service.dto.ParkMsgDTO;
import com.shield.service.mapper.ParkMsgMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link ParkMsg}.
 */
@Service
@Transactional
public class ParkMsgServiceImpl implements ParkMsgService {

    private final Logger log = LoggerFactory.getLogger(ParkMsgServiceImpl.class);

    private final ParkMsgRepository parkMsgRepository;

    private final ParkMsgMapper parkMsgMapper;

    public ParkMsgServiceImpl(ParkMsgRepository parkMsgRepository, ParkMsgMapper parkMsgMapper) {
        this.parkMsgRepository = parkMsgRepository;
        this.parkMsgMapper = parkMsgMapper;
    }

    /**
     * Save a parkMsg.
     *
     * @param parkMsgDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public ParkMsgDTO save(ParkMsgDTO parkMsgDTO) {
        log.debug("Request to save ParkMsg : {}", parkMsgDTO);
        ParkMsg parkMsg = parkMsgMapper.toEntity(parkMsgDTO);
        parkMsg = parkMsgRepository.save(parkMsg);
        return parkMsgMapper.toDto(parkMsg);
    }

    /**
     * Get all the parkMsgs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ParkMsgDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ParkMsgs");
        return parkMsgRepository.findAll(pageable)
            .map(parkMsgMapper::toDto);
    }


    /**
     * Get one parkMsg by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ParkMsgDTO> findOne(Long id) {
        log.debug("Request to get ParkMsg : {}", id);
        return parkMsgRepository.findById(id)
            .map(parkMsgMapper::toDto);
    }

    /**
     * Delete the parkMsg by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ParkMsg : {}", id);
        parkMsgRepository.deleteById(id);
    }
}
