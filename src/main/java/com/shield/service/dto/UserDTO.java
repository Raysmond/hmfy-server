package com.shield.service.dto;

import com.shield.config.Constants;

import com.shield.domain.Authority;
import com.shield.domain.User;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.*;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 */
public class UserDTO {

    private Long id;

    @NotBlank
//    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 2, max = 50)
    private String login;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    @Size(max = 256)
    private String imageUrl;

    private boolean activated = false;

    @Size(min = 2, max = 6)
    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Set<String> authorities;

    private WxMaUserDTO userInfo;

    private Long regionId;

    private String regionName;

    private String truckNumber;

    // 用于设置密码
    private String rawPassword;

    private String company;

    // 车辆所属公司
    private String carCompany;

    // 行驶证上荷载量
    private Double carCapacity;

    private String phone;

    private String memo;

    private Long unionId;

    private String unionUsername;


    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.activated = user.getActivated();
        this.imageUrl = user.getImageUrl();
        this.langKey = user.getLangKey();
        this.createdBy = user.getCreatedBy();
        this.createdDate = user.getCreatedDate();
        this.lastModifiedBy = user.getLastModifiedBy();
        this.company = user.getCompany();
        this.lastModifiedDate = user.getLastModifiedDate();
        this.authorities = user.getAuthorities().stream()
            .map(Authority::getName)
            .collect(Collectors.toSet());
        this.truckNumber = user.getTruckNumber();
        this.unionId = user.getUnionId();
        this.unionUsername = user.getUnionUsername();
        if (null != user.getWxMaUser()) {
            this.userInfo = new WxMaUserDTO();
            this.userInfo.setOpenId(user.getWxMaUser().getOpenId());
            this.userInfo.setUnionId(user.getWxMaUser().getUnionId());
            this.userInfo.setNickName(user.getWxMaUser().getNickName());
            this.userInfo.setCreateTime(user.getWxMaUser().getCreateTime());
            this.userInfo.setGender(user.getWxMaUser().getGender());
            this.userInfo.setAvatarUrl(user.getWxMaUser().getAvatarUrl());
            this.userInfo.setLanguage(user.getWxMaUser().getLanguage());
            this.userInfo.setCountry(user.getWxMaUser().getCountry());
            this.userInfo.setProvince(user.getWxMaUser().getProvince());
            this.userInfo.setCity(user.getWxMaUser().getCity());
        }
        if (null != user.getRegion()) {
            this.regionId = user.getRegion().getId();
            this.regionName = user.getRegion().getName();
        }
        this.setCarCompany(user.getCarCompany());
        this.setCarCapacity(user.getCarCapacity());
        this.setMemo(user.getMemo());
        this.setPhone(user.getPhone());
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public String getRawPassword() {
        return rawPassword;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }

    public WxMaUserDTO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(WxMaUserDTO userInfo) {
        this.userInfo = userInfo;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public String getCarCompany() {
        return carCompany;
    }

    public void setCarCompany(String carCompany) {
        this.carCompany = carCompany;
    }

    public Double getCarCapacity() {
        return carCapacity;
    }

    public void setCarCapacity(Double carCapacity) {
        this.carCapacity = carCapacity;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Long getUnionId() {
        return unionId;
    }

    public void setUnionId(Long unionId) {
        this.unionId = unionId;
    }

    public String getUnionUsername() {
        return unionUsername;
    }

    public void setUnionUsername(String unionUsername) {
        this.unionUsername = unionUsername;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
            "login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", activated=" + activated +
            ", langKey='" + langKey + '\'' +
            ", createdBy=" + createdBy +
            ", createdDate=" + createdDate +
            ", lastModifiedBy='" + lastModifiedBy + '\'' +
            ", lastModifiedDate=" + lastModifiedDate +
            ", authorities=" + authorities +
            "}";
    }
}
