package com.shield.sqlserver.domain;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "t_VehDelivPlan")
@Getter
@Setter
public class VehDelivPlan {
    @Id
    @Column(name = "fd_ApplyID")
    private Long applyId;

    @Column(name = "fd_ApplyNumber")
    private String applyNumber;

    @Column(name = "fd_Customer")
    private String customer;

    @Column(name = "fd_TruckNumber")
    private String truckNumber;

    @Column(name = "fd_AuditStatus")
    private Integer auditStatus;

    @Column(name = "fd_DelivePosition")
    private String deliverPosition;

    @Column(name = "fd_ProductName")
    private String productName;

    @Column(name = "fd_CreatDate")
    private ZonedDateTime createTime;

    @Column(name = "fd_DeliDate")
    private ZonedDateTime deliverTime;

    @Column(name = "fd_AllowInTime")
    private ZonedDateTime allowInTime;

    @Column(name = "fd_GateTime")
    private ZonedDateTime gateTime;

    @Column(name = "fd_LeaveTime")
    private ZonedDateTime leaveTime;

    @Column(name = "fd_TareTime")
    private ZonedDateTime tareTime;

    @Column(name = "fd_WeightTime")
    private ZonedDateTime weightTime;

    @Column(name = "fd_NetWeight")
    private Double netWeight;

    @Column(name = "fd_WeigherNo")
    private String weigherNo;
}
