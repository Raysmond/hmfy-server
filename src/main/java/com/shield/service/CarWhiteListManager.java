package com.shield.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.dto.ShipPlanDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

import static com.shield.service.ParkingHandlerService.*;

@Service
@Slf4j
public class CarWhiteListManager {
    private final RegionService regionService;

    private final CarWhiteListService carWhiteListService;

    private final HuachanCarWhitelistService huachanCarWhitelistService;

    private final RedisTemplate<String, Long> redisLongTemplate;


    @Autowired
    public CarWhiteListManager(
        RegionService regionService,
        CarWhiteListService carWhiteListService,
        HuachanCarWhitelistService huachanCarWhitelistService,
        @Qualifier("redisLongTemplate") RedisTemplate<String, Long> redisLongTemplate) {
        this.regionService = regionService;
        this.carWhiteListService = carWhiteListService;
        this.huachanCarWhitelistService = huachanCarWhitelistService;
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
                    redisLongTemplate.opsForSet().remove(REDIS_KEY_DELETE_CAR_WHITELIST, appointmentDTO.getId());
                    redisLongTemplate.opsForSet().add(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointmentDTO.getId());
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
                    redisLongTemplate.opsForSet().remove(REDIS_KEY_UPLOAD_CAR_WHITELIST, appointmentDTO.getId());
                    redisLongTemplate.opsForSet().add(REDIS_KEY_DELETE_CAR_WHITELIST, appointmentDTO.getId());
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
                    redisLongTemplate.opsForSet().remove(AUTO_REGISTERED_PLAN_IDS, planDTO.getId());
                    redisLongTemplate.opsForSet().remove(AUTO_DELETE_PLAN_ID_QUEUE, planDTO.getId());
                    redisLongTemplate.opsForSet().add(AUTO_REGISTER_PLAN_ID_QUEUE, planDTO.getId());
                    break;
                case DATABASE:
                    carWhiteListService.registerCarWhiteList(
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
                    redisLongTemplate.opsForSet().remove(AUTO_REGISTER_PLAN_ID_QUEUE, planDTO.getId());
                    redisLongTemplate.opsForSet().add(AUTO_DELETE_PLAN_ID_QUEUE, planDTO.getId());
                    break;
                case DATABASE:
                    carWhiteListService.deleteCarWhiteList(planDTO.getTruckNumber());
                    break;
            }
        }
    }

}
