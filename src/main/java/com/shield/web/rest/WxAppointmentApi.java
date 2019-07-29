package com.shield.web.rest;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.google.common.collect.Lists;
import com.shield.config.WxMiniAppConfiguration;
import com.shield.domain.User;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.security.AuthoritiesConstants;
import com.shield.security.SecurityUtils;
import com.shield.service.AppointmentService;
import com.shield.service.RegionService;
import com.shield.service.ShipPlanService;
import com.shield.service.UserService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.PlanDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.web.rest.errors.BadRequestAlertException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/wx/{appid}")
public class WxAppointmentApi {

    private final Logger log = LoggerFactory.getLogger(WxAppointmentApi.class);

    private static final String ENTITY_NAME = "appointment";

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private ShipPlanService shipPlanService;

    /**
     * 预约排队接口
     */
    @PostMapping("/ship_plans/{id}/make_appointment")
    public ResponseEntity<PlanDTO> makeAppointment(
        @PathVariable String appid,
        @PathVariable Long id) {
        log.debug("REST request to make appointment with ship plan id : {}", id);
        final WxMaService wxService = WxMiniAppConfiguration.getMaService(appid);

        User user = userService.getUserWithAuthorities().get();
        if (!SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.APPOINTMENT) || StringUtils.isBlank(user.getTruckNumber())) {
            throw new BadRequestAlertException("不具备预约员权限", ENTITY_NAME, "");
        }

        Pageable page = PageRequest.of(0, 1, Sort.Direction.DESC, "deliverTime");
        Page<PlanDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), id);
        if (result.getContent().isEmpty()) {
            throw new BadRequestAlertException("未找到发运计划", ENTITY_NAME, "");
        }

        PlanDTO plan = result.getContent().get(0);
        RegionDTO region = regionService.findByName(plan.getPlan().getDeliverPosition());

        if (region == null || !regionService.isRegionOpen(region.getId())) {
            throw new BadRequestAlertException("该区域未开放预约服务", "appointment", "regionId");
        }

        if (plan.getAppointment() != null && plan.getAppointment().isValid()) {
            throw new BadRequestAlertException("该计划已有预约号，不能重复取号", ENTITY_NAME, "");
        }

        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setLicensePlateNumber(user.getTruckNumber());
        appointmentDTO.setDriver(user.getFirstName());
        appointmentDTO.setRegionId(region.getId());
        appointmentDTO.setApplyId(plan.getPlan().getApplyId());
        appointmentDTO.setVip(false);
        AppointmentDTO appointment = appointmentService.makeAppointment(region.getId(), appointmentDTO);

        plan.setAppointment(appointment);
        if (appointment.getStatus().equals(AppointmentStatus.START)) {
            plan.setStatus("预约成功");
            plan.setMaxAllowInTime(appointment.getStartTime().plusSeconds(region.getValidTime()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        } else {
            plan.setStatus("排队中");
        }

        return ResponseEntity.ok(plan);
    }

    @PostMapping("/ship_plans/{id}/cancel_appointment")
    public ResponseEntity<PlanDTO> cancelAppointment(@PathVariable String appid, @PathVariable Long id) {
        log.debug("REST request to cancel appointment with ship plan id : {}", id);
        final WxMaService wxService = WxMiniAppConfiguration.getMaService(appid);

        User user = userService.getUserWithAuthorities().get();
        if (!SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.APPOINTMENT) || StringUtils.isBlank(user.getTruckNumber())) {
            throw new BadRequestAlertException("不具备预约员权限", ENTITY_NAME, "");
        }

        Pageable page = PageRequest.of(0, 1, Sort.Direction.DESC, "deliverTime");
        Page<PlanDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), id);
        if (result.getContent().isEmpty()) {
            throw new BadRequestAlertException("未找到发运计划", ENTITY_NAME, "");
        }

        PlanDTO plan = result.getContent().get(0);
        RegionDTO region = regionService.findByName(plan.getPlan().getDeliverPosition());

        if (StringUtils.isBlank(plan.getStatus()) || (!plan.getStatus().equals("预约成功") && !plan.getStatus().equals("排队中"))) {
            throw new BadRequestAlertException("当前计划无法取消", ENTITY_NAME, "");
        } else {
            AppointmentDTO appointmentDTO = appointmentService.cancelAppointment(plan.getAppointment().getId());
            log.info("Appointment [{}], truckNumber: {} is canceled", plan.getAppointment().getId(), plan.getPlan().getTruckNumber());

            plan.setAppointment(null);
            plan.setStatus("可预约");
            plan.setMaxAllowInTime(null);

            return ResponseEntity.ok(plan);
        }
    }


    @GetMapping("/ship_plans/available")
    public ResponseEntity<List<PlanDTO>> getUserShipPlans(@PathVariable String appid, @RequestParam String regionName) {
        RegionDTO regionDTO = regionService.findByName(regionName);
        if (regionDTO == null) {
            return ResponseEntity.notFound().build();
        }
        User user = userService.getUserWithAuthorities().get();
        if (StringUtils.isNotBlank(user.getTruckNumber()) && SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.APPOINTMENT)) {
            List<ShipPlanDTO> shipPlanDTOS = shipPlanService.getAvailableByTruckNumber(regionDTO.getId(), user.getTruckNumber());
            if (CollectionUtils.isEmpty(shipPlanDTOS)) {
                return ResponseEntity.ok(Lists.newArrayList());
            }
            Pageable page = PageRequest.of(0, 1, Sort.Direction.DESC, "deliverTime");
            Page<PlanDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), shipPlanDTOS.get(0).getId());
            return ResponseEntity.ok(result.getContent());
        }
        return ResponseEntity.ok(Lists.newArrayList());
    }

    @GetMapping("/ship_plans/latest")
    public ResponseEntity<Page<PlanDTO>> getUserLatestShipPlans(@PathVariable String appid, Pageable pageable) {
        User user = userService.getUserWithAuthorities().get();
        if (StringUtils.isNotBlank(user.getTruckNumber()) && SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.APPOINTMENT)) {
            Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC, "deliverTime");
            Page<PlanDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), null);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @GetMapping("/ship_plans/{id}")
    public ResponseEntity<PlanDTO> getPlan(@PathVariable String appid, @PathVariable Long id) {
        User user = userService.getUserWithAuthorities().get();
        if (StringUtils.isNotBlank(user.getTruckNumber()) && SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.APPOINTMENT)) {
            Pageable page = PageRequest.of(0, 1, Sort.Direction.DESC, "deliverTime");
            Page<PlanDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), id);
            if (result.getContent().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result.getContent().get(0));
        }
        return ResponseEntity.badRequest().body(null);
    }

    @GetMapping("/region/{regionName}")
    public ResponseEntity<RegionDTO> getRegion(@PathVariable String appid, @PathVariable String regionName) {
        RegionDTO regionDTO = regionService.findByName(regionName);
        if (regionDTO == null) {
            return ResponseEntity.notFound().build();
        }
        log.info("request to get region {}", regionName);
        regionDTO.setOpen(Boolean.FALSE);
        regionDTO.setRemainQuota(0);
        if (regionService.isRegionOpen(regionDTO.getId())) {
            regionDTO.setOpen(Boolean.TRUE);
            Integer appointmentsCount = appointmentService.countAppointmentOfRegionIdAndCreateTime(
                regionDTO.getId(),
                ZonedDateTime.now().minusHours(12)).intValue();
            regionDTO.setRemainQuota(regionDTO.getQuota() - appointmentsCount);

            if (regionDTO.getQueueQuota() > 0 && (regionDTO.getQuota() - appointmentsCount) <= 0) {
                Integer waitingCount = appointmentService.countAllWaitByRegionId(regionDTO.getId()).intValue();
                regionDTO.setQueueQuota(regionDTO.getQueueQuota() - waitingCount);
            }
        }
        return ResponseEntity.ok(regionDTO);
    }

}
