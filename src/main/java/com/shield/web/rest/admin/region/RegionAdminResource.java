package com.shield.web.rest.admin.region;

import com.google.common.collect.Lists;
import com.shield.domain.Region;
import com.shield.service.RegionService;
import com.shield.service.UserService;
import com.shield.service.dto.RegionDTO;
import com.shield.service.mapper.RegionMapper;
import com.shield.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PageUtil;
import io.github.jhipster.web.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing {@link com.shield.domain.Region}.
 */
@RestController
@RequestMapping("/region-admin/api")
public class RegionAdminResource {

    private final Logger log = LoggerFactory.getLogger(RegionAdminResource.class);

    private static final String ENTITY_NAME = "region";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RegionService regionService;

    @Autowired
    private UserService userService;

    @Autowired
    private RegionMapper regionMapper;

    public RegionAdminResource(RegionService regionService) {
        this.regionService = regionService;
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

        Region region = userService.getUserWithAuthorities().get().getRegion();
        if (region == null || !region.getId().equals(regionDTO.getId())) {
            return ResponseEntity.badRequest().body(null);
        }

        RegionDTO result = regionService.save(regionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, regionDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /regions} : get all the regions.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of regions in body.
     */
    @GetMapping({"/regions"})
    public ResponseEntity<List<RegionDTO>> getAllRegions(Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get a page of Regions");
        Region region = userService.getUserWithAuthorities().get().getRegion();
        Page<RegionDTO> page = Page.empty(pageable);
        if (region != null) {
            page = PageUtil.createPageFromList(Lists.newArrayList(regionMapper.toDto(region)), pageable);
            Map<Long, Long> countDrivers = regionService.countDriversByRegionId();
            page.map(it -> {
                if (countDrivers.containsKey(it.getId())) {
                    it.setDrivers(countDrivers.get(it.getId()).intValue());
                }
                return it;
            });
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
        Region region = userService.getUserWithAuthorities().get().getRegion();
        if (region == null || !region.getId().equals(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(regionMapper.toDto(region));
    }

}
