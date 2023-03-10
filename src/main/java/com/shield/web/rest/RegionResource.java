package com.shield.web.rest;

import com.google.common.collect.Lists;
import com.shield.domain.User;
import com.shield.security.AuthoritiesConstants;
import com.shield.security.SecurityUtils;
import com.shield.service.RegionService;
import com.shield.service.UserService;
import com.shield.service.mapper.RegionMapper;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.service.dto.RegionDTO;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PageUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.shield.domain.Region}.
 */
@RestController
@RequestMapping("/api")
public class RegionResource {

    private final Logger log = LoggerFactory.getLogger(RegionResource.class);

    private static final String ENTITY_NAME = "region";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RegionService regionService;

    public RegionResource(RegionService regionService) {
        this.regionService = regionService;
    }

    /**
     * {@code POST  /regions} : Create a new region.
     *
     * @param regionDTO the regionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new regionDTO, or with status {@code 400 (Bad Request)} if the region has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/regions")
    public ResponseEntity<RegionDTO> createRegion(@Valid @RequestBody RegionDTO regionDTO) throws URISyntaxException {
        log.debug("REST request to save Region : {}", regionDTO);
        if (regionDTO.getId() != null) {
            throw new BadRequestAlertException("A new region cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RegionDTO result = regionService.save(regionDTO);
        return ResponseEntity.created(new URI("/api/regions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /regions} : Updates an existing region.
     *
     * @param regionDTO the regionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated regionDTO,
     * or with status {@code 400 (Bad Request)} if the regionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the regionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/regions")
    public ResponseEntity<RegionDTO> updateRegion(@Valid @RequestBody RegionDTO regionDTO) throws URISyntaxException {
        log.debug("REST request to update Region : {}", regionDTO);
        if (regionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        regionDTO.setUpdateTime(ZonedDateTime.now());
        RegionDTO result = regionService.save(regionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, regionDTO.getId().toString()))
            .body(result);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private RegionMapper regionMapper;

    /**
     * {@code GET  /regions} : get all the regions.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of regions in body.
     */
    @GetMapping("/regions")
    public ResponseEntity<List<RegionDTO>> getAllRegions(Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get a page of Regions");
        Page<RegionDTO> page = null;
        if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.REGION_ADMIN)) {
            User user = userService.getUserWithAuthorities().get();
            if (user.getRegion() == null) {
                page = Page.empty();
            } else {
                page = PageUtil.createPageFromList(Lists.newArrayList(regionService.findOne(user.getRegion().getId()).get()), pageable);
            }
        } else {
            page = regionService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /regions/:id} : get the "id" region.
     *
     * @param id the id of the regionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the regionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/regions/{id}")
    public ResponseEntity<RegionDTO> getRegion(@PathVariable Long id) {
        log.debug("REST request to get Region : {}", id);
        Optional<RegionDTO> regionDTO = regionService.findOne(id);
        if (regionDTO.isPresent() && SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.REGION_ADMIN)) {
            User user = userService.getUserWithAuthorities().get();
            if (user.getRegion() == null || !user.getRegion().getName().equals(regionDTO.get().getName())) {
                 regionDTO = Optional.empty();
            }
        }
        return ResponseUtil.wrapOrNotFound(regionDTO);
    }

    /**
     * {@code DELETE  /regions/:id} : delete the "id" region.
     *
     * @param id the id of the regionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
//    @DeleteMapping("/regions/{id}")
//    public ResponseEntity<Void> deleteRegion(@PathVariable Long id) {
//        log.debug("REST request to delete Region : {}", id);
//        regionService.delete(id);
//        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
//    }
}
