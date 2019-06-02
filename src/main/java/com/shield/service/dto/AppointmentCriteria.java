package com.shield.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import com.shield.domain.enumeration.AppointmentStatus;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.ZonedDateTimeFilter;

/**
 * Criteria class for the {@link com.shield.domain.Appointment} entity. This class is used
 * in {@link com.shield.web.rest.AppointmentResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /appointments?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class AppointmentCriteria implements Serializable, Criteria {
    /**
     * Class for filtering AppointmentStatus
     */
    public static class AppointmentStatusFilter extends Filter<AppointmentStatus> {

        public AppointmentStatusFilter() {
        }

        public AppointmentStatusFilter(AppointmentStatusFilter filter) {
            super(filter);
        }

        @Override
        public AppointmentStatusFilter copy() {
            return new AppointmentStatusFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter licensePlateNumber;

    private StringFilter driver;

    private StringFilter phone;

    private IntegerFilter number;

    private BooleanFilter valid;

    private AppointmentStatusFilter status;

    private IntegerFilter queueNumber;

    private BooleanFilter vip;

    private ZonedDateTimeFilter createTime;

    private ZonedDateTimeFilter updateTime;

    private ZonedDateTimeFilter startTime;

    private ZonedDateTimeFilter enterTime;

    private ZonedDateTimeFilter leaveTime;

    private ZonedDateTimeFilter expireTime;

    private LongFilter regionId;

    private LongFilter userId;

    public AppointmentCriteria(){
    }

    public AppointmentCriteria(AppointmentCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.licensePlateNumber = other.licensePlateNumber == null ? null : other.licensePlateNumber.copy();
        this.driver = other.driver == null ? null : other.driver.copy();
        this.phone = other.phone == null ? null : other.phone.copy();
        this.number = other.number == null ? null : other.number.copy();
        this.valid = other.valid == null ? null : other.valid.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.queueNumber = other.queueNumber == null ? null : other.queueNumber.copy();
        this.vip = other.vip == null ? null : other.vip.copy();
        this.createTime = other.createTime == null ? null : other.createTime.copy();
        this.updateTime = other.updateTime == null ? null : other.updateTime.copy();
        this.startTime = other.startTime == null ? null : other.startTime.copy();
        this.enterTime = other.enterTime == null ? null : other.enterTime.copy();
        this.leaveTime = other.leaveTime == null ? null : other.leaveTime.copy();
        this.expireTime = other.expireTime == null ? null : other.expireTime.copy();
        this.regionId = other.regionId == null ? null : other.regionId.copy();
        this.userId = other.userId == null ? null : other.userId.copy();
    }

    @Override
    public AppointmentCriteria copy() {
        return new AppointmentCriteria(this);
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

    public StringFilter getPhone() {
        return phone;
    }

    public void setPhone(StringFilter phone) {
        this.phone = phone;
    }

    public IntegerFilter getNumber() {
        return number;
    }

    public void setNumber(IntegerFilter number) {
        this.number = number;
    }

    public BooleanFilter getValid() {
        return valid;
    }

    public void setValid(BooleanFilter valid) {
        this.valid = valid;
    }

    public AppointmentStatusFilter getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatusFilter status) {
        this.status = status;
    }

    public IntegerFilter getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(IntegerFilter queueNumber) {
        this.queueNumber = queueNumber;
    }

    public BooleanFilter getVip() {
        return vip;
    }

    public void setVip(BooleanFilter vip) {
        this.vip = vip;
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

    public ZonedDateTimeFilter getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTimeFilter startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTimeFilter getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(ZonedDateTimeFilter enterTime) {
        this.enterTime = enterTime;
    }

    public ZonedDateTimeFilter getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(ZonedDateTimeFilter leaveTime) {
        this.leaveTime = leaveTime;
    }

    public ZonedDateTimeFilter getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(ZonedDateTimeFilter expireTime) {
        this.expireTime = expireTime;
    }

    public LongFilter getRegionId() {
        return regionId;
    }

    public void setRegionId(LongFilter regionId) {
        this.regionId = regionId;
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
        final AppointmentCriteria that = (AppointmentCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(licensePlateNumber, that.licensePlateNumber) &&
            Objects.equals(driver, that.driver) &&
            Objects.equals(phone, that.phone) &&
            Objects.equals(number, that.number) &&
            Objects.equals(valid, that.valid) &&
            Objects.equals(status, that.status) &&
            Objects.equals(queueNumber, that.queueNumber) &&
            Objects.equals(vip, that.vip) &&
            Objects.equals(createTime, that.createTime) &&
            Objects.equals(updateTime, that.updateTime) &&
            Objects.equals(startTime, that.startTime) &&
            Objects.equals(enterTime, that.enterTime) &&
            Objects.equals(leaveTime, that.leaveTime) &&
            Objects.equals(expireTime, that.expireTime) &&
            Objects.equals(regionId, that.regionId) &&
            Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        licensePlateNumber,
        driver,
        phone,
        number,
        valid,
        status,
        queueNumber,
        vip,
        createTime,
        updateTime,
        startTime,
        enterTime,
        leaveTime,
        expireTime,
        regionId,
        userId
        );
    }

    @Override
    public String toString() {
        return "AppointmentCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (licensePlateNumber != null ? "licensePlateNumber=" + licensePlateNumber + ", " : "") +
                (driver != null ? "driver=" + driver + ", " : "") +
                (phone != null ? "phone=" + phone + ", " : "") +
                (number != null ? "number=" + number + ", " : "") +
                (valid != null ? "valid=" + valid + ", " : "") +
                (status != null ? "status=" + status + ", " : "") +
                (queueNumber != null ? "queueNumber=" + queueNumber + ", " : "") +
                (vip != null ? "vip=" + vip + ", " : "") +
                (createTime != null ? "createTime=" + createTime + ", " : "") +
                (updateTime != null ? "updateTime=" + updateTime + ", " : "") +
                (startTime != null ? "startTime=" + startTime + ", " : "") +
                (enterTime != null ? "enterTime=" + enterTime + ", " : "") +
                (leaveTime != null ? "leaveTime=" + leaveTime + ", " : "") +
                (expireTime != null ? "expireTime=" + expireTime + ", " : "") +
                (regionId != null ? "regionId=" + regionId + ", " : "") +
                (userId != null ? "userId=" + userId + ", " : "") +
            "}";
    }

}
