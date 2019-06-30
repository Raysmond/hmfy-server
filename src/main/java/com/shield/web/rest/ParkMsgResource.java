package com.shield.web.rest;

import com.shield.service.ParkMsgService;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.service.dto.ParkMsgDTO;

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
 * REST controller for managing {@link com.shield.domain.ParkMsg}.
 */
@RestController
@RequestMapping("/api")
public class ParkMsgResource {

    private final Logger log = LoggerFactory.getLogger(ParkMsgResource.class);

    private static final String ENTITY_NAME = "parkMsg";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ParkMsgService parkMsgService;

    public ParkMsgResource(ParkMsgService parkMsgService) {
        this.parkMsgService = parkMsgService;
    }

    /**
     * {@code POST  /park-msgs} : Create a new parkMsg.
     *
     * @param parkMsgDTO the parkMsgDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new parkMsgDTO, or with status {@code 400 (Bad Request)} if the parkMsg has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/park-msgs")
    public ResponseEntity<ParkMsgDTO> createParkMsg(@Valid @RequestBody ParkMsgDTO parkMsgDTO) throws URISyntaxException {
        log.debug("REST request to save ParkMsg : {}", parkMsgDTO);
        if (parkMsgDTO.getId() != null) {
            throw new BadRequestAlertException("A new parkMsg cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ParkMsgDTO result = parkMsgService.save(parkMsgDTO);
        return ResponseEntity.created(new URI("/api/park-msgs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /park-msgs} : Updates an existing parkMsg.
     *
     * @param parkMsgDTO the parkMsgDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated parkMsgDTO,
     * or with status {@code 400 (Bad Request)} if the parkMsgDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the parkMsgDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/park-msgs")
    public ResponseEntity<ParkMsgDTO> updateParkMsg(@Valid @RequestBody ParkMsgDTO parkMsgDTO) throws URISyntaxException {
        log.debug("REST request to update ParkMsg : {}", parkMsgDTO);
        if (parkMsgDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ParkMsgDTO result = parkMsgService.save(parkMsgDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, parkMsgDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /park-msgs} : get all the parkMsgs.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of parkMsgs in body.
     */
    @GetMapping("/park-msgs")
    public ResponseEntity<List<ParkMsgDTO>> getAllParkMsgs(Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get a page of ParkMsgs");
        Page<ParkMsgDTO> page = parkMsgService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /park-msgs/:id} : get the "id" parkMsg.
     *
     * @param id the id of the parkMsgDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the parkMsgDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/park-msgs/{id}")
    public ResponseEntity<ParkMsgDTO> getParkMsg(@PathVariable Long id) {
        log.debug("REST request to get ParkMsg : {}", id);
        Optional<ParkMsgDTO> parkMsgDTO = parkMsgService.findOne(id);
        return ResponseUtil.wrapOrNotFound(parkMsgDTO);
    }

    /**
     * {@code DELETE  /park-msgs/:id} : delete the "id" parkMsg.
     *
     * @param id the id of the parkMsgDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/park-msgs/{id}")
    public ResponseEntity<Void> deleteParkMsg(@PathVariable Long id) {
        log.debug("REST request to delete ParkMsg : {}", id);
        parkMsgService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
