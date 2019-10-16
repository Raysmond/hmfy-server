package com.shield.service.dto;
import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.shield.domain.ShipPlan} entity.
 */
public class ShipPlanDTO implements Serializable {

    private Long id;

    private String company;

    //    @NotNull
    private Long applyId;

    private String applyNumber;

    @NotNull
    private String truckNumber;

    @NotNull
    private Integer auditStatus;

    @NotNull
    private String productName;

    //    @NotNull
    private String deliverPosition;

    //    @NotNull
    private Boolean valid;

    private ZonedDateTime loadingStartTime;

    private ZonedDateTime loadingEndTime;

    private ZonedDateTime gateTime;

    private ZonedDateTime leaveTime;

    @NotNull
    private ZonedDateTime deliverTime;

    private ZonedDateTime allowInTime;

    //    @NotNull
    private ZonedDateTime createTime;

    //    @NotNull
    private ZonedDateTime updateTime;

    private ZonedDateTime syncTime;

    private Long userId;

    private String userLogin;

    private Boolean tareAlert;

    private Boolean leaveAlert;

    private Boolean vip;

    public Boolean getTareAlert() {
        return tareAlert;
    }

    public Boolean getLeaveAlert() {
        return leaveAlert;
    }

    private Double netWeight;

    private String weigherNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public String getApplyNumber() {
        return applyNumber;
    }

    public void setApplyNumber(String applyNumber) {
        this.applyNumber = applyNumber;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDeliverPosition() {
        return deliverPosition;
    }

    public void setDeliverPosition(String deliverPosition) {
        this.deliverPosition = deliverPosition;
    }

    public Boolean isValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public ZonedDateTime getGateTime() {
        return gateTime;
    }

    public void setGateTime(ZonedDateTime gateTime) {
        this.gateTime = gateTime;
    }

    public ZonedDateTime getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(ZonedDateTime leaveTime) {
        this.leaveTime = leaveTime;
    }

    public ZonedDateTime getDeliverTime() {
        return deliverTime;
    }

    public void setDeliverTime(ZonedDateTime deliverTime) {
        this.deliverTime = deliverTime;
    }

    public ZonedDateTime getAllowInTime() {
        return allowInTime;
    }

    public void setAllowInTime(ZonedDateTime allowInTime) {
        this.allowInTime = allowInTime;
    }

    public ZonedDateTime getLoadingStartTime() {
        return loadingStartTime;
    }

    public void setLoadingStartTime(ZonedDateTime loadingStartTime) {
        this.loadingStartTime = loadingStartTime;
    }

    public ZonedDateTime getLoadingEndTime() {
        return loadingEndTime;
    }

    public void setLoadingEndTime(ZonedDateTime loadingEndTime) {
        this.loadingEndTime = loadingEndTime;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public ZonedDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(ZonedDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public ZonedDateTime getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(ZonedDateTime syncTime) {
        this.syncTime = syncTime;
    }

    public Boolean isTareAlert() {
        return tareAlert;
    }

    public void setTareAlert(Boolean tareAlert) {
        this.tareAlert = tareAlert;
    }

    public Boolean isLeaveAlert() {
        return leaveAlert;
    }

    public void setLeaveAlert(Boolean leaveAlert) {
        this.leaveAlert = leaveAlert;
    }

    public Double getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Double netWeight) {
        this.netWeight = netWeight;
    }

    public String getWeigherNo() {
        return weigherNo;
    }

    public void setWeigherNo(String weigherNo) {
        this.weigherNo = weigherNo;
    }

    public Boolean isVip() {
        return vip;
    }

    public void setVip(Boolean vip) {
        this.vip = vip;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShipPlanDTO shipPlanDTO = (ShipPlanDTO) o;
        if (shipPlanDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), shipPlanDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ShipPlanDTO{" +
            "id=" + getId() +
            ", company='" + getCompany() + "'" +
            ", applyId=" + getApplyId() +
            ", applyNumber='" + getApplyNumber() + "'" +
            ", truckNumber='" + getTruckNumber() + "'" +
            ", auditStatus=" + getAuditStatus() +
            ", productName='" + getProductName() + "'" +
            ", deliverPosition='" + getDeliverPosition() + "'" +
            ", valid='" + isValid() + "'" +
            ", gateTime='" + getGateTime() + "'" +
            ", leaveTime='" + getLeaveTime() + "'" +
            ", deliverTime='" + getDeliverTime() + "'" +
            ", allowInTime='" + getAllowInTime() + "'" +
            ", loadingStartTime='" + getLoadingStartTime() + "'" +
            ", loadingEndTime='" + getLoadingEndTime() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            ", syncTime='" + getSyncTime() + "'" +
            ", tareAlert='" + isTareAlert() + "'" +
            ", leaveAlert='" + isLeaveAlert() + "'" +
            ", netWeight=" + getNetWeight() +
            ", weigherNo='" + getWeigherNo() + "'" +
            ", vip='" + isVip() + "'" +
            ", user=" + getUserId() +
            ", user='" + getUserLogin() + "'" +
            "}";
    }
}
