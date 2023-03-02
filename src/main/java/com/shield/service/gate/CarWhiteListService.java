package com.shield.service.gate;

import java.time.ZonedDateTime;

public interface CarWhiteListService {
    void registerCarWhiteListByAppointmentId(Long id);

    void deleteCarWhiteList(String licensePlateNumber);

    void registerCarWhiteList(Long id, String truckNumber, ZonedDateTime now, ZonedDateTime plusHours, String truckNumber1);
}
