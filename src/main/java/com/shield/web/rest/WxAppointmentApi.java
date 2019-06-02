package com.shield.web.rest;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.shield.config.WxMiniAppConfiguration;
import com.shield.domain.User;
import com.shield.security.AuthoritiesConstants;
import com.shield.security.SecurityUtils;
import com.shield.service.AppointmentQueryService;
import com.shield.service.AppointmentService;
import com.shield.service.RegionService;
import com.shield.service.UserService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.web.rest.vm.AppointmentRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/wx/{appid}/appointments")
public class WxAppointmentApi {

    private final Logger log = LoggerFactory.getLogger(WxAppointmentApi.class);

    private static final String ENTITY_NAME = "appointment";

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentQueryService appointmentQueryService;

    @Autowired
    private UserService userService;

    @Autowired
    private RegionService regionService;

    /**
     * 预约排队接口
     */
    @PostMapping("")
    public ResponseEntity<AppointmentDTO> makeAppointment(
        @PathVariable String appid,
        @Valid @RequestBody AppointmentRequestDTO appointment) throws URISyntaxException {
        log.debug("REST request to make Appointment : {}", appointment);
        final WxMaService wxService = WxMiniAppConfiguration.getMaService(appid);

        if (!SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.APPOINTMENT)) {
            throw new BadRequestAlertException("不具备预约员权限", ENTITY_NAME, "");
        }
        User user = userService.getUserWithAuthorities().get();
        if (user.getRegion() == null || !user.getRegion().getId().equals(appointment.getRegionId())) {
            throw new BadRequestAlertException("不能预约未绑定的区域", ENTITY_NAME, "regionId");
        }
        if (!regionService.isRegionOpen(appointment.getRegionId())) {
            throw new BadRequestAlertException(String.format("区域[%d]未开放预约服务", appointment.getRegionId()), "appointment", "regionId");
        }

        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setLicensePlateNumber(appointment.getLicensePlateNumber());
        appointmentDTO.setDriver(appointment.getDriver());
        appointmentDTO.setRegionId(appointment.getRegionId());
        AppointmentDTO result = appointmentService.makeAppointment(appointment.getRegionId(), appointmentDTO);
        return ResponseEntity.created(new URI(String.format("/api/wx/%s/appointments/%d", appid, result.getId()))).body(appointmentDTO);
    }

}
