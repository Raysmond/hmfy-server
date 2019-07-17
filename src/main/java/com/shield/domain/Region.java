package com.shield.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.shield.domain.enumeration.ParkingConnectMethod;

/**
 * A Region.
 */
@Entity
@Table(name = "region")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Region implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Min(value = 0)
    @Column(name = "jhi_quota", nullable = false)
    private Integer quota;

    @NotNull
    @Min(value = 0)
    @Column(name = "vip_quota", nullable = false)
    private Integer vipQuota;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_time")
    private String endTime;

    @Column(name = "days")
    private String days;

    @Column(name = "jhi_open")
    private Boolean open;

    @Column(name = "auto_appointment")
    private Boolean autoAppointment;

    @Enumerated(EnumType.STRING)
    @Column(name = "parking_connect_method")
    private ParkingConnectMethod parkingConnectMethod;

    @Column(name = "park_id")
    private String parkId;

    @NotNull
    @Min(value = 0)
    @Column(name = "valid_time", nullable = false)
    private Integer validTime;

    @NotNull
    @Min(value = 0)
    @Column(name = "queue_quota", nullable = false)
    private Integer queueQuota;

    @NotNull
    @Min(value = 0)
    @Column(name = "queue_valid_time", nullable = false)
    private Integer queueValidTime;

    @Column(name = "create_time")
    private ZonedDateTime createTime;

    @Column(name = "update_time")
    private ZonedDateTime updateTime;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Region name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuota() {
        return quota;
    }

    public Region quota(Integer quota) {
        this.quota = quota;
        return this;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Integer getVipQuota() {
        return vipQuota;
    }

    public Region vipQuota(Integer vipQuota) {
        this.vipQuota = vipQuota;
        return this;
    }

    public void setVipQuota(Integer vipQuota) {
        this.vipQuota = vipQuota;
    }

    public String getStartTime() {
        return startTime;
    }

    public Region startTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Region endTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDays() {
        return days;
    }

    public Region days(String days) {
        this.days = days;
        return this;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public Boolean isOpen() {
        return open;
    }

    public Region open(Boolean open) {
        this.open = open;
        return this;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public Boolean isAutoAppointment() {
        return autoAppointment;
    }

    public Region autoAppointment(Boolean autoAppointment) {
        this.autoAppointment = autoAppointment;
        return this;
    }

    public void setAutoAppointment(Boolean autoAppointment) {
        this.autoAppointment = autoAppointment;
    }

    public ParkingConnectMethod getParkingConnectMethod() {
        if (parkingConnectMethod == null) {
            return ParkingConnectMethod.TCP;
        }
        return parkingConnectMethod;
    }

    public Region parkingConnectMethod(ParkingConnectMethod parkingConnectMethod) {
        this.parkingConnectMethod = parkingConnectMethod;
        return this;
    }

    public void setParkingConnectMethod(ParkingConnectMethod parkingConnectMethod) {
        this.parkingConnectMethod = parkingConnectMethod;
    }

    public String getParkId() {
        return parkId;
    }

    public Region parkId(String parkId) {
        this.parkId = parkId;
        return this;
    }

    public void setParkId(String parkId) {
        this.parkId = parkId;
    }

    public Integer getValidTime() {
        return validTime;
    }

    public Region validTime(Integer validTime) {
        this.validTime = validTime;
        return this;
    }

    public void setValidTime(Integer validTime) {
        this.validTime = validTime;
    }

    public Integer getQueueQuota() {
        return queueQuota;
    }

    public Region queueQuota(Integer queueQuota) {
        this.queueQuota = queueQuota;
        return this;
    }

    public void setQueueQuota(Integer queueQuota) {
        this.queueQuota = queueQuota;
    }

    public Integer getQueueValidTime() {
        return queueValidTime;
    }

    public Region queueValidTime(Integer queueValidTime) {
        this.queueValidTime = queueValidTime;
        return this;
    }

    public void setQueueValidTime(Integer queueValidTime) {
        this.queueValidTime = queueValidTime;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public Region createTime(ZonedDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public ZonedDateTime getUpdateTime() {
        return updateTime;
    }

    public Region updateTime(ZonedDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public void setUpdateTime(ZonedDateTime updateTime) {
        this.updateTime = updateTime;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Region)) {
            return false;
        }
        return id != null && id.equals(((Region) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Region{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", quota=" + getQuota() +
            ", vipQuota=" + getVipQuota() +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            ", days='" + getDays() + "'" +
            ", open='" + isOpen() + "'" +
            ", autoAppointment='" + isAutoAppointment() + "'" +
            ", parkingConnectMethod='" + getParkingConnectMethod() + "'" +
            ", parkId='" + getParkId() + "'" +
            ", validTime=" + getValidTime() +
            ", queueQuota=" + getQueueQuota() +
            ", queueValidTime=" + getQueueValidTime() +
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            "}";
    }
}
