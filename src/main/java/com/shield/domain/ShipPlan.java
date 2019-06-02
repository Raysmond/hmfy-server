package com.shield.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.shield.domain.enumeration.ShipMethod;

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
    @Min(value = 0)
    @Column(name = "demanded_amount", nullable = false)
    private Integer demandedAmount;

    @Column(name = "finish_amount")
    private Integer finishAmount;

    @Column(name = "remain_amount")
    private Integer remainAmount;

    @Column(name = "available_amount")
    private Integer availableAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "ship_methond")
    private ShipMethod shipMethond;

    @Column(name = "ship_number")
    private String shipNumber;

    @Column(name = "end_time")
    private ZonedDateTime endTime;

    @Column(name = "create_time")
    private ZonedDateTime createTime;

    @Column(name = "update_time")
    private ZonedDateTime updateTime;

    @NotNull
    @Column(name = "license_plate_number", nullable = false)
    private String licensePlateNumber;

    @Column(name = "driver")
    private String driver;

    @Column(name = "phone")
    private String phone;

    @ManyToOne
    @JsonIgnoreProperties("shipPlans")
    private User user;

    @ManyToOne
    @JsonIgnoreProperties("shipPlans")
    private User toUser;

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

    public Integer getDemandedAmount() {
        return demandedAmount;
    }

    public ShipPlan demandedAmount(Integer demandedAmount) {
        this.demandedAmount = demandedAmount;
        return this;
    }

    public void setDemandedAmount(Integer demandedAmount) {
        this.demandedAmount = demandedAmount;
    }

    public Integer getFinishAmount() {
        return finishAmount;
    }

    public ShipPlan finishAmount(Integer finishAmount) {
        this.finishAmount = finishAmount;
        return this;
    }

    public void setFinishAmount(Integer finishAmount) {
        this.finishAmount = finishAmount;
    }

    public Integer getRemainAmount() {
        return remainAmount;
    }

    public ShipPlan remainAmount(Integer remainAmount) {
        this.remainAmount = remainAmount;
        return this;
    }

    public void setRemainAmount(Integer remainAmount) {
        this.remainAmount = remainAmount;
    }

    public Integer getAvailableAmount() {
        return availableAmount;
    }

    public ShipPlan availableAmount(Integer availableAmount) {
        this.availableAmount = availableAmount;
        return this;
    }

    public void setAvailableAmount(Integer availableAmount) {
        this.availableAmount = availableAmount;
    }

    public ShipMethod getShipMethond() {
        return shipMethond;
    }

    public ShipPlan shipMethond(ShipMethod shipMethond) {
        this.shipMethond = shipMethond;
        return this;
    }

    public void setShipMethond(ShipMethod shipMethond) {
        this.shipMethond = shipMethond;
    }

    public String getShipNumber() {
        return shipNumber;
    }

    public ShipPlan shipNumber(String shipNumber) {
        this.shipNumber = shipNumber;
        return this;
    }

    public void setShipNumber(String shipNumber) {
        this.shipNumber = shipNumber;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public ShipPlan endTime(ZonedDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
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

    public String getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public ShipPlan licensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
        return this;
    }

    public void setLicensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
    }

    public String getDriver() {
        return driver;
    }

    public ShipPlan driver(String driver) {
        this.driver = driver;
        return this;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPhone() {
        return phone;
    }

    public ShipPlan phone(String phone) {
        this.phone = phone;
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public User getToUser() {
        return toUser;
    }

    public ShipPlan toUser(User user) {
        this.toUser = user;
        return this;
    }

    public void setToUser(User user) {
        this.toUser = user;
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
            ", demandedAmount=" + getDemandedAmount() +
            ", finishAmount=" + getFinishAmount() +
            ", remainAmount=" + getRemainAmount() +
            ", availableAmount=" + getAvailableAmount() +
            ", shipMethond='" + getShipMethond() + "'" +
            ", shipNumber='" + getShipNumber() + "'" +
            ", endTime='" + getEndTime() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            ", licensePlateNumber='" + getLicensePlateNumber() + "'" +
            ", driver='" + getDriver() + "'" +
            ", phone='" + getPhone() + "'" +
            "}";
    }
}
