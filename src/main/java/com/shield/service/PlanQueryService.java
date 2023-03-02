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

import com.shield.domain.Plan;
import com.shield.domain.*; // for static metamodels
import com.shield.repository.PlanRepository;
import com.shield.service.dto.PlanCriteria;
import com.shield.service.dto.PlanDTO;
import com.shield.service.mapper.PlanMapper;

/**
 * Service for executing complex queries for {@link Plan} entities in the database.
 * The main input is a {@link PlanCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link PlanDTO} or a {@link Page} of {@link PlanDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PlanQueryService extends QueryService<Plan> {

    private final Logger log = LoggerFactory.getLogger(PlanQueryService.class);

    private final PlanRepository planRepository;

    private final PlanMapper planMapper;

    public PlanQueryService(PlanRepository planRepository, PlanMapper planMapper) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
    }

    /**
     * Return a {@link List} of {@link PlanDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<PlanDTO> findByCriteria(PlanCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Plan> specification = createSpecification(criteria);
        return planMapper.toDto(planRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link PlanDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<PlanDTO> findByCriteria(PlanCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Plan> specification = createSpecification(criteria);
        return planRepository.findAll(specification, page)
            .map(planMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(PlanCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Plan> specification = createSpecification(criteria);
        return planRepository.count(specification);
    }

    /**
     * Function to convert PlanCriteria to a {@link Specification}.
     */
    private Specification<Plan> createSpecification(PlanCriteria criteria) {
        Specification<Plan> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Plan_.id));
            }
            if (criteria.getPlanNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPlanNumber(), Plan_.planNumber));
            }
            if (criteria.getLocation() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLocation(), Plan_.location));
            }
            if (criteria.getWorkDay() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getWorkDay(), Plan_.workDay));
            }
            if (criteria.getStockName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getStockName(), Plan_.stockName));
            }
            if (criteria.getLoadingStartTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLoadingStartTime(), Plan_.loadingStartTime));
            }
            if (criteria.getLoadingEndTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLoadingEndTime(), Plan_.loadingEndTime));
            }
            if (criteria.getWeightSum() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getWeightSum(), Plan_.weightSum));
            }
            if (criteria.getOperator() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOperator(), Plan_.operator));
            }
            if (criteria.getOperation() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOperation(), Plan_.operation));
            }
            if (criteria.getOpPosition() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOpPosition(), Plan_.opPosition));
            }
            if (criteria.getChannel() != null) {
                specification = specification.and(buildStringSpecification(criteria.getChannel(), Plan_.channel));
            }
            if (criteria.getComment() != null) {
                specification = specification.and(buildStringSpecification(criteria.getComment(), Plan_.comment));
            }
            if (criteria.getCreateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreateTime(), Plan_.createTime));
            }
            if (criteria.getUpdateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdateTime(), Plan_.updateTime));
            }
        }
        return specification;
    }
}
