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

import com.shield.domain.ShipPlan;
import com.shield.domain.*; // for static metamodels
import com.shield.repository.ShipPlanRepository;
import com.shield.service.dto.ShipPlanCriteria;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.mapper.ShipPlanMapper;

/**
 * Service for executing complex queries for {@link ShipPlan} entities in the database.
 * The main input is a {@link ShipPlanCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ShipPlanDTO} or a {@link Page} of {@link ShipPlanDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ShipPlanQueryService extends QueryService<ShipPlan> {

    private final Logger log = LoggerFactory.getLogger(ShipPlanQueryService.class);

    private final ShipPlanRepository shipPlanRepository;

    private final ShipPlanMapper shipPlanMapper;

    public ShipPlanQueryService(ShipPlanRepository shipPlanRepository, ShipPlanMapper shipPlanMapper) {
        this.shipPlanRepository = shipPlanRepository;
        this.shipPlanMapper = shipPlanMapper;
    }

    /**
     * Return a {@link List} of {@link ShipPlanDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ShipPlanDTO> findByCriteria(ShipPlanCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ShipPlan> specification = createSpecification(criteria);
        return shipPlanMapper.toDto(shipPlanRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link ShipPlanDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ShipPlanDTO> findByCriteria(ShipPlanCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ShipPlan> specification = createSpecification(criteria);
        return shipPlanRepository.findAll(specification, page)
            .map(shipPlanMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ShipPlanCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<ShipPlan> specification = createSpecification(criteria);
        return shipPlanRepository.count(specification);
    }

    /**
     * Function to convert ShipPlanCriteria to a {@link Specification}.
     */
    private Specification<ShipPlan> createSpecification(ShipPlanCriteria criteria) {
        Specification<ShipPlan> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), ShipPlan_.id));
            }
            if (criteria.getCompany() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCompany(), ShipPlan_.company));
            }
            if (criteria.getApplyId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getApplyId(), ShipPlan_.applyId));
            }
            if (criteria.getApplyNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getApplyNumber(), ShipPlan_.applyNumber));
            }
            if (criteria.getTruckNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTruckNumber(), ShipPlan_.truckNumber));
            }
            if (criteria.getAuditStatus() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAuditStatus(), ShipPlan_.auditStatus));
            }
            if (criteria.getProductName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getProductName(), ShipPlan_.productName));
            }
            if (criteria.getDeliverPosition() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDeliverPosition(), ShipPlan_.deliverPosition));
            }
            if (criteria.getValid() != null) {
                specification = specification.and(buildSpecification(criteria.getValid(), ShipPlan_.valid));
            }
            if (criteria.getGateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getGateTime(), ShipPlan_.gateTime));
            }
            if (criteria.getLeaveTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLeaveTime(), ShipPlan_.leaveTime));
            }
            if (criteria.getDeliverTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDeliverTime(), ShipPlan_.deliverTime));
            }
            if (criteria.getAllowInTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAllowInTime(), ShipPlan_.allowInTime));
            }
            if (criteria.getLoadingStartTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLoadingStartTime(), ShipPlan_.loadingStartTime));
            }
            if (criteria.getLoadingEndTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLoadingEndTime(), ShipPlan_.loadingEndTime));
            }
            if (criteria.getCreateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreateTime(), ShipPlan_.createTime));
            }
            if (criteria.getUpdateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdateTime(), ShipPlan_.updateTime));
            }
            if (criteria.getSyncTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSyncTime(), ShipPlan_.syncTime));
            }
            if (criteria.getTareAlert() != null) {
                specification = specification.and(buildSpecification(criteria.getTareAlert(), ShipPlan_.tareAlert));
            }
            if (criteria.getLeaveAlert() != null) {
                specification = specification.and(buildSpecification(criteria.getLeaveAlert(), ShipPlan_.leaveAlert));
            }
            if (criteria.getNetWeight() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getNetWeight(), ShipPlan_.netWeight));
            }
            if (criteria.getWeigherNo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getWeigherNo(), ShipPlan_.weigherNo));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(buildSpecification(criteria.getUserId(),
                    root -> root.join(ShipPlan_.user, JoinType.LEFT).get(User_.id)));
            }
        }
        return specification;
    }
}
