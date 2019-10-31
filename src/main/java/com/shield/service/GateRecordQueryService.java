package com.shield.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import com.shield.domain.GateRecord;
import com.shield.domain.*; // for static metamodels
import com.shield.repository.GateRecordRepository;
import com.shield.service.dto.GateRecordCriteria;
import com.shield.service.dto.GateRecordDTO;
import com.shield.service.mapper.GateRecordMapper;

/**
 * Service for executing complex queries for {@link GateRecord} entities in the database.
 * The main input is a {@link GateRecordCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link GateRecordDTO} or a {@link Page} of {@link GateRecordDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class GateRecordQueryService extends QueryService<GateRecord> {

    private final Logger log = LoggerFactory.getLogger(GateRecordQueryService.class);

    private final GateRecordRepository gateRecordRepository;

    private final GateRecordMapper gateRecordMapper;

    public GateRecordQueryService(GateRecordRepository gateRecordRepository, GateRecordMapper gateRecordMapper) {
        this.gateRecordRepository = gateRecordRepository;
        this.gateRecordMapper = gateRecordMapper;
    }

    /**
     * Return a {@link List} of {@link GateRecordDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<GateRecordDTO> findByCriteria(GateRecordCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<GateRecord> specification = createSpecification(criteria);
        return gateRecordMapper.toDto(gateRecordRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link GateRecordDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<GateRecordDTO> findByCriteria(GateRecordCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<GateRecord> specification = createSpecification(criteria);
        return gateRecordRepository.findAll(specification, page)
            .map(gateRecordMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(GateRecordCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<GateRecord> specification = createSpecification(criteria);
        return gateRecordRepository.count(specification);
    }

    /**
     * Function to convert GateRecordCriteria to a {@link Specification}.
     */
    private Specification<GateRecord> createSpecification(GateRecordCriteria criteria) {
        Specification<GateRecord> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), GateRecord_.id));
            }
            if (criteria.getRecordType() != null) {
                specification = specification.and(buildSpecification(criteria.getRecordType(), GateRecord_.recordType));
            }
            if (criteria.getTruckNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTruckNumber(), GateRecord_.truckNumber));
            }
            if (criteria.getRecordTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRecordTime(), GateRecord_.recordTime));
            }
            if (criteria.getRid() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRid(), GateRecord_.rid));
            }
            if (criteria.getCreateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreateTime(), GateRecord_.createTime));
            }
            if (criteria.getRegionId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRegionId(), GateRecord_.regionId));
            }
            if (criteria.getDataMd5() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDataMd5(), GateRecord_.dataMd5));
            }
            if (criteria.getModifyTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getModifyTime(), GateRecord_.modifyTime));
            }
        }
        return specification;
    }
}
