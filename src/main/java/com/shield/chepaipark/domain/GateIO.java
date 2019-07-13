package com.shield.chepaipark.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "pk_gateio")
@Data
public class GateIO implements Serializable {
    @Id
    @Column(name = "RecordID")
    private Long recordId;

    @Column(name = "CardNo")
    private String cardNo;

    @Column(name = "CardType")
    private Integer cardType;

    @Column(name = "CarUser")
    private String carUser;

    @Column(name = "InCarMark")
    private String inCarMark;

    @Column(name = "OutCarMark")
    private String outCarMark;

    @Column(name = "CarType")
    private Integer carType;

    @Column(name = "ChargeType")
    private Integer chargeType;

    @Column(name = "InDeviceID")
    private Integer inDeviceId;

    @Column(name = "OutDeviceID")
    private Integer outDeviceId;

    @Column(name = "GateInID")
    private Integer gateInID;

    @Column(name = "GateOutID")
    private Integer gateOutID;

    @Column(name = "GateInTime")
    private ZonedDateTime gateInTime;

    @Column(name = "GateOutTime")
    private ZonedDateTime gateOutTime;

    @Column(name = "GateInName")
    private String gateInName;

    @Column(name = "GateOutName")
    private String gateOutName;

    @Column(name = "IOState")
    private Integer ioState;

    @Column(name = "CreateTime")
    private ZonedDateTime createTime;

    @Column(name = "CarTypedesc")
    private String carTypeDesc; // 大车

    @Column(name = "InSynState")
    private Integer inSynState;

    @Column(name = "UpdatedTime")
    private ZonedDateTime updatedTime;

    @Column(name = "RecordType")
    private Integer recordType;

    @Column(name = "AreaID")
    private Integer areaId;

    @Column(name = "AreaName")
    private String areaName;

    @Column(name = "BlueToothState")
    private Integer blueToothState;

    @Column(name = "payed")
    private Integer payed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GateIO gateIO = (GateIO) o;
        return recordId.equals(gateIO.recordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }
}
