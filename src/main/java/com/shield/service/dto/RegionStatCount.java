package com.shield.service.dto;

import com.shield.domain.enumeration.AppointmentStatus;

public interface RegionStatCount {
    Long getRegionId();
    AppointmentStatus getStatus();
    Long getAppointmentCount();
}
