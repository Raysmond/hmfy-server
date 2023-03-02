package com.shield.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanAppointmentDTO {
    private ShipPlanDTO plan;
    private AppointmentDTO appointment;
    private String status = "";

    // 预约成功之后的准入时间
    private String maxAllowInTime;


    public PlanAppointmentDTO(ShipPlanDTO plan) {
        this.plan = plan;
    }
}
