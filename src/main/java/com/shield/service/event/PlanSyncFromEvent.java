package com.shield.service.event;

import com.shield.service.dto.ShipPlanDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class PlanSyncFromEvent extends ApplicationEvent {
    private ShipPlanDTO old;
    private ShipPlanDTO synced;

    public PlanSyncFromEvent(Object source, ShipPlanDTO old, ShipPlanDTO synced) {
        super(source);
        this.old = old;
        this.synced = synced;
    }
}
