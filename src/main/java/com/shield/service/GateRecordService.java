package com.shield.service;

import com.shield.domain.GateRecord;
import com.shield.repository.GateRecordRepository;
import com.shield.service.dto.GateRecordDTO;
import com.shield.service.mapper.GateRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link GateRecord}.
 */
@Service
@Transactional
public class GateRecordService {

    private final Logger log = LoggerFactory.getLogger(GateRecordService.class);

    private final GateRecordRepository gateRecordRepository;

    private final GateRecordMapper gateRecordMapper;

    public GateRecordService(GateRecordRepository gateRecordRepository, GateRecordMapper gateRecordMapper) {
        this.gateRecordRepository = gateRecordRepository;
        this.gateRecordMapper = gateRecordMapper;
    }

    /**
     * Save a gateRecord.
     *
     * @param gateRecordDTO the entity to save.
     * @return the persisted entity.
     */
    public GateRecordDTO save(GateRecordDTO gateRecordDTO) {
        log.debug("Request to save GateRecord : {}", gateRecordDTO);
        GateRecord gateRecord = gateRecordMapper.toEntity(gateRecordDTO);
        gateRecord = gateRecordRepository.save(gateRecord);
        return gateRecordMapper.toDto(gateRecord);
    }

    /**
     * Get all the gateRecords.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<GateRecordDTO> findAll(Pageable pageable) {
        log.debug("Request to get all GateRecords");
        return gateRecordRepository.findAll(pageable)
            .map(gateRecordMapper::toDto);
    }


    /**
     * Get one gateRecord by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<GateRecordDTO> findOne(Long id) {
        log.debug("Request to get GateRecord : {}", id);
        return gateRecordRepository.findById(id)
            .map(gateRecordMapper::toDto);
    }

    /**
     * Delete the gateRecord by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete GateRecord : {}", id);
        gateRecordRepository.deleteById(id);
    }
}
