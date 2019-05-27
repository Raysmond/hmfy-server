package com.shield.web.rest;

import com.shield.service.WxMaUserService;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.service.dto.WxMaUserDTO;
import com.shield.service.dto.WxMaUserCriteria;
import com.shield.service.WxMaUserQueryService;

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
 * REST controller for managing {@link com.shield.domain.WxMaUser}.
 */
@RestController
@RequestMapping("/api")
public class WxMaUserResource {

    private final Logger log = LoggerFactory.getLogger(WxMaUserResource.class);

    private static final String ENTITY_NAME = "wxMaUser";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final WxMaUserService wxMaUserService;

    private final WxMaUserQueryService wxMaUserQueryService;

    public WxMaUserResource(WxMaUserService wxMaUserService, WxMaUserQueryService wxMaUserQueryService) {
        this.wxMaUserService = wxMaUserService;
        this.wxMaUserQueryService = wxMaUserQueryService;
    }

    /**
     * {@code POST  /wx-ma-users} : Create a new wxMaUser.
     *
     * @param wxMaUserDTO the wxMaUserDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new wxMaUserDTO, or with status {@code 400 (Bad Request)} if the wxMaUser has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/wx-ma-users")
    public ResponseEntity<WxMaUserDTO> createWxMaUser(@Valid @RequestBody WxMaUserDTO wxMaUserDTO) throws URISyntaxException {
        log.debug("REST request to save WxMaUser : {}", wxMaUserDTO);
        if (wxMaUserDTO.getId() != null) {
            throw new BadRequestAlertException("A new wxMaUser cannot already have an ID", ENTITY_NAME, "idexists");
        }
        WxMaUserDTO result = wxMaUserService.save(wxMaUserDTO);
        return ResponseEntity.created(new URI("/api/wx-ma-users/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /wx-ma-users} : Updates an existing wxMaUser.
     *
     * @param wxMaUserDTO the wxMaUserDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wxMaUserDTO,
     * or with status {@code 400 (Bad Request)} if the wxMaUserDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the wxMaUserDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/wx-ma-users")
    public ResponseEntity<WxMaUserDTO> updateWxMaUser(@Valid @RequestBody WxMaUserDTO wxMaUserDTO) throws URISyntaxException {
        log.debug("REST request to update WxMaUser : {}", wxMaUserDTO);
        if (wxMaUserDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        WxMaUserDTO result = wxMaUserService.save(wxMaUserDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, wxMaUserDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /wx-ma-users} : get all the wxMaUsers.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of wxMaUsers in body.
     */
    @GetMapping("/wx-ma-users")
    public ResponseEntity<List<WxMaUserDTO>> getAllWxMaUsers(WxMaUserCriteria criteria, Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get WxMaUsers by criteria: {}", criteria);
        Page<WxMaUserDTO> page = wxMaUserQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
    * {@code GET  /wx-ma-users/count} : count all the wxMaUsers.
    *
    * @param criteria the criteria which the requested entities should match.
    * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
    */
    @GetMapping("/wx-ma-users/count")
    public ResponseEntity<Long> countWxMaUsers(WxMaUserCriteria criteria) {
        log.debug("REST request to count WxMaUsers by criteria: {}", criteria);
        return ResponseEntity.ok().body(wxMaUserQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /wx-ma-users/:id} : get the "id" wxMaUser.
     *
     * @param id the id of the wxMaUserDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the wxMaUserDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/wx-ma-users/{id}")
    public ResponseEntity<WxMaUserDTO> getWxMaUser(@PathVariable Long id) {
        log.debug("REST request to get WxMaUser : {}", id);
        Optional<WxMaUserDTO> wxMaUserDTO = wxMaUserService.findOne(id);
        return ResponseUtil.wrapOrNotFound(wxMaUserDTO);
    }

    /**
     * {@code DELETE  /wx-ma-users/:id} : delete the "id" wxMaUser.
     *
     * @param id the id of the wxMaUserDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/wx-ma-users/{id}")
    public ResponseEntity<Void> deleteWxMaUser(@PathVariable Long id) {
        log.debug("REST request to delete WxMaUser : {}", id);
        wxMaUserService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
