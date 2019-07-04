package com.shield.service.dto;
import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import com.shield.domain.enumeration.ParkMsgType;

/**
 * A DTO for the {@link com.shield.domain.ParkMsg} entity.
 */
public class ParkMsgDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 64)
    private String parkid;

    @NotNull
    @Size(max = 64)
    private String service;

    private String truckNumber;

    @NotNull
    private ZonedDateTime createTime;

    @NotNull
    private ZonedDateTime sendTime;

    @NotNull
    @Size(max = 4096)
    private String body;

    private ParkMsgType type;

    @NotNull
    private Integer sendTimes;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParkid() {
        return parkid;
    }

    public void setParkid(String parkid) {
        this.parkid = parkid;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public ZonedDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(ZonedDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public ParkMsgType getType() {
        return type;
    }

    public void setType(ParkMsgType type) {
        this.type = type;
    }

    public Integer getSendTimes() {
        return sendTimes;
    }

    public void setSendTimes(Integer sendTimes) {
        this.sendTimes = sendTimes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParkMsgDTO parkMsgDTO = (ParkMsgDTO) o;
        if (parkMsgDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), parkMsgDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ParkMsgDTO{" +
            "id=" + getId() +
            ", parkid='" + getParkid() + "'" +
            ", service='" + getService() + "'" +
            ", truckNumber='" + getTruckNumber() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", sendTime='" + getSendTime() + "'" +
            ", body='" + getBody() + "'" +
            ", type='" + getType() + "'" +
            ", sendTimes=" + getSendTimes() +
            "}";
    }
}
