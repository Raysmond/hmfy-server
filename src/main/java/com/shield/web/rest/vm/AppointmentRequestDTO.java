package com.shield.web.rest.vm;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class AppointmentRequestDTO implements Serializable {

    @NotNull
    private Long regionId;

    @NotNull
    private String licensePlateNumber;

    @NotNull
    private String driver;

    @NotNull
    private String phone;

}
