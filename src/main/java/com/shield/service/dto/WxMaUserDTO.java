package com.shield.service.dto;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;

import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.shield.domain.WxMaUser} entity.
 */
public class WxMaUserDTO implements Serializable {

    private Long id;

    @NotNull
    private String openId;

    private String nickName;

    private String gender;

    private String language;

    private String city;

    private String province;

    private String country;

    private String avatarUrl;

    private String unionId;

    private String watermark;

    @NotNull
    private ZonedDateTime createTime;

    @NotNull
    private ZonedDateTime updateTime;

    private String phone;

    @NotNull
    private String appId;

    private Long userId;

    private String userLogin;

    public void updateWithWxUserInfo(WxMaUserInfo wxMaUserInfo) {
        this.nickName = wxMaUserInfo.getNickName();
        this.avatarUrl = wxMaUserInfo.getAvatarUrl();
        this.province = wxMaUserInfo.getProvince();
        this.city = wxMaUserInfo.getCity();
        this.language = wxMaUserInfo.getLanguage();
        this.gender = wxMaUserInfo.getGender();
        this.country = wxMaUserInfo.getCountry();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getWatermark() {
        return watermark;
    }

    public void setWatermark(String watermark) {
        this.watermark = watermark;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WxMaUserDTO wxMaUserDTO = (WxMaUserDTO) o;
        if (wxMaUserDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), wxMaUserDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "WxMaUserDTO{" +
            "id=" + getId() +
            ", openId='" + getOpenId() + "'" +
            ", nickName='" + getNickName() + "'" +
            ", gender='" + getGender() + "'" +
            ", language='" + getLanguage() + "'" +
            ", city='" + getCity() + "'" +
            ", province='" + getProvince() + "'" +
            ", country='" + getCountry() + "'" +
            ", avatarUrl='" + getAvatarUrl() + "'" +
            ", unionId='" + getUnionId() + "'" +
            ", watermark='" + getWatermark() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            ", phone='" + getPhone() + "'" +
            ", appId='" + getAppId() + "'" +
            ", user=" + getUserId() +
            ", user='" + getUserLogin() + "'" +
            "}";
    }
}
