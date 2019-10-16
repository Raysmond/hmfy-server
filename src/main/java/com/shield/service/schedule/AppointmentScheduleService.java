package com.shield.service.schedule;

import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.ShipPlan;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.AppointmentService;
import io.github.jhipster.config.JHipsterConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.shield.config.Constants.AUTO_SET_LEAVE_TIME_AFTER_FINISH_HOURS;

@Service
@Slf4j
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class AppointmentScheduleService {
    private final RegionRepository regionRepository;

    private final AppointmentRepository appointmentRepository;

    private final ShipPlanRepository shipPlanRepository;

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentScheduleService(
        RegionRepository regionRepository,
        AppointmentRepository appointmentRepository,
        ShipPlanRepository shipPlanRepository,
        AppointmentService appointmentService
    ) {
        this.regionRepository = regionRepository;
        this.appointmentRepository = appointmentRepository;
        this.shipPlanRepository = shipPlanRepository;
        this.appointmentService = appointmentService;
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
            List<Appointment> waitingList = appointmentRepository.findWaitingList(region.getId(), ZonedDateTime.now().minusDays(2));
            for (Appointment appointment : waitingList) {
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
            List<Appointment> appointmentsShouldExpire = appointmentRepository
                .findAllByStatusAndStartTime(region.getId(), AppointmentStatus.START, Boolean.TRUE, now.minusHours(12)).stream()
                .filter(it -> !it.isVip() && it.getStartTime() != null)
                .filter(it -> it.getStartTime().plusHours(validHours).isBefore(ZonedDateTime.now()))
                .collect(Collectors.toList());

            for (Appointment appointment : appointmentsShouldExpire) {
                appointmentService.expireAppointment(appointment);
            }

            // Set appointment status to LEAVE after a long time
            List<Appointment> appointments = appointmentRepository
                .findAllByStatusAndStartTime(region.getId(), AppointmentStatus.ENTER, Boolean.TRUE, now.minusHours(12)).stream()
                .filter(it -> it.getApplyId() != null)
                .collect(Collectors.toList());
            for (Appointment appointment : appointments) {
                ShipPlan plan = shipPlanRepository.findOneByApplyId(appointment.getApplyId());
                if (plan.getLoadingEndTime() != null && plan.getLoadingEndTime().plusHours(AUTO_SET_LEAVE_TIME_AFTER_FINISH_HOURS).isBefore(now)) {
                    appointmentService.autoSetAppointmentLeave(appointment, plan);
                }
            }
        }
    }
}
