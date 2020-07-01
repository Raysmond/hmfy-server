package com.shield.web.rest;

import com.shield.domain.User;
import com.shield.security.AuthoritiesConstants;
import com.shield.security.SecurityUtils;
import com.shield.service.ShipPlanService;
import com.shield.service.UserService;
import com.shield.service.util.RandomUtil;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.dto.ShipPlanCriteria;
import com.shield.service.ShipPlanQueryService;

import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.ZonedDateTimeFilter;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.shield.domain.ShipPlan}.
 */
@RestController
@RequestMapping("/api")
public class ShipPlanResource {

    private final Logger log = LoggerFactory.getLogger(ShipPlanResource.class);

    private static final String ENTITY_NAME = "shipPlan";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ShipPlanService shipPlanService;

    private final ShipPlanQueryService shipPlanQueryService;

    @Autowired
    @Qualifier("redisLongTemplate")
    private RedisTemplate<String, Long> redisLongTemplate;

    private static final String REDIS_KEY_CUSTOM_APPLY_ID_INC = "auto_inc_apply_id";

    @Autowired
    private UserService userService;

    public ShipPlanResource(ShipPlanService shipPlanService, ShipPlanQueryService shipPlanQueryService) {
        this.shipPlanService = shipPlanService;
        this.shipPlanQueryService = shipPlanQueryService;
    }

    /**
     * {@code POST  /ship-plans} : Create a new shipPlan.
     *
     * @param shipPlanDTO the shipPlanDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new shipPlanDTO, or with status {@code 400 (Bad Request)} if the shipPlan has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/ship-plans")
    public ResponseEntity<ShipPlanDTO> createShipPlan(@Valid @RequestBody ShipPlanDTO shipPlanDTO) throws URISyntaxException {
        log.debug("REST request to save ShipPlan : {}", shipPlanDTO);
        if (shipPlanDTO.getId() != null) {
            throw new BadRequestAlertException("A new shipPlan cannot already have an ID", ENTITY_NAME, "idexists");
        }
        shipPlanDTO.setCreateTime(ZonedDateTime.now());
        shipPlanDTO.setUpdateTime(ZonedDateTime.now());
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime tomorrow = today.plusDays(1L);
        shipPlanDTO.setDeliverTime(shipPlanDTO.getDeliverTime().withZoneSameInstant(ZoneId.systemDefault()));
        ZonedDateTime d = LocalDate.of(shipPlanDTO.getDeliverTime().getYear(), shipPlanDTO.getDeliverTime().getMonth(), shipPlanDTO.getDeliverTime().getDayOfMonth()).atStartOfDay(ZoneId.systemDefault());
        if (!(d.equals(today) || d.equals(tomorrow))) {
            throw new BadRequestAlertException("只能创建今天和明天的计划", ENTITY_NAME, "deliverTimeOnlyTodayAndTomorrow");
        }
        shipPlanDTO.setDeliverTime(d);
        shipPlanDTO.setValid(true);
        if (!redisLongTemplate.hasKey(REDIS_KEY_CUSTOM_APPLY_ID_INC)) {
            redisLongTemplate.opsForValue().increment(REDIS_KEY_CUSTOM_APPLY_ID_INC, 10000L);
        }
        shipPlanDTO.setApplyId(100000000L + redisLongTemplate.opsForValue().increment(REDIS_KEY_CUSTOM_APPLY_ID_INC, 1L));
        // WJRQKA20190608280042
        shipPlanDTO.setApplyNumber("FKSN" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHMMSS")));

        User user = userService.getUserWithAuthorities().get();
        shipPlanDTO.setUserId(user.getId());
        if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.REGION_ADMIN)) {
            shipPlanDTO.setDeliverPosition(user.getRegion().getName());
        } else {
            if (org.apache.commons.lang3.StringUtils.isBlank(shipPlanDTO.getDeliverPosition())) {
                throw new BadRequestAlertException("请选择区域", ENTITY_NAME, "");
            }
        }
        ShipPlanDTO result = shipPlanService.save(shipPlanDTO);
        return ResponseEntity.created(new URI("/api/ship-plans/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /ship-plans} : Updates an existing shipPla
     * @param shipPlanDTO the shipPlanDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shipPlanDTO,iprodo
     * or with status {@code 400 (Bad Request)} if the shipPlanDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the shipPlanDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/ship-plans")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ShipPlanDTO> updateShipPlan(@Valid @RequestBody ShipPlanDTO shipPlanDTO) throws URISyntaxException {
        log.debug("REST request to update ShipPlan : {}", shipPlanDTO);
        if (shipPlanDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (shipPlanDTO.getLeaveTime() != null) {
            throw new BadRequestAlertException("已离场数据无法变更", ENTITY_NAME, "");
        }
        ShipPlanDTO result = shipPlanService.save(shipPlanDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, shipPlanDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /ship-plans} : get all the shipPlans.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of shipPlans in body.
     */
    @GetMapping("/ship-plans")
    public ResponseEntity<List<ShipPlanDTO>> getAllShipPlans(ShipPlanCriteria criteria, Pageable pageable,
                                                             @RequestParam MultiValueMap<String, String> queryParams,
                                                             @RequestParam(required = true) String deliverTimeBegin,
                                                             @RequestParam(required = false) String deliverTimeEnd,
                                                             UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get ShipPlans by criteria: {}", criteria);
        if (!StringUtils.isEmpty(deliverTimeBegin)) {
            LocalDate t = LocalDate.parse(deliverTimeBegin, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            ZonedDateTimeFilter f = new ZonedDateTimeFilter();
//            f.setEquals(t.atStartOfDay(ZoneId.systemDefault()));
            f.setGreaterOrEqualThan(t.atStartOfDay(ZoneId.systemDefault()));
            if (!StringUtils.isEmpty(deliverTimeEnd)) {
                LocalDate end = LocalDate.parse(deliverTimeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                f.setLessOrEqualThan(end.atStartOfDay(ZoneId.systemDefault()));
            }
            criteria.setDeliverTime(f);
        }
        if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.REGION_ADMIN)) {
            StringFilter deliverPosition = new StringFilter();
            deliverPosition.setEquals(userService.getUserWithAuthorities().get().getRegion().getName());
            criteria.setDeliverPosition(deliverPosition);
        }
        Page<ShipPlanDTO> page = shipPlanQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /ship-plans/count} : count all the shipPlans.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/ship-plans/count")
    public ResponseEntity<Long> countShipPlans(ShipPlanCriteria criteria) {
        log.debug("REST request to count ShipPlans by criteria: {}", criteria);
        return ResponseEntity.ok().body(shipPlanQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /ship-plans/:id} : get the "id" shipPlan.
     *
     * @param id the id of the shipPlanDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the shipPlanDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ship-plans/{id}")
    public ResponseEntity<ShipPlanDTO> getShipPlan(@PathVariable Long id) {
        log.debug("REST request to get ShipPlan : {}", id);
        Optional<ShipPlanDTO> shipPlanDTO = shipPlanService.findOne(id);
        return ResponseUtil.wrapOrNotFound(shipPlanDTO);
    }

    /**
     * {@code DELETE  /ship-plans/:id} : delete the "id" shipPlan.
     *
     * @param id the id of the shipPlanDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
//    @DeleteMapping("/ship-plans/{id}")
//    public ResponseEntity<Void> deleteShipPlan(@PathVariable Long id) {
//        log.debug("REST request to delete ShipPlan : {}", id);
//        shipPlanService.delete(id);
//        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
//    }
}
