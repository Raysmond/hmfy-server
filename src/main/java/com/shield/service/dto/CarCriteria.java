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
import io.github.jhipster.service.filter.ZonedDateTimeFilter;

/**
 * Criteria class for the {@link com.shield.domain.Car} entity. This class is used
 * in {@link com.shield.web.rest.CarResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /cars?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CarCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter licensePlateNumber;

    private StringFilter driver;

    private ZonedDateTimeFilter createTime;

    private ZonedDateTimeFilter updateTime;

    private LongFilter userId;

    public CarCriteria(){
    }

    public CarCriteria(CarCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.licensePlateNumber = other.licensePlateNumber == null ? null : other.licensePlateNumber.copy();
        this.driver = other.driver == null ? null : other.driver.copy();
        this.createTime = other.createTime == null ? null : other.createTime.copy();
        this.updateTime = other.updateTime == null ? null : other.updateTime.copy();
        this.userId = other.userId == null ? null : other.userId.copy();
    }

    @Override
    public CarCriteria copy() {
        return new CarCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
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

    public LongFilter getUserId() {
        return userId;
    }

    public void setUserId(LongFilter userId) {
        this.userId = userId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CarCriteria that = (CarCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(licensePlateNumber, that.licensePlateNumber) &&
            Objects.equals(driver, that.driver) &&
            Objects.equals(createTime, that.createTime) &&
            Objects.equals(updateTime, that.updateTime) &&
            Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        licensePlateNumber,
        driver,
        createTime,
        updateTime,
        userId
        );
    }

    @Override
    public String toString() {
        return "CarCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (licensePlateNumber != null ? "licensePlateNumber=" + licensePlateNumber + ", " : "") +
                (driver != null ? "driver=" + driver + ", " : "") +
                (createTime != null ? "createTime=" + createTime + ", " : "") +
                (updateTime != null ? "updateTime=" + updateTime + ", " : "") +
                (userId != null ? "userId=" + userId + ", " : "") +
            "}";
    }

}
