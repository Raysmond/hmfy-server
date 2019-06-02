package com.shield.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import com.shield.domain.enumeration.ShipMethod;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.ZonedDateTimeFilter;

/**
 * Criteria class for the {@link com.shield.domain.ShipPlan} entity. This class is used
 * in {@link com.shield.web.rest.ShipPlanResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /ship-plans?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ShipPlanCriteria implements Serializable, Criteria {
    /**
     * Class for filtering ShipMethod
     */
    public static class ShipMethodFilter extends Filter<ShipMethod> {

        public ShipMethodFilter() {
        }

        public ShipMethodFilter(ShipMethodFilter filter) {
            super(filter);
        }

        @Override
        public ShipMethodFilter copy() {
            return new ShipMethodFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter company;

    private IntegerFilter demandedAmount;

    private IntegerFilter finishAmount;

    private IntegerFilter remainAmount;

    private IntegerFilter availableAmount;

    private ShipMethodFilter shipMethond;

    private StringFilter shipNumber;

    private ZonedDateTimeFilter endTime;

    private ZonedDateTimeFilter createTime;

    private ZonedDateTimeFilter updateTime;

    private StringFilter licensePlateNumber;

    private StringFilter driver;

    private StringFilter phone;

    private LongFilter userId;

    private LongFilter toUserId;

    public ShipPlanCriteria(){
    }

    public ShipPlanCriteria(ShipPlanCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.company = other.company == null ? null : other.company.copy();
        this.demandedAmount = other.demandedAmount == null ? null : other.demandedAmount.copy();
        this.finishAmount = other.finishAmount == null ? null : other.finishAmount.copy();
        this.remainAmount = other.remainAmount == null ? null : other.remainAmount.copy();
        this.availableAmount = other.availableAmount == null ? null : other.availableAmount.copy();
        this.shipMethond = other.shipMethond == null ? null : other.shipMethond.copy();
        this.shipNumber = other.shipNumber == null ? null : other.shipNumber.copy();
        this.endTime = other.endTime == null ? null : other.endTime.copy();
        this.createTime = other.createTime == null ? null : other.createTime.copy();
        this.updateTime = other.updateTime == null ? null : other.updateTime.copy();
        this.licensePlateNumber = other.licensePlateNumber == null ? null : other.licensePlateNumber.copy();
        this.driver = other.driver == null ? null : other.driver.copy();
        this.phone = other.phone == null ? null : other.phone.copy();
        this.userId = other.userId == null ? null : other.userId.copy();
        this.toUserId = other.toUserId == null ? null : other.toUserId.copy();
    }

    @Override
    public ShipPlanCriteria copy() {
        return new ShipPlanCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getCompany() {
        return company;
    }

    public void setCompany(StringFilter company) {
        this.company = company;
    }

    public IntegerFilter getDemandedAmount() {
        return demandedAmount;
    }

    public void setDemandedAmount(IntegerFilter demandedAmount) {
        this.demandedAmount = demandedAmount;
    }

    public IntegerFilter getFinishAmount() {
        return finishAmount;
    }

    public void setFinishAmount(IntegerFilter finishAmount) {
        this.finishAmount = finishAmount;
    }

    public IntegerFilter getRemainAmount() {
        return remainAmount;
    }

    public void setRemainAmount(IntegerFilter remainAmount) {
        this.remainAmount = remainAmount;
    }

    public IntegerFilter getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(IntegerFilter availableAmount) {
        this.availableAmount = availableAmount;
    }

    public ShipMethodFilter getShipMethond() {
        return shipMethond;
    }

    public void setShipMethond(ShipMethodFilter shipMethond) {
        this.shipMethond = shipMethond;
    }

    public StringFilter getShipNumber() {
        return shipNumber;
    }

    public void setShipNumber(StringFilter shipNumber) {
        this.shipNumber = shipNumber;
    }

    public ZonedDateTimeFilter getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTimeFilter endTime) {
        this.endTime = endTime;
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

    public StringFilter getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public void setLicensePlateNumber(StringFilter licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
    }

    public StringFilter getDriver() {
        return driver;
    }

    public void setDriver(StringFilter driver) {
        this.driver = driver;
    }

    public StringFilter getPhone() {
        return phone;
    }

    public void setPhone(StringFilter phone) {
        this.phone = phone;
    }

    public LongFilter getUserId() {
        return userId;
    }

    public void setUserId(LongFilter userId) {
        this.userId = userId;
    }

    public LongFilter getToUserId() {
        return toUserId;
    }

    public void setToUserId(LongFilter toUserId) {
        this.toUserId = toUserId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ShipPlanCriteria that = (ShipPlanCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(company, that.company) &&
            Objects.equals(demandedAmount, that.demandedAmount) &&
            Objects.equals(finishAmount, that.finishAmount) &&
            Objects.equals(remainAmount, that.remainAmount) &&
            Objects.equals(availableAmount, that.availableAmount) &&
            Objects.equals(shipMethond, that.shipMethond) &&
            Objects.equals(shipNumber, that.shipNumber) &&
            Objects.equals(endTime, that.endTime) &&
            Objects.equals(createTime, that.createTime) &&
            Objects.equals(updateTime, that.updateTime) &&
            Objects.equals(licensePlateNumber, that.licensePlateNumber) &&
            Objects.equals(driver, that.driver) &&
            Objects.equals(phone, that.phone) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(toUserId, that.toUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        company,
        demandedAmount,
        finishAmount,
        remainAmount,
        availableAmount,
        shipMethond,
        shipNumber,
        endTime,
        createTime,
        updateTime,
        licensePlateNumber,
        driver,
        phone,
        userId,
        toUserId
        );
    }

    @Override
    public String toString() {
        return "ShipPlanCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (company != null ? "company=" + company + ", " : "") +
                (demandedAmount != null ? "demandedAmount=" + demandedAmount + ", " : "") +
                (finishAmount != null ? "finishAmount=" + finishAmount + ", " : "") +
                (remainAmount != null ? "remainAmount=" + remainAmount + ", " : "") +
                (availableAmount != null ? "availableAmount=" + availableAmount + ", " : "") +
                (shipMethond != null ? "shipMethond=" + shipMethond + ", " : "") +
                (shipNumber != null ? "shipNumber=" + shipNumber + ", " : "") +
                (endTime != null ? "endTime=" + endTime + ", " : "") +
                (createTime != null ? "createTime=" + createTime + ", " : "") +
                (updateTime != null ? "updateTime=" + updateTime + ", " : "") +
                (licensePlateNumber != null ? "licensePlateNumber=" + licensePlateNumber + ", " : "") +
                (driver != null ? "driver=" + driver + ", " : "") +
                (phone != null ? "phone=" + phone + ", " : "") +
                (userId != null ? "userId=" + userId + ", " : "") +
                (toUserId != null ? "toUserId=" + toUserId + ", " : "") +
            "}";
    }

}
