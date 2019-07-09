package com.shield.service;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.shield.domain.WxMaUser;
import com.shield.service.dto.WxMaUserDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link com.shield.domain.WxMaUser}.
 */
public interface WxMaUserService {

    WxMaUserDTO createOrUpdateWxUserInfo(WxMaUserInfo wxMaUserInfo, Long userId);

    Optional<WxMaUserDTO> findByOpenId(String appId, String openId);

    Optional<WxMaUserDTO> findByUserId(Long userId);

    /**
     * Save a wxMaUser.
     *
     * @param wxMaUserDTO the entity to save.
     * @return the persisted entity.
     */
    WxMaUserDTO save(WxMaUserDTO wxMaUserDTO);

    /**
     * Get all the wxMaUsers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<WxMaUserDTO> findAll(Pageable pageable);


    /**
     * Get the "id" wxMaUser.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<WxMaUserDTO> findOne(Long id);

    /**
     * Delete the "id" wxMaUser.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
