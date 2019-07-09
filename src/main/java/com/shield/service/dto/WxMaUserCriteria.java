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
 * Criteria class for the {@link com.shield.domain.WxMaUser} entity. This class is used
 * in {@link com.shield.web.rest.WxMaUserResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /wx-ma-users?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class WxMaUserCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter openId;

    private StringFilter nickName;

    private StringFilter gender;

    private StringFilter language;

    private StringFilter city;

    private StringFilter province;

    private StringFilter country;

    private StringFilter avatarUrl;

    private StringFilter unionId;

    private StringFilter watermark;

    private ZonedDateTimeFilter createTime;

    private ZonedDateTimeFilter updateTime;

    private StringFilter phone;

    private StringFilter appId;

    private LongFilter userId;

    public WxMaUserCriteria(){
    }

    public WxMaUserCriteria(WxMaUserCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.openId = other.openId == null ? null : other.openId.copy();
        this.nickName = other.nickName == null ? null : other.nickName.copy();
        this.gender = other.gender == null ? null : other.gender.copy();
        this.language = other.language == null ? null : other.language.copy();
        this.city = other.city == null ? null : other.city.copy();
        this.province = other.province == null ? null : other.province.copy();
        this.country = other.country == null ? null : other.country.copy();
        this.avatarUrl = other.avatarUrl == null ? null : other.avatarUrl.copy();
        this.unionId = other.unionId == null ? null : other.unionId.copy();
        this.watermark = other.watermark == null ? null : other.watermark.copy();
        this.createTime = other.createTime == null ? null : other.createTime.copy();
        this.updateTime = other.updateTime == null ? null : other.updateTime.copy();
        this.phone = other.phone == null ? null : other.phone.copy();
        this.appId = other.appId == null ? null : other.appId.copy();
        this.userId = other.userId == null ? null : other.userId.copy();
    }

    @Override
    public WxMaUserCriteria copy() {
        return new WxMaUserCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getOpenId() {
        return openId;
    }

    public void setOpenId(StringFilter openId) {
        this.openId = openId;
    }

    public StringFilter getNickName() {
        return nickName;
    }

    public void setNickName(StringFilter nickName) {
        this.nickName = nickName;
    }

    public StringFilter getGender() {
        return gender;
    }

    public void setGender(StringFilter gender) {
        this.gender = gender;
    }

    public StringFilter getLanguage() {
        return language;
    }

    public void setLanguage(StringFilter language) {
        this.language = language;
    }

    public StringFilter getCity() {
        return city;
    }

    public void setCity(StringFilter city) {
        this.city = city;
    }

    public StringFilter getProvince() {
        return province;
    }

    public void setProvince(StringFilter province) {
        this.province = province;
    }

    public StringFilter getCountry() {
        return country;
    }

    public void setCountry(StringFilter country) {
        this.country = country;
    }

    public StringFilter getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(StringFilter avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public StringFilter getUnionId() {
        return unionId;
    }

    public void setUnionId(StringFilter unionId) {
        this.unionId = unionId;
    }

    public StringFilter getWatermark() {
        return watermark;
    }

    public void setWatermark(StringFilter watermark) {
        this.watermark = watermark;
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

    public StringFilter getPhone() {
        return phone;
    }

    public void setPhone(StringFilter phone) {
        this.phone = phone;
    }

    public StringFilter getAppId() {
        return appId;
    }

    public void setAppId(StringFilter appId) {
        this.appId = appId;
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
        final WxMaUserCriteria that = (WxMaUserCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(openId, that.openId) &&
            Objects.equals(nickName, that.nickName) &&
            Objects.equals(gender, that.gender) &&
            Objects.equals(language, that.language) &&
            Objects.equals(city, that.city) &&
            Objects.equals(province, that.province) &&
            Objects.equals(country, that.country) &&
            Objects.equals(avatarUrl, that.avatarUrl) &&
            Objects.equals(unionId, that.unionId) &&
            Objects.equals(watermark, that.watermark) &&
            Objects.equals(createTime, that.createTime) &&
            Objects.equals(updateTime, that.updateTime) &&
            Objects.equals(phone, that.phone) &&
            Objects.equals(appId, that.appId) &&
            Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        openId,
        nickName,
        gender,
        language,
        city,
        province,
        country,
        avatarUrl,
        unionId,
        watermark,
        createTime,
        updateTime,
        phone,
        appId,
        userId
        );
    }

    @Override
    public String toString() {
        return "WxMaUserCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (openId != null ? "openId=" + openId + ", " : "") +
                (nickName != null ? "nickName=" + nickName + ", " : "") +
                (gender != null ? "gender=" + gender + ", " : "") +
                (language != null ? "language=" + language + ", " : "") +
                (city != null ? "city=" + city + ", " : "") +
                (province != null ? "province=" + province + ", " : "") +
                (country != null ? "country=" + country + ", " : "") +
                (avatarUrl != null ? "avatarUrl=" + avatarUrl + ", " : "") +
                (unionId != null ? "unionId=" + unionId + ", " : "") +
                (watermark != null ? "watermark=" + watermark + ", " : "") +
                (createTime != null ? "createTime=" + createTime + ", " : "") +
                (updateTime != null ? "updateTime=" + updateTime + ", " : "") +
                (phone != null ? "phone=" + phone + ", " : "") +
                (appId != null ? "appId=" + appId + ", " : "") +
                (userId != null ? "userId=" + userId + ", " : "") +
            "}";
    }

}
