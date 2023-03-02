package com.shield.service.gate;

import com.shield.service.RegionService;
import com.shield.service.gate.CarWhiteListService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;


@Service
@Slf4j
public class CarWhiteListManager {
    private final RegionService regionService;

    private final CarWhiteListService carWhiteListService;

    private final RedisTemplate<String, Long> redisLongTemplate;


    @Autowired
    public CarWhiteListManager(
        RegionService regionService,
        CarWhiteListService carWhiteListService,
        @Qualifier("redisLongTemplate") RedisTemplate<String, Long> redisLongTemplate) {
        this.regionService = regionService;
        this.carWhiteListService = carWhiteListService;
        this.redisLongTemplate = redisLongTemplate;
    }

    private boolean isRegionOpen(RegionDTO region) {
        return region != null && region.isOpen() && region.getParkingConnectMethod() != null;
    }

    public void registerCarWhiteList(AppointmentDTO appointmentDTO) {
        RegionDTO region = regionService.findOne(appointmentDTO.getRegionId()).get();
        if (isRegionOpen(region)) {
            switch (region.getParkingConnectMethod()) {
                case HUA_CHAN_API:
                    // 由定时任务进行预约
//                    if (appointmentDTO.getStatus() == AppointmentStatus.START_CHECK && StringUtils.isBlank(appointmentDTO.getHsCode())) {
//                        try {
//                            huachanCarWhitelistService.registerCar(appointmentDTO);
//                        } catch (JsonProcessingException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    break;
                case TCP:
                    break;
                case DATABASE:
                    carWhiteListService.registerCarWhiteListByAppointmentId(appointmentDTO.getId());
                    break;
            }
        }
    }

    public void deleteCarWhiteList(AppointmentDTO appointmentDTO) {
        RegionDTO region = regionService.findOne(appointmentDTO.getRegionId()).get();
        if (isRegionOpen(region)) {
            switch (region.getParkingConnectMethod()) {
                case HUA_CHAN_API:
                    // pass
                    break;
                case TCP:
                    break;
                case DATABASE:
                    carWhiteListService.deleteCarWhiteList(appointmentDTO.getLicensePlateNumber());
                    break;
            }
        }
    }

    public void registerCarWhiteList(ShipPlanDTO planDTO) {
        RegionDTO region = regionService.findByName(planDTO.getDeliverPosition());
        if (isRegionOpen(region)) {
            switch (region.getParkingConnectMethod()) {
                case HUA_CHAN_API:
                    // pass
                    break;
                case TCP:
                    break;
                case DATABASE:
                    carWhiteListService.registerCarWhiteList(
                        region.getId(),
                        planDTO.getTruckNumber(),
                        ZonedDateTime.now(),
                        ZonedDateTime.now().plusHours(region.getValidTime()),
                        planDTO.getTruckNumber());
                    break;
            }
        }
    }

    public void deleteCarWhiteList(ShipPlanDTO planDTO) {
        RegionDTO region = regionService.findByName(planDTO.getDeliverPosition());
        if (isRegionOpen(region)) {
            switch (region.getParkingConnectMethod()) {
                case HUA_CHAN_API:
                    // pass
                    break;
                case TCP:
                    break;
                case DATABASE:
                    carWhiteListService.deleteCarWhiteList(planDTO.getTruckNumber());
                    break;
            }
        }
    }

}
