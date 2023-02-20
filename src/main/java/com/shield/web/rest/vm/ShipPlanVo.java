package com.shield.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shield.service.dto.ShipPlanDTO;
import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class ShipPlanVo {
    private Long planId;
    private String appointmentNumber;
    private Integer status;
    private String customerName;
    private Double planWeight;
    private String truckNumber;
    private String productName;
    private String deliverPosition;
    private String destinationAddress;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private ZonedDateTime deliverTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private ZonedDateTime loadingStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private ZonedDateTime loadingEndTime;
    private Double netWeight;
    private Double tareWeight;
    private String weigherNo;
    private String weightCode;
    private String truckOn;
    private String truckOnCname;

    private String uniqueQrcodeNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private ZonedDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private ZonedDateTime updateTime;

    public ShipPlanVo() {

    }

    public ShipPlanVo(ShipPlanDTO planDTO) {
        this.planId = planDTO.getId();
        this.appointmentNumber = planDTO.getAppointmentNumber();
        this.status = planDTO.getAuditStatus();
        this.truckNumber = planDTO.getTruckNumber();
        this.productName = planDTO.getProductName();
        this.deliverPosition = planDTO.getDeliverPosition();
        this.destinationAddress = planDTO.getDestinationAddress();
        this.deliverTime = planDTO.getDeliverTime();
        this.loadingStartTime = planDTO.getLoadingStartTime();
        this.loadingEndTime = planDTO.getLoadingEndTime();
        this.netWeight = planDTO.getNetWeight();
        this.tareWeight = planDTO.getTareWeight();
        this.weigherNo = planDTO.getWeigherNo();
        this.weightCode = planDTO.getWeightCode();
        this.truckOn = planDTO.getTruckOn();
//        this.planWeight = planDTO.getPlanWeight();
        this.customerName = planDTO.getCompany();
        this.truckOnCname = planDTO.getTruckOnCname();
        this.createTime = planDTO.getCreateTime();
        this.updateTime = planDTO.getUpdateTime();
        if (planDTO.getApplyId() != null && planDTO.getAppointmentNumber() != null) {
            this.uniqueQrcodeNumber = planDTO.getUniqueQrcodeNumber();
        }
    }
}
