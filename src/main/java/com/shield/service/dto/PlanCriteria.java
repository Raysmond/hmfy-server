package com.shield.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.LocalDateFilter;
import io.github.jhipster.service.filter.ZonedDateTimeFilter;

/**
 * Criteria class for the {@link com.shield.domain.Plan} entity. This class is used
 * in {@link com.shield.web.rest.PlanResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /plans?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class PlanCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter planNumber;

    private StringFilter location;

    private LocalDateFilter workDay;

    private StringFilter stockName;

    private ZonedDateTimeFilter loadingStartTime;

    private ZonedDateTimeFilter loadingEndTime;

    private DoubleFilter weightSum;

    private StringFilter operator;

    private StringFilter operation;

    private StringFilter opPosition;

    private StringFilter channel;

    private StringFilter comment;

    private ZonedDateTimeFilter createTime;

    private ZonedDateTimeFilter updateTime;

    public PlanCriteria(){
    }

    public PlanCriteria(PlanCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.planNumber = other.planNumber == null ? null : other.planNumber.copy();
        this.location = other.location == null ? null : other.location.copy();
        this.workDay = other.workDay == null ? null : other.workDay.copy();
        this.stockName = other.stockName == null ? null : other.stockName.copy();
        this.loadingStartTime = other.loadingStartTime == null ? null : other.loadingStartTime.copy();
        this.loadingEndTime = other.loadingEndTime == null ? null : other.loadingEndTime.copy();
        this.weightSum = other.weightSum == null ? null : other.weightSum.copy();
        this.operator = other.operator == null ? null : other.operator.copy();
        this.operation = other.operation == null ? null : other.operation.copy();
        this.opPosition = other.opPosition == null ? null : other.opPosition.copy();
        this.channel = other.channel == null ? null : other.channel.copy();
        this.comment = other.comment == null ? null : other.comment.copy();
        this.createTime = other.createTime == null ? null : other.createTime.copy();
        this.updateTime = other.updateTime == null ? null : other.updateTime.copy();
    }

    @Override
    public PlanCriteria copy() {
        return new PlanCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getPlanNumber() {
        return planNumber;
    }

    public void setPlanNumber(StringFilter planNumber) {
        this.planNumber = planNumber;
    }

    public StringFilter getLocation() {
        return location;
    }

    public void setLocation(StringFilter location) {
        this.location = location;
    }

    public LocalDateFilter getWorkDay() {
        return workDay;
    }

    public void setWorkDay(LocalDateFilter workDay) {
        this.workDay = workDay;
    }

    public StringFilter getStockName() {
        return stockName;
    }

    public void setStockName(StringFilter stockName) {
        this.stockName = stockName;
    }

    public ZonedDateTimeFilter getLoadingStartTime() {
        return loadingStartTime;
    }

    public void setLoadingStartTime(ZonedDateTimeFilter loadingStartTime) {
        this.loadingStartTime = loadingStartTime;
    }

    public ZonedDateTimeFilter getLoadingEndTime() {
        return loadingEndTime;
    }

    public void setLoadingEndTime(ZonedDateTimeFilter loadingEndTime) {
        this.loadingEndTime = loadingEndTime;
    }

    public DoubleFilter getWeightSum() {
        return weightSum;
    }

    public void setWeightSum(DoubleFilter weightSum) {
        this.weightSum = weightSum;
    }

    public StringFilter getOperator() {
        return operator;
    }

    public void setOperator(StringFilter operator) {
        this.operator = operator;
    }

    public StringFilter getOperation() {
        return operation;
    }

    public void setOperation(StringFilter operation) {
        this.operation = operation;
    }

    public StringFilter getOpPosition() {
        return opPosition;
    }

    public void setOpPosition(StringFilter opPosition) {
        this.opPosition = opPosition;
    }

    public StringFilter getChannel() {
        return channel;
    }

    public void setChannel(StringFilter channel) {
        this.channel = channel;
    }

    public StringFilter getComment() {
        return comment;
    }

    public void setComment(StringFilter comment) {
        this.comment = comment;
    }

    public ZonedDateTimeFilter getCreateTime() {
        return createTime;
    }

    public void setCreateTime(ZonedDateTimeFilter createTime) {
        this.createTime = createTime;
    }

    public ZonedDateTimeFilter getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(ZonedDateTimeFilter updateTime) {
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
        final PlanCriteria that = (PlanCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(planNumber, that.planNumber) &&
            Objects.equals(location, that.location) &&
            Objects.equals(workDay, that.workDay) &&
            Objects.equals(stockName, that.stockName) &&
            Objects.equals(loadingStartTime, that.loadingStartTime) &&
            Objects.equals(loadingEndTime, that.loadingEndTime) &&
            Objects.equals(weightSum, that.weightSum) &&
            Objects.equals(operator, that.operator) &&
            Objects.equals(operation, that.operation) &&
            Objects.equals(opPosition, that.opPosition) &&
            Objects.equals(channel, that.channel) &&
            Objects.equals(comment, that.comment) &&
            Objects.equals(createTime, that.createTime) &&
            Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        planNumber,
        location,
        workDay,
        stockName,
        loadingStartTime,
        loadingEndTime,
        weightSum,
        operator,
        operation,
        opPosition,
        channel,
        comment,
        createTime,
        updateTime
        );
    }

    @Override
    public String toString() {
        return "PlanCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (planNumber != null ? "planNumber=" + planNumber + ", " : "") +
                (location != null ? "location=" + location + ", " : "") +
                (workDay != null ? "workDay=" + workDay + ", " : "") +
                (stockName != null ? "stockName=" + stockName + ", " : "") +
                (loadingStartTime != null ? "loadingStartTime=" + loadingStartTime + ", " : "") +
                (loadingEndTime != null ? "loadingEndTime=" + loadingEndTime + ", " : "") +
                (weightSum != null ? "weightSum=" + weightSum + ", " : "") +
                (operator != null ? "operator=" + operator + ", " : "") +
                (operation != null ? "operation=" + operation + ", " : "") +
                (opPosition != null ? "opPosition=" + opPosition + ", " : "") +
                (channel != null ? "channel=" + channel + ", " : "") +
                (comment != null ? "comment=" + comment + ", " : "") +
                (createTime != null ? "createTime=" + createTime + ", " : "") +
                (updateTime != null ? "updateTime=" + updateTime + ", " : "") +
            "}";
    }

}
