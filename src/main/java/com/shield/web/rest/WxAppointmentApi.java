package com.shield.web.rest;


import cn.binarywang.wx.miniapp.api.WxMaService;
import com.shield.config.WxMiniAppConfiguration;
import com.shield.service.AppointmentQueryService;
import com.shield.service.AppointmentService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.web.rest.vm.AppointmentRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * REST controller for managing {@link com.shield.domain.Appointment}.
 */
@RestController
@RequestMapping("/api/wx/{appid}/appointments")
public class WxAppointmentApi {

    private final Logger log = LoggerFactory.getLogger(AppointmentResource.class);

    private static final String ENTITY_NAME = "appointment";

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentQueryService appointmentQueryService;

    /**
     * {@code POST  /appointments} : Create a new appointment.
     *
     * @param appointment the appointmentDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new appointmentDTO, or with status {@code 400 (Bad Request)} if the appointment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AppointmentDTO> createAppointment(
        @PathVariable String appid,
        @Valid @RequestBody AppointmentRequestDTO appointment) throws URISyntaxException {
        final WxMaService wxService = WxMiniAppConfiguration.getMaService(appid);
        log.debug("REST request to make Appointment : {}", appointment);
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setLicensePlateNumber(appointment.getLicensePlateNumber());
        appointmentDTO.setDriver(appointment.getDriver());
        appointmentDTO.setRegionId(appointment.getRegionId());
        AppointmentDTO result = appointmentService.makeAppointment(appointment.getRegionId(), appointmentDTO);
        return ResponseEntity.created(new URI("/api/wx/appointments/" + result.getId())).body(result);
    }

}
