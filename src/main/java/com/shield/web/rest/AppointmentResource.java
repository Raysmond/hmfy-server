package com.shield.web.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.shield.domain.Appointment;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.service.AppointmentService;
import com.shield.service.UserService;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.AppointmentCriteria;
import com.shield.service.AppointmentQueryService;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
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

import static com.shield.domain.enumeration.AppointmentStatus.*;

/**
 * REST controller for managing {@link com.shield.domain.Appointment}.
 */
@RestController
@RequestMapping("/api")
public class AppointmentResource {

    private final Logger log = LoggerFactory.getLogger(AppointmentResource.class);

    private static final String ENTITY_NAME = "appointment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AppointmentService appointmentService;

    private final AppointmentQueryService appointmentQueryService;

    private final UserService userService;

    @Autowired
    public AppointmentResource(AppointmentService appointmentService, AppointmentQueryService appointmentQueryService, UserService userService) {
        this.appointmentService = appointmentService;
        this.appointmentQueryService = appointmentQueryService;
        this.userService = userService;
    }

    /**
     * {@code POST  /appointments} : Create a new appointment.
     *
     * @param appointmentDTO the appointmentDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new appointmentDTO, or with status {@code 400 (Bad Request)} if the appointment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/appointments")
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) throws URISyntaxException {
        log.debug("REST request to save Appointment : {}", appointmentDTO);
        if (appointmentDTO.getId() != null) {
            throw new BadRequestAlertException("A new appointment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (appointmentDTO.getRegionId() == null) {
            throw new BadRequestAlertException("???????????????", ENTITY_NAME, "regionRequired");
        }
        List<AppointmentDTO> latestAppointments = appointmentService.findLatestByTruckNumber(appointmentDTO.getLicensePlateNumber(), ZonedDateTime.now().minusDays(1));
        if (latestAppointments.size() > 0 && Sets.newHashSet(START, START_CHECK, WAIT).contains(latestAppointments.get(0).getStatus())) {
            throw new BadRequestAlertException("??????????????????????????????????????????", "appointment", "RAW_TITLE");
        }
        appointmentDTO.setUserId(userService.getUserWithAuthorities().get().getId());
        appointmentDTO.setVip(true);
        appointmentDTO.setStatus(AppointmentStatus.CREATE);
        AppointmentDTO result = appointmentService.makeAppointment(appointmentDTO.getRegionId(), appointmentDTO);
        return ResponseEntity.created(new URI("/api/appointments/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /appointments} : Updates an existing appointment.
     *
     * @param appointmentDTO the appointmentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appointmentDTO,
     * or with status {@code 400 (Bad Request)} if the appointmentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the appointmentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/appointments")
    public ResponseEntity<AppointmentDTO> updateAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) throws URISyntaxException {
        log.debug("REST request to update Appointment : {}", appointmentDTO);
        if (appointmentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AppointmentDTO old = appointmentService.findOne(appointmentDTO.getId()).get();
        // ??????????????????????????????
        old.setValid(appointmentDTO.isValid());
        old.setVip(appointmentDTO.isVip());
        old.setStatus(appointmentDTO.getStatus());

        if (old.getStatus() == AppointmentStatus.ENTER && old.getEnterTime() == null) {
            old.setEnterTime(ZonedDateTime.now());
        }
        if (old.getStatus() == AppointmentStatus.LEAVE && old.getLeaveTime() == null) {
            old.setLeaveTime(ZonedDateTime.now());
        }
        AppointmentDTO result = appointmentService.save(old);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, old.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /appointments} : get all the appointments.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appointments in body.
     */
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments(
        AppointmentCriteria criteria,
        Pageable pageable,
        @RequestParam MultiValueMap<String, String> queryParams,
        UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get Appointments by criteria: {}", criteria);
        Page<AppointmentDTO> page = appointmentQueryService.findByCriteria(criteria, pageable);
        List<AppointmentDTO> result = page.getContent();
        if (!CollectionUtils.isEmpty(result)) {
            appointmentService.setApplyNumber(result);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(result);
    }

    /**
     * {@code GET  /appointments/count} : count all the appointments.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/appointments/count")
    public ResponseEntity<Long> countAppointments(AppointmentCriteria criteria) {
        log.debug("REST request to count Appointments by criteria: {}", criteria);
        return ResponseEntity.ok().body(appointmentQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /appointments/:id} : get the "id" appointment.
     *
     * @param id the id of the appointmentDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the appointmentDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentDTO> getAppointment(@PathVariable Long id) {
        log.debug("REST request to get Appointment : {}", id);
        Optional<AppointmentDTO> appointmentDTO = appointmentService.findOne(id);
        if (appointmentDTO.isPresent()) {
            List<AppointmentDTO> result = Lists.newArrayList(appointmentDTO.get());
            appointmentService.setApplyNumber(result);
        }
        return ResponseUtil.wrapOrNotFound(appointmentDTO);
    }

    /**
     * {@code DELETE  /appointments/:id} : delete the "id" appointment.
     *
     * @param id the id of the appointmentDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        log.debug("REST request to delete Appointment : {}", id);
        appointmentService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
