package com.shield.service.event;

import com.shield.service.dto.ShipPlanDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class PlanChangedEvent extends ApplicationEvent {
    private ShipPlanDTO old;
    private ShipPlanDTO updated;

    public PlanChangedEvent(Object source, ShipPlanDTO old, ShipPlanDTO updated) {
        super(source);
        this.old = old;
        this.updated = updated;
    }
}
