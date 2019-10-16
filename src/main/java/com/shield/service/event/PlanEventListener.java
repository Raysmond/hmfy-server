package com.shield.service.event;

import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.domain.ShipPlan;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
public class PlanEventListener {

    private final AppointmentService appointmentService;

    private final CarWhiteListService carWhiteListService;

    private final ShipPlanRepository shipPlanRepository;

    @Autowired
    public PlanEventListener(
        AppointmentService appointmentService,
        CarWhiteListService carWhiteListService,
        ShipPlanRepository shipPlanRepository
    ) {
        this.appointmentService = appointmentService;
        this.carWhiteListService = carWhiteListService;
        this.shipPlanRepository = shipPlanRepository;
    }

    @Async
    @TransactionalEventListener
    public void handlePlanStatusChangeEvent(PlanStatusChangeEvent planStatusChangeEvent) {
        log.info("[EVENT] listen on PlanStatusChangeEvent event, applyId: {}, beforeStatus: {}, afterStatus: {}",
            planStatusChangeEvent.getApplyId(), planStatusChangeEvent.getBeforeStatus(), planStatusChangeEvent.getAfterStatus()
        );
//        ShipPlan plan = shipPlanRepository.findOneByApplyId(planStatusChangeEvent.getApplyId());
        Integer before = planStatusChangeEvent.getBeforeStatus();
        Integer after = planStatusChangeEvent.getAfterStatus();
        if (before.equals(1) && after.equals(2)) { // 计划取消
            appointmentService.updateStatusAfterCancelShipPlan(planStatusChangeEvent.getApplyId());
//            carWhiteListService.deleteCarWhiteList(plan.getTruckNumber());
        }
    }

    @Async
    @TransactionalEventListener
    public void handlePlanChangedEvent(PlanChangedEvent planChangedEvent) {
        log.info("[EVENT] listen on PlanChangedEvent, applyId: {}, truckNumber: {}",
            planChangedEvent.getOld().getApplyId(), planChangedEvent.getOld().getTruckNumber());
    }

    @Async
    @TransactionalEventListener
    public void handlePlanSyncFromEvent(PlanSyncFromEvent planSyncFromEvent) {
        log.info("[EVENT] listen on PlanSyncFromEvent, applyId: {}, truckNumber: {}",
            planSyncFromEvent.getOld().getApplyId(), planSyncFromEvent.getOld().getTruckNumber());

    }

}
