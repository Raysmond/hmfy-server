package com.shield.web.rest;

import com.shield.service.AppointmentService;
import com.shield.service.RegionService;
import com.shield.service.ShipPlanService;

import com.shield.web.rest.vm.AppointmentStat;
import com.shield.web.rest.vm.RegionStatDTO;
import com.shield.web.rest.vm.WeightStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/admin-dashboard")
public class AdminDashboardResource {
    @Autowired
    private RegionService regionService;

    @Autowired
    private ShipPlanService shipPlanService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/region-stats")
    public RegionStatDTO getAllAppointments() {
        return regionService.countRegionStat();
    }

    @GetMapping("/weight-stats")
    public WeightStat getWeightStatCount(@RequestParam String currentRegion, @RequestParam String date) {
        ZonedDateTime begin = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault());
        ZonedDateTime end = begin.plusDays(1);
        if (date.equals("昨日")) {
            begin = begin.minusDays(1);
            end = end.minusDays(1);
        } else if (date.equals("最近七天")) {
            begin = begin.minusDays(7);
            end = end.minusDays(1);
        }
        return shipPlanService.countWeightStat(currentRegion, begin, end);
    }

    @GetMapping("/appointment-stats")
    public AppointmentStat getAppointmentStat(@RequestParam String date) {
        ZonedDateTime begin = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault());
        ZonedDateTime end = begin.plusDays(1);
        if (date.equals("昨日")) {
            begin = begin.minusDays(1);
            end = end.minusDays(1);
        } else if (date.equals("最近七天")) {
            begin = begin.minusDays(7);
            end = end.minusDays(1);
        }
        return appointmentService.countAppointmentStat(begin, end);
    }

    @GetMapping("/appointment-stats-today")
    public AppointmentStat getAppointmentStatToday(@RequestParam String currentRegion, @RequestParam String date) {
        ZonedDateTime begin = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault());
        ZonedDateTime end = begin.plusDays(1);
        if (date.equals("昨日")) {
            begin = begin.minusDays(1);
            end = end.minusDays(1);
        } else if (date.equals("最近七天")) {
            begin = begin.minusDays(7);
            end = end.minusDays(1);
        }

        return appointmentService.countAppointmentStat(currentRegion, begin, end);
    }
}
