package com.shield.sqlserver.domain;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "t_GateLog")
@Getter
@Setter
public class VipGateLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fd_Rowid")
    private Long rowId;

    @Column(name = "fd_TruckNumber")
    private String truckNumber;

    @Column(name = "fd_PlateColor")
    private String plateColor = "黄";

    @Column(name = "fd_Intime")
    private ZonedDateTime inTime;

    @Column(name = "fd_OutTime")
    private ZonedDateTime outTime;

    @Column(name = "fd_Note")
    private String note;
}
