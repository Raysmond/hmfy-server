package com.shield.chepaipark.domain;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "t_parkcard")
@Data
public class ParkCard implements Serializable {
    @Id
    @Column(name = "CID")
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cid;

    // 司机
    @Column(name = "UserName")
    private String userName;

    // 车牌号
    @Column(name = "CardNo")
    private String cardNo;

    @Column(name = "CTID")
    private Integer ctid = 1;

    @Column(name = "FCTCode")
    private Integer fctCode = 1;

    @Column(name = "CardState")
    private Integer cardState = 1;

    @Column(name = "RegisterDate")
    private ZonedDateTime registerDate;

    @Column(name = "Address")
    private String address;

    @Column(name = "Phone")
    private String phone;

    // 车牌号
    @Column(name = "CarNo")
    private String carNo;

    @Column(name = "CardMoney")
    private Double cardMoney;

    @Column(name = "CarColor")
    private String carColor;

    // 驾驶证
    @Column(name = "DriveNo")
    private String driveNo;

    @Column(name = "ShopNo")
    private String shopNo;

    @Column(name = "StartDate")
    private ZonedDateTime startDate;

    @Column(name = "ValidDate")
    private ZonedDateTime validDate;

    @Column(name = "OutGateID")
    private Integer outGateId;

    @Column(name = "InGateID")
    private Integer inGateId;

    @Column(name = "InDate")
    private ZonedDateTime inDate;

    @Column(name = "OutDate")
    private ZonedDateTime outDate;

    @Column(name = "CarLocate")
    private String carLocate;

    @Column(name = "CUser")
    private String cUser;

    @Column(name = "Remark")
    private String remark;

    @Column(name = "CDate")
    private ZonedDateTime cDate;

    @Column(name = "lasttime")
    private ZonedDateTime lastTime;

    @Column(name = "GrantState")
    private Integer grantState;

    @Column(name = "ChargeType")
    private Integer chargeType;

    @Column(name = "IOState")
    private Integer ioState = 0;

    @Column(name = "HCardNo")
    private String hcardNo; // 190713210155473

    @Column(name = "FeePeriod")
    private String feePeriod; // 月

    @Column(name = "AreaID")
    private Integer areaId = -1;

    @Column(name = "ZMCarLocateCount")
    private Integer zMCarLocateCount = 0;

    @Column(name = "ZMUsedLocateCount")
    private Integer zMUsedLocateCount = 0;

    @Column(name = "LimitDayType")
    private Integer limitDayType = 0;

    @Column(name = "Uploaded")
    private Integer uploaded = 0;

    @Column(name = "UploadedComm")
    private Integer uploadedComm = 0;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkCard parkCard = (ParkCard) o;
        return cid.equals(parkCard.cid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cid);
    }
}
