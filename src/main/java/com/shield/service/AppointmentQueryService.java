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

import com.shield.domain.Appointment;
import com.shield.domain.*; // for static metamodels
import com.shield.repository.AppointmentRepository;
import com.shield.service.dto.AppointmentCriteria;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.mapper.AppointmentMapper;

/**
 * Service for executing complex queries for {@link Appointment} entities in the database.
 * The main input is a {@link AppointmentCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AppointmentDTO} or a {@link Page} of {@link AppointmentDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AppointmentQueryService extends QueryService<Appointment> {

    private final Logger log = LoggerFactory.getLogger(AppointmentQueryService.class);

    private final AppointmentRepository appointmentRepository;

    private final AppointmentMapper appointmentMapper;

    public AppointmentQueryService(AppointmentRepository appointmentRepository, AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
    }

    /**
     * Return a {@link List} of {@link AppointmentDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> findByCriteria(AppointmentCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Appointment> specification = createSpecification(criteria);
        return appointmentMapper.toDto(appointmentRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link AppointmentDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AppointmentDTO> findByCriteria(AppointmentCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Appointment> specification = createSpecification(criteria);
        return appointmentRepository.findAll(specification, page)
            .map(appointmentMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AppointmentCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Appointment> specification = createSpecification(criteria);
        return appointmentRepository.count(specification);
    }

    /**
     * Function to convert AppointmentCriteria to a {@link Specification}.
     */
    private Specification<Appointment> createSpecification(AppointmentCriteria criteria) {
        Specification<Appointment> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Appointment_.id));
            }
            if (criteria.getLicensePlateNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLicensePlateNumber(), Appointment_.licensePlateNumber));
            }
            if (criteria.getDriver() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDriver(), Appointment_.driver));
            }
            if (criteria.getNumber() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getNumber(), Appointment_.number));
            }
            if (criteria.getValid() != null) {
                specification = specification.and(buildSpecification(criteria.getValid(), Appointment_.valid));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), Appointment_.status));
            }
            if (criteria.getQueueNumber() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getQueueNumber(), Appointment_.queueNumber));
            }
            if (criteria.getVip() != null) {
                specification = specification.and(buildSpecification(criteria.getVip(), Appointment_.vip));
            }
            if (criteria.getCreateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreateTime(), Appointment_.createTime));
            }
            if (criteria.getUpdateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdateTime(), Appointment_.updateTime));
            }
            if (criteria.getStartTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getStartTime(), Appointment_.startTime));
            }
            if (criteria.getEnterTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEnterTime(), Appointment_.enterTime));
            }
            if (criteria.getLeaveTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLeaveTime(), Appointment_.leaveTime));
            }
            if (criteria.getRegionId() != null) {
                specification = specification.and(buildSpecification(criteria.getRegionId(),
                    root -> root.join(Appointment_.region, JoinType.LEFT).get(Region_.id)));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(buildSpecification(criteria.getUserId(),
                    root -> root.join(Appointment_.user, JoinType.LEFT).get(User_.id)));
            }
        }
        return specification;
    }
}
