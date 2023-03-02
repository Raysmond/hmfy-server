package com.shield.service.dto;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.shield.domain.Plan} entity.
 */
public class PlanDTO implements Serializable {

    private Long id;

    @NotNull
    private String planNumber;

    private String location;

    @NotNull
    private LocalDate workDay;

    private String stockName;

    private ZonedDateTime loadingStartTime;

    private ZonedDateTime loadingEndTime;

    @NotNull
    private Double weightSum;

    private String operator;

    private String operation;

    private String opPosition;

    private String channel;

    private String comment;

    private ZonedDateTime createTime;

    private ZonedDateTime updateTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlanNumber() {
        return planNumber;
    }

    public void setPlanNumber(String planNumber) {
        this.planNumber = planNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getWorkDay() {
        return workDay;
    }

    public void setWorkDay(LocalDate workDay) {
        this.workDay = workDay;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
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

    public Double getWeightSum() {
        return weightSum;
    }

    public void setWeightSum(Double weightSum) {
        this.weightSum = weightSum;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOpPosition() {
        return opPosition;
    }

    public void setOpPosition(String opPosition) {
        this.opPosition = opPosition;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlanDTO planDTO = (PlanDTO) o;
        if (planDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), planDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PlanDTO{" +
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
