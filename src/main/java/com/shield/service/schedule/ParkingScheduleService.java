package com.shield.service.schedule;

import com.shield.chepaipark.service.CarWhiteListService;
import io.github.jhipster.config.JHipsterConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class ParkingScheduleService {
    private final CarWhiteListService carWhiteListService;

    @Autowired
    public ParkingScheduleService(CarWhiteListService carWhiteListService) {
        this.carWhiteListService = carWhiteListService;
    }

    @Scheduled(fixedRate = 5 * 1000)
    public void syncCarGateIOEvents() {
        carWhiteListService.syncCarGateIOEvents();
    }
}
