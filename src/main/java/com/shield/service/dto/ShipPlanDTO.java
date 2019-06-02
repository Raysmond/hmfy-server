package com.shield.service.dto;
import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import com.shield.domain.enumeration.ShipMethod;

/**
 * A DTO for the {@link com.shield.domain.ShipPlan} entity.
 */
public class ShipPlanDTO implements Serializable {

    private Long id;

    private String company;

    @NotNull
    @Min(value = 0)
    private Integer demandedAmount;

    private Integer finishAmount;

    private Integer remainAmount;

    private Integer availableAmount;

    private ShipMethod shipMethond;

    private String shipNumber;

    private ZonedDateTime endTime;

    private ZonedDateTime createTime;

    private ZonedDateTime updateTime;

    @NotNull
    private String licensePlateNumber;

    private String driver;

    private String phone;


    private Long userId;

    private String userLogin;

    private Long toUserId;

    private String toUserLogin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Integer getDemandedAmount() {
        return demandedAmount;
    }

    public void setDemandedAmount(Integer demandedAmount) {
        this.demandedAmount = demandedAmount;
    }

    public Integer getFinishAmount() {
        return finishAmount;
    }

    public void setFinishAmount(Integer finishAmount) {
        this.finishAmount = finishAmount;
    }

    public Integer getRemainAmount() {
        return remainAmount;
    }

    public void setRemainAmount(Integer remainAmount) {
        this.remainAmount = remainAmount;
    }

    public Integer getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(Integer availableAmount) {
        this.availableAmount = availableAmount;
    }

    public ShipMethod getShipMethond() {
        return shipMethond;
    }

    public void setShipMethond(ShipMethod shipMethond) {
        this.shipMethond = shipMethond;
    }

    public String getShipNumber() {
        return shipNumber;
    }

    public void setShipNumber(String shipNumber) {
        this.shipNumber = shipNumber;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
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

    public String getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public void setLicensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long userId) {
        this.toUserId = userId;
    }

    public String getToUserLogin() {
        return toUserLogin;
    }

    public void setToUserLogin(String userLogin) {
        this.toUserLogin = userLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShipPlanDTO shipPlanDTO = (ShipPlanDTO) o;
        if (shipPlanDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), shipPlanDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ShipPlanDTO{" +
            "id=" + getId() +
            ", company='" + getCompany() + "'" +
            ", demandedAmount=" + getDemandedAmount() +
            ", finishAmount=" + getFinishAmount() +
            ", remainAmount=" + getRemainAmount() +
            ", availableAmount=" + getAvailableAmount() +
            ", shipMethond='" + getShipMethond() + "'" +
            ", shipNumber='" + getShipNumber() + "'" +
            ", endTime='" + getEndTime() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            ", licensePlateNumber='" + getLicensePlateNumber() + "'" +
            ", driver='" + getDriver() + "'" +
            ", phone='" + getPhone() + "'" +
            ", user=" + getUserId() +
            ", user='" + getUserLogin() + "'" +
            ", toUser=" + getToUserId() +
            ", toUser='" + getToUserLogin() + "'" +
            "}";
    }
}
