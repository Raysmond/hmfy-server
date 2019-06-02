package com.shield.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.shield.domain.enumeration.AppointmentStatus;

/**
 * A Appointment.
 */
@Entity
@Table(name = "appointment")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "license_plate_number", nullable = false)
    private String licensePlateNumber;

    @NotNull
    @Column(name = "driver", nullable = false)
    private String driver;

    @Column(name = "phone")
    private String phone;

    
    @Column(name = "jhi_number", unique = true)
    private Integer number;

    @NotNull
    @Column(name = "valid", nullable = false)
    private Boolean valid;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @Column(name = "queue_number")
    private Integer queueNumber;

    @NotNull
    @Column(name = "vip", nullable = false)
    private Boolean vip;

    @Column(name = "create_time")
    private ZonedDateTime createTime;

    @Column(name = "update_time")
    private ZonedDateTime updateTime;

    @Column(name = "start_time")
    private ZonedDateTime startTime;

    @Column(name = "enter_time")
    private ZonedDateTime enterTime;

    @Column(name = "leave_time")
    private ZonedDateTime leaveTime;

    @Column(name = "expire_time")
    private ZonedDateTime expireTime;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties("appointments")
    private Region region;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties("appointments")
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public Appointment licensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
        return this;
    }

    public void setLicensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
    }

    public String getDriver() {
        return driver;
    }

    public Appointment driver(String driver) {
        this.driver = driver;
        return this;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPhone() {
        return phone;
    }

    public Appointment phone(String phone) {
        this.phone = phone;
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getNumber() {
        return number;
    }

    public Appointment number(Integer number) {
        this.number = number;
        return this;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Boolean isValid() {
        return valid;
    }

    public Appointment valid(Boolean valid) {
        this.valid = valid;
        return this;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public Appointment status(AppointmentStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public Integer getQueueNumber() {
        return queueNumber;
    }

    public Appointment queueNumber(Integer queueNumber) {
        this.queueNumber = queueNumber;
        return this;
    }

    public void setQueueNumber(Integer queueNumber) {
        this.queueNumber = queueNumber;
    }

    public Boolean isVip() {
        return vip;
    }

    public Appointment vip(Boolean vip) {
        this.vip = vip;
        return this;
    }

    public void setVip(Boolean vip) {
        this.vip = vip;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public Appointment createTime(ZonedDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public ZonedDateTime getUpdateTime() {
        return updateTime;
    }

    public Appointment updateTime(ZonedDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public void setUpdateTime(ZonedDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public Appointment startTime(ZonedDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEnterTime() {
        return enterTime;
    }

    public Appointment enterTime(ZonedDateTime enterTime) {
        this.enterTime = enterTime;
        return this;
    }

    public void setEnterTime(ZonedDateTime enterTime) {
        this.enterTime = enterTime;
    }

    public ZonedDateTime getLeaveTime() {
        return leaveTime;
    }

    public Appointment leaveTime(ZonedDateTime leaveTime) {
        this.leaveTime = leaveTime;
        return this;
    }

    public void setLeaveTime(ZonedDateTime leaveTime) {
        this.leaveTime = leaveTime;
    }

    public ZonedDateTime getExpireTime() {
        return expireTime;
    }

    public Appointment expireTime(ZonedDateTime expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public void setExpireTime(ZonedDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Region getRegion() {
        return region;
    }

    public Appointment region(Region region) {
        this.region = region;
        return this;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public User getUser() {
        return user;
    }

    public Appointment user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Appointment)) {
            return false;
        }
        return id != null && id.equals(((Appointment) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Appointment{" +
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
            "}";
    }
}
