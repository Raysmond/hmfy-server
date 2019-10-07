package com.shield.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import com.shield.domain.enumeration.RecordType;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.ZonedDateTimeFilter;

/**
 * Criteria class for the {@link com.shield.domain.GateRecord} entity. This class is used
 * in {@link com.shield.web.rest.GateRecordResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /gate-records?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class GateRecordCriteria implements Serializable, Criteria {
    /**
     * Class for filtering RecordType
     */
    public static class RecordTypeFilter extends Filter<RecordType> {

        public RecordTypeFilter() {
        }

        public RecordTypeFilter(RecordTypeFilter filter) {
            super(filter);
        }

        @Override
        public RecordTypeFilter copy() {
            return new RecordTypeFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private RecordTypeFilter recordType;

    private StringFilter truckNumber;

    private ZonedDateTimeFilter recordTime;

    private StringFilter rid;

    private ZonedDateTimeFilter createTime;

    private LongFilter regionId;

    private StringFilter dataMd5;

    public GateRecordCriteria(){
    }

    public GateRecordCriteria(GateRecordCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.recordType = other.recordType == null ? null : other.recordType.copy();
        this.truckNumber = other.truckNumber == null ? null : other.truckNumber.copy();
        this.recordTime = other.recordTime == null ? null : other.recordTime.copy();
        this.rid = other.rid == null ? null : other.rid.copy();
        this.createTime = other.createTime == null ? null : other.createTime.copy();
        this.regionId = other.regionId == null ? null : other.regionId.copy();
        this.dataMd5 = other.dataMd5 == null ? null : other.dataMd5.copy();
    }

    @Override
    public GateRecordCriteria copy() {
        return new GateRecordCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public RecordTypeFilter getRecordType() {
        return recordType;
    }

    public void setRecordType(RecordTypeFilter recordType) {
        this.recordType = recordType;
    }

    public StringFilter getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(StringFilter truckNumber) {
        this.truckNumber = truckNumber;
    }

    public ZonedDateTimeFilter getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(ZonedDateTimeFilter recordTime) {
        this.recordTime = recordTime;
    }

    public StringFilter getRid() {
        return rid;
    }

    public void setRid(StringFilter rid) {
        this.rid = rid;
    }

    public ZonedDateTimeFilter getCreateTime() {
        return createTime;
    }

    public void setCreateTime(ZonedDateTimeFilter createTime) {
        this.createTime = createTime;
    }

    public LongFilter getRegionId() {
        return regionId;
    }

    public void setRegionId(LongFilter regionId) {
        this.regionId = regionId;
    }

    public StringFilter getDataMd5() {
        return dataMd5;
    }

    public void setDataMd5(StringFilter dataMd5) {
        this.dataMd5 = dataMd5;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final GateRecordCriteria that = (GateRecordCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(recordType, that.recordType) &&
            Objects.equals(truckNumber, that.truckNumber) &&
            Objects.equals(recordTime, that.recordTime) &&
            Objects.equals(rid, that.rid) &&
            Objects.equals(createTime, that.createTime) &&
            Objects.equals(regionId, that.regionId) &&
            Objects.equals(dataMd5, that.dataMd5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        recordType,
        truckNumber,
        recordTime,
        rid,
        createTime,
        regionId,
        dataMd5
        );
    }

    @Override
    public String toString() {
        return "GateRecordCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (recordType != null ? "recordType=" + recordType + ", " : "") +
                (truckNumber != null ? "truckNumber=" + truckNumber + ", " : "") +
                (recordTime != null ? "recordTime=" + recordTime + ", " : "") +
                (rid != null ? "rid=" + rid + ", " : "") +
                (createTime != null ? "createTime=" + createTime + ", " : "") +
                (regionId != null ? "regionId=" + regionId + ", " : "") +
                (dataMd5 != null ? "dataMd5=" + dataMd5 + ", " : "") +
            "}";
    }

}
