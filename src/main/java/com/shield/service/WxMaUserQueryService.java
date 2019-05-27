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

import com.shield.domain.WxMaUser;
import com.shield.domain.*; // for static metamodels
import com.shield.repository.WxMaUserRepository;
import com.shield.service.dto.WxMaUserCriteria;
import com.shield.service.dto.WxMaUserDTO;
import com.shield.service.mapper.WxMaUserMapper;

/**
 * Service for executing complex queries for {@link WxMaUser} entities in the database.
 * The main input is a {@link WxMaUserCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link WxMaUserDTO} or a {@link Page} of {@link WxMaUserDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class WxMaUserQueryService extends QueryService<WxMaUser> {

    private final Logger log = LoggerFactory.getLogger(WxMaUserQueryService.class);

    private final WxMaUserRepository wxMaUserRepository;

    private final WxMaUserMapper wxMaUserMapper;

    public WxMaUserQueryService(WxMaUserRepository wxMaUserRepository, WxMaUserMapper wxMaUserMapper) {
        this.wxMaUserRepository = wxMaUserRepository;
        this.wxMaUserMapper = wxMaUserMapper;
    }

    /**
     * Return a {@link List} of {@link WxMaUserDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<WxMaUserDTO> findByCriteria(WxMaUserCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<WxMaUser> specification = createSpecification(criteria);
        return wxMaUserMapper.toDto(wxMaUserRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link WxMaUserDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<WxMaUserDTO> findByCriteria(WxMaUserCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<WxMaUser> specification = createSpecification(criteria);
        return wxMaUserRepository.findAll(specification, page)
            .map(wxMaUserMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(WxMaUserCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<WxMaUser> specification = createSpecification(criteria);
        return wxMaUserRepository.count(specification);
    }

    /**
     * Function to convert WxMaUserCriteria to a {@link Specification}.
     */
    private Specification<WxMaUser> createSpecification(WxMaUserCriteria criteria) {
        Specification<WxMaUser> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), WxMaUser_.id));
            }
            if (criteria.getOpenId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOpenId(), WxMaUser_.openId));
            }
            if (criteria.getNickName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getNickName(), WxMaUser_.nickName));
            }
            if (criteria.getGender() != null) {
                specification = specification.and(buildStringSpecification(criteria.getGender(), WxMaUser_.gender));
            }
            if (criteria.getLanguage() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLanguage(), WxMaUser_.language));
            }
            if (criteria.getCity() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCity(), WxMaUser_.city));
            }
            if (criteria.getProvince() != null) {
                specification = specification.and(buildStringSpecification(criteria.getProvince(), WxMaUser_.province));
            }
            if (criteria.getCountry() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCountry(), WxMaUser_.country));
            }
            if (criteria.getAvatarUrl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAvatarUrl(), WxMaUser_.avatarUrl));
            }
            if (criteria.getUnionId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUnionId(), WxMaUser_.unionId));
            }
            if (criteria.getWatermark() != null) {
                specification = specification.and(buildStringSpecification(criteria.getWatermark(), WxMaUser_.watermark));
            }
            if (criteria.getCreateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreateTime(), WxMaUser_.createTime));
            }
            if (criteria.getUpdateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdateTime(), WxMaUser_.updateTime));
            }
            if (criteria.getPhone() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPhone(), WxMaUser_.phone));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(buildSpecification(criteria.getUserId(),
                    root -> root.join(WxMaUser_.user, JoinType.LEFT).get(User_.id)));
            }
        }
        return specification;
    }
}
