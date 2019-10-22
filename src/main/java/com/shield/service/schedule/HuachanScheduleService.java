package com.shield.service.schedule;

import com.shield.service.HuachanCarWhitelistService;
import io.github.jhipster.config.JHipsterConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 化产车辆进厂预约，进出场数据获取定时任务
 */
@Service
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
@Slf4j
public class HuachanScheduleService {

    private final HuachanCarWhitelistService huachanCarWhitelistService;

    @Autowired
    public HuachanScheduleService(HuachanCarWhitelistService huachanCarWhitelistService) {
        this.huachanCarWhitelistService = huachanCarWhitelistService;
    }

    /**
     * 化产检查车辆预约审批结果
     */
    @Scheduled(fixedRate = 30 * 1000)
    public void checkRegisterStatus() {
        huachanCarWhitelistService.checkRegisterStatus();
    }

    /**
     * 化产获取出入场时间，更新预约/计划状态和时间
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void updateAppointmentStatusByGateRecords() {
        huachanCarWhitelistService.updateAppointmentStatusByGateRecords();
    }

    /**
     * 化产出入场时间同步到本地MySQL
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void syncCarInOutRecords() {
        huachanCarWhitelistService.syncCarInOutRecords();
    }
}
