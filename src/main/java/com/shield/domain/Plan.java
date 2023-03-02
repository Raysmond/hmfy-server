package com.shield.domain;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * A Plan.
 */
@Entity
@Table(name = "plan")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Plan implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "plan_number", nullable = false)
    private String planNumber;

    @Column(name = "location")
    private String location;

    @NotNull
    @Column(name = "work_day", nullable = false)
    private LocalDate workDay;

    @Column(name = "stock_name")
    private String stockName;

    @Column(name = "loading_start_time")
    private ZonedDateTime loadingStartTime;

    @Column(name = "loading_end_time")
    private ZonedDateTime loadingEndTime;

    @NotNull
    @Column(name = "weight_sum", nullable = false)
    private Double weightSum;

    @Column(name = "operator")
    private String operator;

    @Column(name = "operation")
    private String operation;

    @Column(name = "op_position")
    private String opPosition;

    @Column(name = "channel")
    private String channel;

    @Column(name = "comment")
    private String comment;

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

    public String getPlanNumber() {
        return planNumber;
    }

    public Plan planNumber(String planNumber) {
        this.planNumber = planNumber;
        return this;
    }

    public void setPlanNumber(String planNumber) {
        this.planNumber = planNumber;
    }

    public String getLocation() {
        return location;
    }

    public Plan location(String location) {
        this.location = location;
        return this;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getWorkDay() {
        return workDay;
    }

    public Plan workDay(LocalDate workDay) {
        this.workDay = workDay;
        return this;
    }

    public void setWorkDay(LocalDate workDay) {
        this.workDay = workDay;
    }

    public String getStockName() {
        return stockName;
    }

    public Plan stockName(String stockName) {
        this.stockName = stockName;
        return this;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public ZonedDateTime getLoadingStartTime() {
        return loadingStartTime;
    }

    public Plan loadingStartTime(ZonedDateTime loadingStartTime) {
        this.loadingStartTime = loadingStartTime;
        return this;
    }

    public void setLoadingStartTime(ZonedDateTime loadingStartTime) {
        this.loadingStartTime = loadingStartTime;
    }

    public ZonedDateTime getLoadingEndTime() {
        return loadingEndTime;
    }

    public Plan loadingEndTime(ZonedDateTime loadingEndTime) {
        this.loadingEndTime = loadingEndTime;
        return this;
    }

    public void setLoadingEndTime(ZonedDateTime loadingEndTime) {
        this.loadingEndTime = loadingEndTime;
    }

    public Double getWeightSum() {
        return weightSum;
    }

    public Plan weightSum(Double weightSum) {
        this.weightSum = weightSum;
        return this;
    }

    public void setWeightSum(Double weightSum) {
        this.weightSum = weightSum;
    }

    public String getOperator() {
        return operator;
    }

    public Plan operator(String operator) {
        this.operator = operator;
        return this;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperation() {
        return operation;
    }

    public Plan operation(String operation) {
        this.operation = operation;
        return this;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOpPosition() {
        return opPosition;
    }

    public Plan opPosition(String opPosition) {
        this.opPosition = opPosition;
        return this;
    }

    public void setOpPosition(String opPosition) {
        this.opPosition = opPosition;
    }

    public String getChannel() {
        return channel;
    }

    public Plan channel(String channel) {
        this.channel = channel;
        return this;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getComment() {
        return comment;
    }

    public Plan comment(String comment) {
        this.comment = comment;
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public Plan createTime(ZonedDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public ZonedDateTime getUpdateTime() {
        return updateTime;
    }

    public Plan updateTime(ZonedDateTime updateTime) {
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
        if (!(o instanceof Plan)) {
            return false;
        }
        return id != null && id.equals(((Plan) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Plan{" +
            "id=" + getId() +
            ", planNumber='" + getPlanNumber() + "'" +
            ", location='" + getLocation() + "'" +
            ", workDay='" + getWorkDay() + "'" +
            ", stockName='" + getStockName() + "'" +
            ", loadingStartTime='" + getLoadingStartTime() + "'" +
            ", loadingEndTime='" + getLoadingEndTime() + "'" +
            ", weightSum=" + getWeightSum() +
            ", operator='" + getOperator() + "'" +
            ", operation='" + getOperation() + "'" +
            ", opPosition='" + getOpPosition() + "'" +
            ", channel='" + getChannel() + "'" +
            ", comment='" + getComment() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            "}";
    }
}
