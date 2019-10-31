package com.shield.service.dto;
import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Lob;
import com.shield.domain.enumeration.RecordType;

/**
 * A DTO for the {@link com.shield.domain.GateRecord} entity.
 */
public class GateRecordDTO implements Serializable {

    private Long id;

    @NotNull
    private RecordType recordType;

    @NotNull
    private String truckNumber;

    @NotNull
    private ZonedDateTime recordTime;

    @Lob
    private String data;

    @NotNull
    private String rid;

    @NotNull
    private ZonedDateTime createTime;

    @NotNull
    private Long regionId;

    private String dataMd5;

    private ZonedDateTime modifyTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public ZonedDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(ZonedDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getDataMd5() {
        return dataMd5;
    }

    public void setDataMd5(String dataMd5) {
        this.dataMd5 = dataMd5;
    }

    public ZonedDateTime getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(ZonedDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GateRecordDTO gateRecordDTO = (GateRecordDTO) o;
        if (gateRecordDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), gateRecordDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "GateRecordDTO{" +
            "id=" + getId() +
            ", recordType='" + getRecordType() + "'" +
            ", truckNumber='" + getTruckNumber() + "'" +
            ", recordTime='" + getRecordTime() + "'" +
            ", data='" + getData() + "'" +
            ", rid='" + getRid() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", regionId=" + getRegionId() +
            ", dataMd5='" + getDataMd5() + "'" +
            ", modifyTime='" + getModifyTime() + "'" +
            "}";
    }
}
