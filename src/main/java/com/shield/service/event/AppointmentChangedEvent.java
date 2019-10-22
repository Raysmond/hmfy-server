package com.shield.service.event;

import com.shield.service.dto.AppointmentDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class AppointmentChangedEvent extends ApplicationEvent {
    private AppointmentDTO before;
    private AppointmentDTO after;

    public AppointmentChangedEvent(Object source, AppointmentDTO before, AppointmentDTO after) {
        super(source);
        this.before = before;
        this.after = after;
    }
}
