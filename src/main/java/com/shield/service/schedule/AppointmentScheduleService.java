package com.shield.service.schedule;

import com.shield.domain.Region;
import com.shield.domain.ShipPlan;
import com.shield.domain.User;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.AppointmentService;
import com.shield.service.RegionService;
import com.shield.service.ShipPlanService;
import com.shield.service.UserService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.mapper.AppointmentMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shield.config.Constants.AUTO_SET_LEAVE_TIME_AFTER_FINISH_HOURS;
import static com.shield.config.Constants.VIP_CUSTOMER_COMPANIES;

@Service
@Slf4j
//@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class AppointmentScheduleService {
    private final RegionRepository regionRepository;

    private final AppointmentRepository appointmentRepository;

    private final ShipPlanRepository shipPlanRepository;

    private final AppointmentService appointmentService;

    private final AppointmentMapper appointmentMapper;

    private final RegionService regionService;

    private final ShipPlanService shipPlanService;

    private final UserService userService;


    @Autowired
    public AppointmentScheduleService(
        RegionRepository regionRepository,
        AppointmentRepository appointmentRepository,
        ShipPlanRepository shipPlanRepository,
        AppointmentService appointmentService,
        AppointmentMapper appointmentMapper,
        RegionService regionService,
        ShipPlanService shipPlanService,
        UserService userService) {
        this.regionRepository = regionRepository;
        this.appointmentRepository = appointmentRepository;
        this.shipPlanRepository = shipPlanRepository;
        this.appointmentService = appointmentService;
        this.appointmentMapper = appointmentMapper;
        this.regionService = regionService;
        this.shipPlanService = shipPlanService;
        this.userService = userService;
    }

    /**
     * 排队自动抢号
     */
    @Scheduled(fixedRate = 5 * 1000)
    public void autoMakeAppointmentForWaitingUsers() {
        List<Region> regions = regionRepository.findAll().stream()
            .filter(it -> it.isOpen() && it.getQueueQuota() != null && it.getQueueQuota() > 0)
            .collect(Collectors.toList());
        for (Region region : regions) {
            List<AppointmentDTO> waitingList = appointmentRepository
                .findWaitingList(region.getId(), ZonedDateTime.now().minusDays(2)).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
            List<AppointmentDTO> normalWaitingList = waitingList.stream().filter(it -> !it.isVip()).collect(Collectors.toList());
            List<AppointmentDTO> vipWaitingList = waitingList.stream().filter(AppointmentDTO::isVip).collect(Collectors.toList());

            for (AppointmentDTO appointment : vipWaitingList) {
                if (appointment.getCreateTime().plusHours(region.getQueueValidTime()).isBefore(ZonedDateTime.now())) {
                    appointmentService.expireWaitAppointment(appointment);
                } else if (!appointmentService.autoMakeAppointmentForWaitUser(appointment)) {
                    break;
                }
            }

            for (AppointmentDTO appointment : normalWaitingList) {
                if (appointment.getCreateTime().plusHours(region.getQueueValidTime()).isBefore(ZonedDateTime.now())) {
                    appointmentService.expireWaitAppointment(appointment);
                } else if (!appointmentService.autoMakeAppointmentForWaitUser(appointment)) {
                    break;
                }
            }
        }
    }


    /**
     * 检查预约状态
     * <p>
     * 1. 预约过期：START --> EXPIRED
     * 2. 拿不到出场时间失效：下磅之后1h，还没有拿到出场时间，则设置出场时间为当前系统时间，并且valid=FALSE
     */
    @Scheduled(fixedRate = 45 * 1000)
    public void checkAppointments() {
        ZonedDateTime now = ZonedDateTime.now();
        List<Region> regions = regionRepository.findAll().stream().filter(Region::isOpen).collect(Collectors.toList());
        for (Region region : regions) {
            long validHours = region.getValidTime().longValue();

            // Appointment was expired
            List<AppointmentDTO> appointmentsShouldExpire = appointmentRepository
                .findAllByStatusAndStartTime(region.getId(), AppointmentStatus.START, Boolean.TRUE, now.minusHours(12)).stream()
                .filter(it -> !it.isVip() && it.getStartTime() != null)
                .filter(it -> it.getStartTime().plusHours(validHours).isBefore(ZonedDateTime.now()))
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());

            for (AppointmentDTO appointment : appointmentsShouldExpire) {
                appointmentService.expireAppointment(appointment);
            }

            // Set appointment status to LEAVE after a long time
            List<AppointmentDTO> appointments = appointmentRepository
                .findAllByStatusAndStartTime(region.getId(), AppointmentStatus.ENTER, Boolean.TRUE, now.minusHours(12)).stream()
                .filter(it -> it.getApplyId() != null)
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
            for (AppointmentDTO appointment : appointments) {
                ShipPlan plan = shipPlanRepository.findOneByApplyId(appointment.getApplyId());
                if (plan.getLoadingEndTime() != null && plan.getLoadingEndTime().plusHours(AUTO_SET_LEAVE_TIME_AFTER_FINISH_HOURS).isBefore(now)) {
                    appointmentService.autoSetAppointmentLeave(appointment);
                }
            }
        }
    }

    /**
     * 自动预约VIP计划
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void autoRegisterVipPlans() {
        ZonedDateTime beginTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endTime = ZonedDateTime.now();
        List<RegionDTO> regions = regionService.findAllConnectParkingSystem();

        for (RegionDTO region : regions) {
            if (region.isOpen() && StringUtils.isNotBlank(region.getParkId())) {
                List<ShipPlanDTO> shipPlanDTOS = shipPlanService
                    .findAllByDeliverTime(region.getName(), beginTime, endTime, 1)
                    .stream().filter(it -> it.getCompany() != null && (VIP_CUSTOMER_COMPANIES.contains(it.getCompany()) || it.isVip()))
                    .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(shipPlanDTOS)) {
                    continue;
                }
                List<Long> applyIds = shipPlanDTOS.stream().map(ShipPlanDTO::getApplyId).collect(Collectors.toList());
                Map<Long, AppointmentDTO> appointments = appointmentService.findLastByApplyIdIn(applyIds);
                List<Long> newApplyIds = applyIds.stream().filter(it -> !appointments.containsKey(it) || !appointments.get(it).isValid()).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(newApplyIds)) {
                    continue;
                }
                log.info("Find {} apply_ids need to make vip appointment...", newApplyIds.size());
                shipPlanDTOS = shipPlanDTOS.stream().filter(it -> newApplyIds.contains(it.getApplyId())).collect(Collectors.toList());
                for (ShipPlanDTO planDTO : shipPlanDTOS) {
                    log.info("ShipPlan apply_id={}, truckNumber: {}, company: {}, need to make vip appointment...", planDTO.getApplyId(), planDTO.getTruckNumber(), planDTO.getCompany());
                    List<User> users = userService.findByTruckNumber(planDTO.getTruckNumber());
                    AppointmentDTO appointmentDTO = new AppointmentDTO();
                    appointmentDTO.setLicensePlateNumber(planDTO.getTruckNumber());
                    appointmentDTO.setDriver(users.size() > 0 ? users.get(0).getFirstName() : planDTO.getTruckNumber());
                    appointmentDTO.setRegionId(region.getId());
                    appointmentDTO.setApplyId(planDTO.getApplyId());
                    appointmentDTO.setVip(true);
                    appointmentDTO.setUserId(users.size() > 0 ? users.get(0).getId() : 3L);
                    AppointmentDTO appointment = appointmentService.makeAppointment(region.getId(), appointmentDTO);
                    if (!appointment.isValid() || !appointment.getStatus().equals(AppointmentStatus.START)) {
                        log.info("Failed to make vip appointment, not enough quota..");
                        appointmentService.delete(appointment.getId());
                        break;
                    } else {
                        log.info("ShipPlan apply_id={}, truckNumber: {}, company: {}, made vip appointment successfully", planDTO.getApplyId(), planDTO.getTruckNumber(), planDTO.getCompany());
                    }
                }
            }
        }
    }
}
