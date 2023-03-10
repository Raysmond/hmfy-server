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
 * Criteria class for the {@link com.shield.domain.ShipPlan} entity. This class is used
 * in {@link com.shield.web.rest.ShipPlanResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /ship-plans?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ShipPlanCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter company;

    private LongFilter applyId;

    private StringFilter applyNumber;

    private StringFilter truckNumber;

    private IntegerFilter auditStatus;

    private StringFilter productName;

    private StringFilter deliverPosition;

    private BooleanFilter valid;

    private ZonedDateTimeFilter gateTime;

    private ZonedDateTimeFilter leaveTime;

    private ZonedDateTimeFilter deliverTime;

    private ZonedDateTimeFilter allowInTime;

    private ZonedDateTimeFilter loadingStartTime;

    private ZonedDateTimeFilter loadingEndTime;

    private ZonedDateTimeFilter createTime;

    private ZonedDateTimeFilter updateTime;

    private ZonedDateTimeFilter syncTime;

    private BooleanFilter tareAlert;

    private BooleanFilter leaveAlert;

    private DoubleFilter netWeight;

    private StringFilter weigherNo;

    private BooleanFilter vip;

    private LongFilter userId;

    public ShipPlanCriteria(){
    }

    public ShipPlanCriteria(ShipPlanCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.company = other.company == null ? null : other.company.copy();
        this.applyId = other.applyId == null ? null : other.applyId.copy();
        this.applyNumber = other.applyNumber == null ? null : other.applyNumber.copy();
        this.truckNumber = other.truckNumber == null ? null : other.truckNumber.copy();
        this.auditStatus = other.auditStatus == null ? null : other.auditStatus.copy();
        this.productName = other.productName == null ? null : other.productName.copy();
        this.deliverPosition = other.deliverPosition == null ? null : other.deliverPosition.copy();
        this.valid = other.valid == null ? null : other.valid.copy();
        this.gateTime = other.gateTime == null ? null : other.gateTime.copy();
        this.leaveTime = other.leaveTime == null ? null : other.leaveTime.copy();
        this.deliverTime = other.deliverTime == null ? null : other.deliverTime.copy();
        this.allowInTime = other.allowInTime == null ? null : other.allowInTime.copy();
        this.loadingStartTime = other.loadingStartTime == null ? null : other.loadingStartTime.copy();
        this.loadingEndTime = other.loadingEndTime == null ? null : other.loadingEndTime.copy();
        this.createTime = other.createTime == null ? null : other.createTime.copy();
        this.updateTime = other.updateTime == null ? null : other.updateTime.copy();
        this.syncTime = other.syncTime == null ? null : other.syncTime.copy();
        this.tareAlert = other.tareAlert == null ? null : other.tareAlert.copy();
        this.leaveAlert = other.leaveAlert == null ? null : other.leaveAlert.copy();
        this.netWeight = other.netWeight == null ? null : other.netWeight.copy();
        this.weigherNo = other.weigherNo == null ? null : other.weigherNo.copy();
        this.vip = other.vip == null ? null : other.vip.copy();
        this.userId = other.userId == null ? null : other.userId.copy();
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

    public LongFilter getApplyId() {
        return applyId;
    }

    public void setApplyId(LongFilter applyId) {
        this.applyId = applyId;
    }

    public StringFilter getApplyNumber() {
        return applyNumber;
    }

    public void setApplyNumber(StringFilter applyNumber) {
        this.applyNumber = applyNumber;
    }

    public StringFilter getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(StringFilter truckNumber) {
        this.truckNumber = truckNumber;
    }

    public IntegerFilter getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(IntegerFilter auditStatus) {
        this.auditStatus = auditStatus;
    }

    public StringFilter getProductName() {
        return productName;
    }

    public void setProductName(StringFilter productName) {
        this.productName = productName;
    }

    public StringFilter getDeliverPosition() {
        return deliverPosition;
    }

    public void setDeliverPosition(StringFilter deliverPosition) {
        this.deliverPosition = deliverPosition;
    }

    public BooleanFilter getValid() {
        return valid;
    }

    public void setValid(BooleanFilter valid) {
        this.valid = valid;
    }

    public ZonedDateTimeFilter getGateTime() {
        return gateTime;
    }

    public void setGateTime(ZonedDateTimeFilter gateTime) {
        this.gateTime = gateTime;
    }

    public ZonedDateTimeFilter getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(ZonedDateTimeFilter leaveTime) {
        this.leaveTime = leaveTime;
    }

    public ZonedDateTimeFilter getDeliverTime() {
        return deliverTime;
    }

    public void setDeliverTime(ZonedDateTimeFilter deliverTime) {
        this.deliverTime = deliverTime;
    }

    public ZonedDateTimeFilter getAllowInTime() {
        return allowInTime;
    }

    public void setAllowInTime(ZonedDateTimeFilter allowInTime) {
        this.allowInTime = allowInTime;
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

    public ZonedDateTimeFilter getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(ZonedDateTimeFilter syncTime) {
        this.syncTime = syncTime;
    }

    public BooleanFilter getTareAlert() {
        return tareAlert;
    }

    public void setTareAlert(BooleanFilter tareAlert) {
        this.tareAlert = tareAlert;
    }

    public BooleanFilter getLeaveAlert() {
        return leaveAlert;
    }

    public void setLeaveAlert(BooleanFilter leaveAlert) {
        this.leaveAlert = leaveAlert;
    }

    public DoubleFilter getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(DoubleFilter netWeight) {
        this.netWeight = netWeight;
    }

    public StringFilter getWeigherNo() {
        return weigherNo;
    }

    public void setWeigherNo(StringFilter weigherNo) {
        this.weigherNo = weigherNo;
    }

    public BooleanFilter getVip() {
        return vip;
    }

    public void setVip(BooleanFilter vip) {
        this.vip = vip;
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
        final ShipPlanCriteria that = (ShipPlanCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(company, that.company) &&
            Objects.equals(applyId, that.applyId) &&
            Objects.equals(applyNumber, that.applyNumber) &&
            Objects.equals(truckNumber, that.truckNumber) &&
            Objects.equals(auditStatus, that.auditStatus) &&
            Objects.equals(productName, that.productName) &&
            Objects.equals(deliverPosition, that.deliverPosition) &&
            Objects.equals(valid, that.valid) &&
            Objects.equals(gateTime, that.gateTime) &&
            Objects.equals(leaveTime, that.leaveTime) &&
            Objects.equals(deliverTime, that.deliverTime) &&
            Objects.equals(allowInTime, that.allowInTime) &&
            Objects.equals(loadingStartTime, that.loadingStartTime) &&
            Objects.equals(loadingEndTime, that.loadingEndTime) &&
            Objects.equals(createTime, that.createTime) &&
            Objects.equals(updateTime, that.updateTime) &&
            Objects.equals(syncTime, that.syncTime) &&
            Objects.equals(tareAlert, that.tareAlert) &&
            Objects.equals(leaveAlert, that.leaveAlert) &&
            Objects.equals(netWeight, that.netWeight) &&
            Objects.equals(weigherNo, that.weigherNo) &&
            Objects.equals(vip, that.vip) &&
            Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        company,
        applyId,
        applyNumber,
        truckNumber,
        auditStatus,
        productName,
        deliverPosition,
        valid,
        gateTime,
        leaveTime,
        deliverTime,
        allowInTime,
        loadingStartTime,
        loadingEndTime,
        createTime,
        updateTime,
        syncTime,
        tareAlert,
        leaveAlert,
        netWeight,
        weigherNo,
        vip,
        userId
        );
    }

    @Override
    public String toString() {
        return "ShipPlanCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (company != null ? "company=" + company + ", " : "") +
                (applyId != null ? "applyId=" + applyId + ", " : "") +
                (applyNumber != null ? "applyNumber=" + applyNumber + ", " : "") +
                (truckNumber != null ? "truckNumber=" + truckNumber + ", " : "") +
                (auditStatus != null ? "auditStatus=" + auditStatus + ", " : "") +
                (productName != null ? "productName=" + productName + ", " : "") +
                (deliverPosition != null ? "deliverPosition=" + deliverPosition + ", " : "") +
                (valid != null ? "valid=" + valid + ", " : "") +
                (gateTime != null ? "gateTime=" + gateTime + ", " : "") +
                (leaveTime != null ? "leaveTime=" + leaveTime + ", " : "") +
                (deliverTime != null ? "deliverTime=" + deliverTime + ", " : "") +
                (allowInTime != null ? "allowInTime=" + allowInTime + ", " : "") +
                (loadingStartTime != null ? "loadingStartTime=" + loadingStartTime + ", " : "") +
                (loadingEndTime != null ? "loadingEndTime=" + loadingEndTime + ", " : "") +
                (createTime != null ? "createTime=" + createTime + ", " : "") +
                (updateTime != null ? "updateTime=" + updateTime + ", " : "") +
                (syncTime != null ? "syncTime=" + syncTime + ", " : "") +
                (tareAlert != null ? "tareAlert=" + tareAlert + ", " : "") +
                (leaveAlert != null ? "leaveAlert=" + leaveAlert + ", " : "") +
                (netWeight != null ? "netWeight=" + netWeight + ", " : "") +
                (weigherNo != null ? "weigherNo=" + weigherNo + ", " : "") +
                (vip != null ? "vip=" + vip + ", " : "") +
                (userId != null ? "userId=" + userId + ", " : "") +
            "}";
    }

}
