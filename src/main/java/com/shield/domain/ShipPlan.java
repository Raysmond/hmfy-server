package com.shield.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * A ShipPlan.
 */
@Entity
@Table(name = "ship_plan")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ShipPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company")
    private String company;

    @NotNull
    @Column(name = "apply_id", nullable = false)
    private Long applyId;

    @Column(name = "apply_number")
    private String applyNumber;

    @NotNull
    @Column(name = "truck_number", nullable = false)
    private String truckNumber;

    @NotNull
    @Column(name = "audit_status", nullable = false)
    private Integer auditStatus;

    @NotNull
    @Column(name = "product_name", nullable = false)
    private String productName;

    @NotNull
    @Column(name = "deliver_position", nullable = false)
    private String deliverPosition;

    @NotNull
    @Column(name = "valid", nullable = false)
    private Boolean valid;

    @Column(name = "gate_time")
    private ZonedDateTime gateTime;

    @Column(name = "leave_time")
    private ZonedDateTime leaveTime;

    @Column(name = "deliver_time")
    private ZonedDateTime deliverTime;

    @Column(name = "allow_in_time")
    private ZonedDateTime allowInTime;

    @NotNull
    @Column(name = "create_time", nullable = false)
    private ZonedDateTime createTime;

    @NotNull
    @Column(name = "update_time", nullable = false)
    private ZonedDateTime updateTime;

    @Column(name = "sync_time")
    private ZonedDateTime syncTime;

    @ManyToOne
    @JsonIgnoreProperties("shipPlans")
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public ShipPlan company(String company) {
        this.company = company;
        return this;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Long getApplyId() {
        return applyId;
    }

    public ShipPlan applyId(Long applyId) {
        this.applyId = applyId;
        return this;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public String getApplyNumber() {
        return applyNumber;
    }

    public ShipPlan applyNumber(String applyNumber) {
        this.applyNumber = applyNumber;
        return this;
    }

    public void setApplyNumber(String applyNumber) {
        this.applyNumber = applyNumber;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public ShipPlan truckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
        return this;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public ShipPlan auditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
        return this;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getProductName() {
        return productName;
    }

    public ShipPlan productName(String productName) {
        this.productName = productName;
        return this;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDeliverPosition() {
        return deliverPosition;
    }

    public ShipPlan deliverPosition(String deliverPosition) {
        this.deliverPosition = deliverPosition;
        return this;
    }

    public void setDeliverPosition(String deliverPosition) {
        this.deliverPosition = deliverPosition;
    }

    public Boolean isValid() {
        return valid;
    }

    public ShipPlan valid(Boolean valid) {
        this.valid = valid;
        return this;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public ZonedDateTime getGateTime() {
        return gateTime;
    }

    public ShipPlan gateTime(ZonedDateTime gateTime) {
        this.gateTime = gateTime;
        return this;
    }

    public void setGateTime(ZonedDateTime gateTime) {
        this.gateTime = gateTime;
    }

    public ZonedDateTime getLeaveTime() {
        return leaveTime;
    }

    public ShipPlan leaveTime(ZonedDateTime leaveTime) {
        this.leaveTime = leaveTime;
        return this;
    }

    public void setLeaveTime(ZonedDateTime leaveTime) {
        this.leaveTime = leaveTime;
    }

    public ZonedDateTime getDeliverTime() {
        return deliverTime;
    }

    public ShipPlan deliverTime(ZonedDateTime deliverTime) {
        this.deliverTime = deliverTime;
        return this;
    }

    public void setDeliverTime(ZonedDateTime deliverTime) {
        this.deliverTime = deliverTime;
    }

    public ZonedDateTime getAllowInTime() {
        return allowInTime;
    }

    public ShipPlan allowInTime(ZonedDateTime allowInTime) {
        this.allowInTime = allowInTime;
        return this;
    }

    public void setAllowInTime(ZonedDateTime allowInTime) {
        this.allowInTime = allowInTime;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public ShipPlan createTime(ZonedDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public ZonedDateTime getUpdateTime() {
        return updateTime;
    }

    public ShipPlan updateTime(ZonedDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public void setUpdateTime(ZonedDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public ZonedDateTime getSyncTime() {
        return syncTime;
    }

    public ShipPlan syncTime(ZonedDateTime syncTime) {
        this.syncTime = syncTime;
        return this;
    }

    public void setSyncTime(ZonedDateTime syncTime) {
        this.syncTime = syncTime;
    }

    public User getUser() {
        return user;
    }

    public ShipPlan user(User user) {
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
        if (!(o instanceof ShipPlan)) {
            return false;
        }
        return id != null && id.equals(((ShipPlan) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ShipPlan{" +
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
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            ", syncTime='" + getSyncTime() + "'" +
            "}";
    }
}
