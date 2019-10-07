package com.shield.domain;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.shield.domain.enumeration.RecordType;

/**
 * A GateRecord.
 */
@Entity
@Table(name = "gate_record")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GateRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private RecordType recordType;

    @NotNull
    @Column(name = "truck_number", nullable = false)
    private String truckNumber;

    @NotNull
    @Column(name = "record_time", nullable = false)
    private ZonedDateTime recordTime;

//    @Lob
    @Column(name = "data")
    private String data;

    @NotNull
    @Column(name = "rid", nullable = false, unique = true)
    private String rid;

    @NotNull
    @Column(name = "create_time", nullable = false)
    private ZonedDateTime createTime;

    @NotNull
    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(name = "data_md5")
    private String dataMd5;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecordType getRecordType() {
        return recordType;
    }

    public GateRecord recordType(RecordType recordType) {
        this.recordType = recordType;
        return this;
    }

    public void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public GateRecord truckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
        return this;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public ZonedDateTime getRecordTime() {
        return recordTime;
    }

    public GateRecord recordTime(ZonedDateTime recordTime) {
        this.recordTime = recordTime;
        return this;
    }

    public void setRecordTime(ZonedDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public String getData() {
        return data;
    }

    public GateRecord data(String data) {
        this.data = data;
        return this;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRid() {
        return rid;
    }

    public GateRecord rid(String rid) {
        this.rid = rid;
        return this;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public GateRecord createTime(ZonedDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getRegionId() {
        return regionId;
    }

    public GateRecord regionId(Long regionId) {
        this.regionId = regionId;
        return this;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getDataMd5() {
        return dataMd5;
    }

    public GateRecord dataMd5(String dataMd5) {
        this.dataMd5 = dataMd5;
        return this;
    }

    public void setDataMd5(String dataMd5) {
        this.dataMd5 = dataMd5;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GateRecord)) {
            return false;
        }
        return id != null && id.equals(((GateRecord) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "GateRecord{" +
            "id=" + getId() +
            ", recordType='" + getRecordType() + "'" +
            ", truckNumber='" + getTruckNumber() + "'" +
            ", recordTime='" + getRecordTime() + "'" +
            ", data='" + getData() + "'" +
            ", rid='" + getRid() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", regionId=" + getRegionId() +
            ", dataMd5='" + getDataMd5() + "'" +
            "}";
    }
}
