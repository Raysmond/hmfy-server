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

    @NotNull
    private Integer applyId;

    private String applyNumber;

    @NotNull
    private String truckNumber;

    @NotNull
    private Integer auditStatus;

    private ZonedDateTime gateTime;

    private ZonedDateTime leaveTime;

    private ZonedDateTime deliverTime;

    private ZonedDateTime allowInTime;

    @NotNull
    private ZonedDateTime createTime;

    @NotNull
    private ZonedDateTime updateTime;


    private Long userId;

    private String userLogin;

    private Long toUserId;

    private String toUserLogin;

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

    public Integer getApplyId() {
        return applyId;
    }

    public void setApplyId(Integer applyId) {
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

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long userId) {
        this.toUserId = userId;
    }

    public String getToUserLogin() {
        return toUserLogin;
    }

    public void setToUserLogin(String userLogin) {
        this.toUserLogin = userLogin;
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
            ", gateTime='" + getGateTime() + "'" +
            ", leaveTime='" + getLeaveTime() + "'" +
            ", deliverTime='" + getDeliverTime() + "'" +
            ", allowInTime='" + getAllowInTime() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            ", user=" + getUserId() +
            ", user='" + getUserLogin() + "'" +
            ", toUser=" + getToUserId() +
            ", toUser='" + getToUserLogin() + "'" +
            "}";
    }
}
