package com.shield.service.dto;

import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import com.shield.domain.enumeration.AppointmentStatus;

/**
 * A DTO for the {@link com.shield.domain.Appointment} entity.
 */
public class AppointmentDTO implements Serializable {

    private Long id;

    @NotNull
    private String licensePlateNumber;

    @NotNull
    private String driver;

    private String phone;

    private Integer number;

    @NotNull
    private Boolean valid;

    @NotNull
    private AppointmentStatus status;

    private Integer queueNumber;

    @NotNull
    private Boolean vip;

    private ZonedDateTime createTime;

    private ZonedDateTime updateTime;

    private ZonedDateTime startTime;

    private ZonedDateTime enterTime;

    private ZonedDateTime leaveTime;

    private ZonedDateTime expireTime;


    private Long regionId;

    private String regionName;

    private Long userId;

    private String userLogin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public void setLicensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Boolean isValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public Integer getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(Integer queueNumber) {
        this.queueNumber = queueNumber;
    }

    public Boolean isVip() {
        return vip;
    }

    public void setVip(Boolean vip) {
        this.vip = vip;
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

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(ZonedDateTime enterTime) {
        this.enterTime = enterTime;
    }

    public ZonedDateTime getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(ZonedDateTime leaveTime) {
        this.leaveTime = leaveTime;
    }

    public ZonedDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(ZonedDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
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

        AppointmentDTO appointmentDTO = (AppointmentDTO) o;
        if (appointmentDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), appointmentDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "AppointmentDTO{" +
            "id=" + getId() +
            ", licensePlateNumber='" + getLicensePlateNumber() + "'" +
            ", driver='" + getDriver() + "'" +
            ", phone='" + getPhone() + "'" +
            ", number=" + getNumber() +
            ", valid='" + isValid() + "'" +
            ", status='" + getStatus() + "'" +
            ", queueNumber=" + getQueueNumber() +
            ", vip='" + isVip() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            ", startTime='" + getStartTime() + "'" +
            ", enterTime='" + getEnterTime() + "'" +
            ", leaveTime='" + getLeaveTime() + "'" +
            ", expireTime='" + getExpireTime() + "'" +
            ", region=" + getRegionId() +
            ", region='" + getRegionName() + "'" +
            ", user=" + getUserId() +
            ", user='" + getUserLogin() + "'" +
            "}";
    }
}
