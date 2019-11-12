package com.shield.service.dto;

import com.shield.domain.enumeration.ParkingConnectMethod;

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.shield.domain.Region} entity.
 */
public class RegionDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    @Min(value = 0)
    private Integer quota;

    @NotNull
    @Min(value = 0)
    private Integer vipQuota;

    private String startTime;

    private String endTime;

    private String days;

    private Boolean open;

    private Boolean autoAppointment;

    private String parkId;

    private Integer remainQuota = 0;
    private Integer drivers = 0;
    private Integer nextQuotaWaitTime = 0;
    private Long statusStart = 0L;
    private Long statusEnter = 0L;
    private Long statusStartCheck = 0L;
    private Integer nextQuotaNumber = 0;
    private Long statusWait = 0L;
    private Boolean userInWaitingList = Boolean.FALSE;
    private Long statusWaitBeforeUser = 0L;
    private Integer waitTime = 0;

    private ParkingConnectMethod parkingConnectMethod;

    @NotNull
    @Min(value = 0)
    @Max(value = 1440)
    @Column(name = "load_alert_time", nullable = false)
    private Integer loadAlertTime;

    @NotNull
    @Min(value = 0)
    @Max(value = 1440)
    @Column(name = "leave_alert_time", nullable = false)
    private Integer leaveAlertTime;

    @NotNull
    @Min(value = 0)
    private Integer validTime;

    @NotNull
    @Min(value = 0)
    private Integer queueQuota;

    @NotNull
    @Min(value = 0)
    private Integer queueValidTime;

    private ZonedDateTime createTime;

    private ZonedDateTime updateTime;

    public Integer getDrivers() {
        return drivers;
    }

    public void setDrivers(Integer drivers) {
        this.drivers = drivers;
    }

    public Integer getRemainQuota() {
        return remainQuota;
    }

    public void setRemainQuota(Integer remainQuota) {
        this.remainQuota = remainQuota;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Integer getVipQuota() {
        return vipQuota;
    }

    public void setVipQuota(Integer vipQuota) {
        this.vipQuota = vipQuota;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public Boolean isOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public Boolean isAutoAppointment() {
        return autoAppointment;
    }

    public void setAutoAppointment(Boolean autoAppointment) {
        this.autoAppointment = autoAppointment;
    }

    public String getParkId() {
        return parkId;
    }

    public void setParkId(String parkId) {
        this.parkId = parkId;
    }

    public Integer getValidTime() {
        return validTime;
    }

    public void setValidTime(Integer validTime) {
        this.validTime = validTime;
    }

    public Integer getQueueQuota() {
        return queueQuota;
    }

    public void setQueueQuota(Integer queueQuota) {
        this.queueQuota = queueQuota;
    }

    public Integer getQueueValidTime() {
        return queueValidTime;
    }

    public void setQueueValidTime(Integer queueValidTime) {
        this.queueValidTime = queueValidTime;
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

    public ParkingConnectMethod getParkingConnectMethod() {
        return parkingConnectMethod;
    }

    public void setParkingConnectMethod(ParkingConnectMethod parkingConnectMethod) {
        this.parkingConnectMethod = parkingConnectMethod;
    }

    public Integer getNextQuotaWaitTime() {
        return nextQuotaWaitTime;
    }

    public void setNextQuotaWaitTime(Integer nextQuotaWaitTime) {
        this.nextQuotaWaitTime = nextQuotaWaitTime;
    }

    public Long getStatusStart() {
        return statusStart;
    }

    public void setStatusStart(Long statusStart) {
        this.statusStart = statusStart;
    }

    public Long getStatusEnter() {
        return statusEnter;
    }

    public void setStatusEnter(Long statusEnter) {
        this.statusEnter = statusEnter;
    }

    public Integer getNextQuotaNumber() {
        return nextQuotaNumber;
    }

    public void setNextQuotaNumber(Integer nextQuotaNumber) {
        this.nextQuotaNumber = nextQuotaNumber;
    }

    public Boolean getUserInWaitingList() {
        return userInWaitingList;
    }

    public void setUserInWaitingList(Boolean userInWaitingList) {
        this.userInWaitingList = userInWaitingList;
    }

    public Long getStatusWaitBeforeUser() {
        return statusWaitBeforeUser;
    }

    public void setStatusWaitBeforeUser(Long statusWaitBeforeUser) {
        this.statusWaitBeforeUser = statusWaitBeforeUser;
    }

    public Long getStatusWait() {
        return statusWait;
    }

    public void setStatusWait(Long statusWait) {
        this.statusWait = statusWait;
    }

    public Integer getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Integer waitTime) {
        this.waitTime = waitTime;
    }

    public Integer getLoadAlertTime() {
        return loadAlertTime;
    }

    public void setLoadAlertTime(Integer loadAlertTime) {
        this.loadAlertTime = loadAlertTime;
    }

    public Integer getLeaveAlertTime() {
        return leaveAlertTime;
    }

    public void setLeaveAlertTime(Integer leaveAlertTime) {
        this.leaveAlertTime = leaveAlertTime;
    }

    public Long getStatusStartCheck() {
        return statusStartCheck;
    }

    public void setStatusStartCheck(Long statusStartCheck) {
        this.statusStartCheck = statusStartCheck;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RegionDTO regionDTO = (RegionDTO) o;
        if (regionDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), regionDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RegionDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", quota=" + getQuota() +
            ", vipQuota=" + getVipQuota() +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            ", days='" + getDays() + "'" +
            ", open='" + isOpen() + "'" +
            ", autoAppointment='" + isAutoAppointment() + "'" +
            ", parkId='" + getParkId() + "'" +
            ", validTime=" + getValidTime() +
            ", queueQuota=" + getQueueQuota() +
            ", queueValidTime=" + getQueueValidTime() +
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            "}";
    }

}
