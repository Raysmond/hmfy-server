package com.shield.web.rest.api;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shield.config.WxMiniAppConfiguration;
import com.shield.domain.User;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.domain.enumeration.ParkingConnectMethod;
import com.shield.security.AuthoritiesConstants;
import com.shield.security.SecurityUtils;
import com.shield.service.*;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.PlanAppointmentDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.utils.QrCodeUtils;
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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.shield.config.Constants.REGION_ID_HUACHAN;

/**
 * 取号 API
 */
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

    @Autowired
    private PenaltyService penaltyService;

    /**
     * 预约排队接口
     */
    @PostMapping("/ship_plans/{id}/make_appointment")
    public synchronized ResponseEntity<PlanAppointmentDTO> makeAppointment(@PathVariable String appid, @PathVariable Long id) {
        log.debug("REST request to make appointment with ship plan id : {}", id);
        User user = userService.getUserWithAuthorities().get();
        if (!SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.APPOINTMENT) || StringUtils.isBlank(user.getTruckNumber())) {
            throw new BadRequestAlertException("不具备预约员权限", ENTITY_NAME, "");
        }

        if (penaltyService.isUserInCancelPenalty(user.getId())) {
            throw new BadRequestAlertException("取消预约后，30分钟之内无法预约！", ENTITY_NAME, "");
        }

        if (penaltyService.isUserInCancelWaitPenalty(user.getId())) {
            throw new BadRequestAlertException("取消排队后，30分钟之内无法预约！", ENTITY_NAME, "");
        }

        if (penaltyService.isUserInExpirePenalty(user.getId())) {
            throw new BadRequestAlertException("预约过期后，一小时之内无法预约！", ENTITY_NAME, "");
        }

        Pageable page = PageRequest.of(0, 1, Sort.Direction.DESC, "deliverTime");
        Page<PlanAppointmentDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), id);
        if (result.getContent().isEmpty()) {
            throw new BadRequestAlertException("未找到发运计划", ENTITY_NAME, "");
        }

        PlanAppointmentDTO plan = result.getContent().get(0);
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
        appointmentDTO.setVip(plan.getPlan().isVip());
        appointmentDTO.setUserId(user.getId());

        AppointmentDTO appointment = null;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        boolean isTomorrowPlan = plan.getPlan().getDeliverTime().toLocalDate().equals(tomorrow);
        if (isTomorrowPlan) {
            if (region.getId().equals(REGION_ID_HUACHAN) && ZonedDateTime.now().getHour() >= 22) {
                // 只有化产22：00开始，可以排第二天的计划
                appointment = appointmentService.makeAppointmentForTomorrow(region, plan.getPlan(), appointmentDTO);
            } else {
                throw new BadRequestAlertException("不能预约明天的计划", ENTITY_NAME, "");
            }
        } else {
            appointment = appointmentService.makeAppointment(region.getId(), appointmentDTO);
        }

        plan.setAppointment(appointment);
        if (appointment.getStatus().equals(AppointmentStatus.START)) {
            plan.setStatus("预约成功");
            plan.setMaxAllowInTime(appointment.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                + " - " + appointment.getStartTime().plusHours(region.getValidTime()).format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            plan.setStatus("排队中");
        }

        return ResponseEntity.ok(plan);
    }

    @PostMapping("/ship_plans/{id}/cancel_appointment")
    public ResponseEntity<PlanAppointmentDTO> cancelAppointment(@PathVariable String appid, @PathVariable Long id) {
        log.debug("REST request to cancel appointment with ship plan id : {}", id);
        final WxMaService wxService = WxMiniAppConfiguration.getMaService(appid);

        User user = userService.getUserWithAuthorities().get();
        if (!SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.APPOINTMENT) || StringUtils.isBlank(user.getTruckNumber())) {
            throw new BadRequestAlertException("不具备预约员权限", ENTITY_NAME, "");
        }

        Pageable page = PageRequest.of(0, 1, Sort.Direction.DESC, "deliverTime");
        Page<PlanAppointmentDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), id);
        if (result.getContent().isEmpty()) {
            throw new BadRequestAlertException("未找到发运计划", ENTITY_NAME, "");
        }

        PlanAppointmentDTO plan = result.getContent().get(0);
        RegionDTO region = regionService.findByName(plan.getPlan().getDeliverPosition());

        if (ParkingConnectMethod.HUA_CHAN_API.equals(region.getParkingConnectMethod())
            && !plan.getAppointment().getStatus().equals(AppointmentStatus.WAIT)) {
            throw new BadRequestAlertException(String.format("区域%s不支持取消预约", region.getName()), ENTITY_NAME, "");
        }

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
    public ResponseEntity<List<PlanAppointmentDTO>> getUserShipPlans(@PathVariable String appid, @RequestParam String regionName, @RequestParam(required = false) Long regionId) {
        RegionDTO regionDTO = regionId != null ? regionService.findOne(regionId).orElse(null) : regionService.findByName(regionName);
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
<<<<<<< HEAD
            Page<PlanAppointmentDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), shipPlanDTOS.get(0).getId());
=======
            Page<PlanDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), shipPlanDTOS.get(0).getId());
>>>>>>> origin/master
            result.getContent()
                .forEach(planDTO -> {
                    if (planDTO.getPlan().getAppointmentNumber() != null) {
                        planDTO.getPlan().genereteUniqueQrcodeNumber();
                        planDTO.getPlan().setQrcodeImage(QrCodeUtils.generateQrCodeImage(planDTO.getPlan().getUniqueQrcodeNumber()));
                    }

                });
            return ResponseEntity.ok(result.getContent());
        }
        return ResponseEntity.ok(Lists.newArrayList());
    }

    @GetMapping("/ship_plans/latest")
    public ResponseEntity<Page<PlanAppointmentDTO>> getUserLatestShipPlans(@PathVariable String appid, Pageable pageable) {
        User user = userService.getUserWithAuthorities().get();
        if (StringUtils.isNotBlank(user.getTruckNumber()) && SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.APPOINTMENT)) {
            Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC, "deliverTime");
            Page<PlanAppointmentDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), null);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @GetMapping("/ship_plans/{id}")
    public ResponseEntity<PlanAppointmentDTO> getPlan(@PathVariable String appid, @PathVariable Long id) {
        User user = userService.getUserWithAuthorities().get();
        if (StringUtils.isNotBlank(user.getTruckNumber()) && SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.APPOINTMENT)) {
            Pageable page = PageRequest.of(0, 1, Sort.Direction.DESC, "deliverTime");
            Page<PlanAppointmentDTO> result = shipPlanService.getAllByTruckNumber(page, user.getTruckNumber(), id);
            if (result.getContent().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result.getContent().get(0));
        }
        return ResponseEntity.badRequest().body(null);
    }

    private Map<String, Map<String, RegionDTO>> regionStatCache = Maps.newHashMap();
    private Map<String, Map<String, ZonedDateTime>> regionStatCacheTime = Maps.newHashMap();

    /**
     * 统计区域取号额度
     */
    @GetMapping("/region/{regionName}")
    public ResponseEntity<RegionDTO> getRegion(@PathVariable String appid, @PathVariable String regionName) {
        String cacheUser = SecurityUtils.isAuthenticated() ? SecurityUtils.getCurrentUserLogin().get() : "__DEFAULT__";
        if (regionStatCache.containsKey(cacheUser)
            && regionStatCache.get(cacheUser).containsKey(regionName)
            && ZonedDateTime.now().toEpochSecond() - regionStatCacheTime.get(cacheUser).get(regionName).toEpochSecond() < 10) {
            // 10s 内不重复计算
            return ResponseEntity.ok(regionStatCache.get(cacheUser).get(regionName));
        }
        RegionDTO region = regionService.findByName(regionName);
        if (region == null) {
            return ResponseEntity.notFound().build();
        }
        log.info("Get region stat {}", regionName);
        if (regionService.isRegionOpen(region.getId())) {
            region.setOpen(Boolean.TRUE);
            region.setNextQuotaNumber(appointmentService.getNextAppointmentNumber(region.getId()));
            appointmentService.countRemainQuota(region, false);
            if (region.getRemainQuota() == 0) {
                try {
                    appointmentService.calcNextQuotaWaitingTime(region);
                } catch (Exception e) {
                    log.error("failed to calc average quota waiting time", e);
                }
            }
        } else {
            region.setOpen(Boolean.FALSE);
            region.setRemainQuota(0);
        }
        if (!regionStatCache.containsKey(cacheUser)) {
            regionStatCache.put(cacheUser, Maps.newHashMap());
            regionStatCacheTime.put(cacheUser, Maps.newHashMap());
        }
        regionStatCache.get(cacheUser).put(regionName, region);
        regionStatCacheTime.get(cacheUser).put(regionName, ZonedDateTime.now());
        return ResponseEntity.ok(region);
    }

}
