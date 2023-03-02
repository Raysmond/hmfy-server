package com.shield.service.gate;

import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class CarWhiteListServiceImpl implements CarWhiteListService {
    @Override
    public void registerCarWhiteListByAppointmentId(Long id) {

    }

    @Override
    public void deleteCarWhiteList(String licensePlateNumber) {

    }

    @Override
    public void registerCarWhiteList(Long id, String truckNumber, ZonedDateTime now, ZonedDateTime plusHours, String truckNumber1) {

    }
}
