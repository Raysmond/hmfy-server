package com.shield.web.rest;

import com.shield.service.GateRecordService;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.service.dto.GateRecordDTO;
import com.shield.service.dto.GateRecordCriteria;
import com.shield.service.GateRecordQueryService;

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
 * REST controller for managing {@link com.shield.domain.GateRecord}.
 */
@RestController
@RequestMapping("/api")
public class GateRecordResource {

    private final Logger log = LoggerFactory.getLogger(GateRecordResource.class);

    private static final String ENTITY_NAME = "gateRecord";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GateRecordService gateRecordService;

    private final GateRecordQueryService gateRecordQueryService;

    public GateRecordResource(GateRecordService gateRecordService, GateRecordQueryService gateRecordQueryService) {
        this.gateRecordService = gateRecordService;
        this.gateRecordQueryService = gateRecordQueryService;
    }

    /**
     * {@code POST  /gate-records} : Create a new gateRecord.
     *
     * @param gateRecordDTO the gateRecordDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new gateRecordDTO, or with status {@code 400 (Bad Request)} if the gateRecord has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/gate-records")
    public ResponseEntity<GateRecordDTO> createGateRecord(@Valid @RequestBody GateRecordDTO gateRecordDTO) throws URISyntaxException {
        log.debug("REST request to save GateRecord : {}", gateRecordDTO);
        if (gateRecordDTO.getId() != null) {
            throw new BadRequestAlertException("A new gateRecord cannot already have an ID", ENTITY_NAME, "idexists");
        }
        GateRecordDTO result = gateRecordService.save(gateRecordDTO);
        return ResponseEntity.created(new URI("/api/gate-records/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /gate-records} : Updates an existing gateRecord.
     *
     * @param gateRecordDTO the gateRecordDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gateRecordDTO,
     * or with status {@code 400 (Bad Request)} if the gateRecordDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the gateRecordDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/gate-records")
    public ResponseEntity<GateRecordDTO> updateGateRecord(@Valid @RequestBody GateRecordDTO gateRecordDTO) throws URISyntaxException {
        log.debug("REST request to update GateRecord : {}", gateRecordDTO);
        if (gateRecordDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        GateRecordDTO result = gateRecordService.save(gateRecordDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, gateRecordDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /gate-records} : get all the gateRecords.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of gateRecords in body.
     */
    @GetMapping("/gate-records")
    public ResponseEntity<List<GateRecordDTO>> getAllGateRecords(GateRecordCriteria criteria, Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get GateRecords by criteria: {}", criteria);
        Page<GateRecordDTO> page = gateRecordQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
    * {@code GET  /gate-records/count} : count all the gateRecords.
    *
    * @param criteria the criteria which the requested entities should match.
    * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
    */
    @GetMapping("/gate-records/count")
    public ResponseEntity<Long> countGateRecords(GateRecordCriteria criteria) {
        log.debug("REST request to count GateRecords by criteria: {}", criteria);
        return ResponseEntity.ok().body(gateRecordQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /gate-records/:id} : get the "id" gateRecord.
     *
     * @param id the id of the gateRecordDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the gateRecordDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/gate-records/{id}")
    public ResponseEntity<GateRecordDTO> getGateRecord(@PathVariable Long id) {
        log.debug("REST request to get GateRecord : {}", id);
        Optional<GateRecordDTO> gateRecordDTO = gateRecordService.findOne(id);
        return ResponseUtil.wrapOrNotFound(gateRecordDTO);
    }

    /**
     * {@code DELETE  /gate-records/:id} : delete the "id" gateRecord.
     *
     * @param id the id of the gateRecordDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/gate-records/{id}")
    public ResponseEntity<Void> deleteGateRecord(@PathVariable Long id) {
        log.debug("REST request to delete GateRecord : {}", id);
        gateRecordService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
