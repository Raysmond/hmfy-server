package com.shield.web.rest;

import com.shield.service.ShipPlanService;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.dto.ShipPlanCriteria;
import com.shield.service.ShipPlanQueryService;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

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
        ShipPlanDTO result = shipPlanService.save(shipPlanDTO);
        return ResponseEntity.created(new URI("/api/ship-plans/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /ship-plans} : Updates an existing shipPlan.
     *
     * @param shipPlanDTO the shipPlanDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shipPlanDTO,
     * or with status {@code 400 (Bad Request)} if the shipPlanDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the shipPlanDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/ship-plans")
    public ResponseEntity<ShipPlanDTO> updateShipPlan(@Valid @RequestBody ShipPlanDTO shipPlanDTO) throws URISyntaxException {
        log.debug("REST request to update ShipPlan : {}", shipPlanDTO);
        if (shipPlanDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
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
    public ResponseEntity<List<ShipPlanDTO>> getAllShipPlans(ShipPlanCriteria criteria, Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get ShipPlans by criteria: {}", criteria);
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
    @DeleteMapping("/ship-plans/{id}")
    public ResponseEntity<Void> deleteShipPlan(@PathVariable Long id) {
        log.debug("REST request to delete ShipPlan : {}", id);
        shipPlanService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
