package com.shield.service.impl;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.shield.domain.User;
import com.shield.service.WxMaUserService;
import com.shield.domain.WxMaUser;
import com.shield.repository.WxMaUserRepository;
import com.shield.service.dto.WxMaUserDTO;
import com.shield.service.mapper.WxMaUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Service Implementation for managing {@link WxMaUser}.
 */
@Service
@Transactional
public class WxMaUserServiceImpl implements WxMaUserService {

    private final Logger log = LoggerFactory.getLogger(WxMaUserServiceImpl.class);

    private final WxMaUserRepository wxMaUserRepository;

    private final WxMaUserMapper wxMaUserMapper;

    public WxMaUserServiceImpl(WxMaUserRepository wxMaUserRepository, WxMaUserMapper wxMaUserMapper) {
        this.wxMaUserRepository = wxMaUserRepository;
        this.wxMaUserMapper = wxMaUserMapper;
    }

    @Override
    public WxMaUserDTO createOrUpdateWxUserInfo(WxMaUserInfo wxMaUserInfo, Long userId) {
        log.debug("Request to update wxMaUserInfo : {}", wxMaUserInfo);
        WxMaUser wxMaUser = wxMaUserRepository.findOneByUserId(userId);
        if (wxMaUser == null) {
            wxMaUser = new WxMaUser();
            User user = new User();
            user.setId(userId);
            wxMaUser.setUser(user);
            wxMaUser.setCreateTime(ZonedDateTime.now());
        }
        wxMaUser.setOpenId(wxMaUserInfo.getOpenId());
        wxMaUser.setUnionId(wxMaUserInfo.getUnionId());
        wxMaUser.setNickName(wxMaUserInfo.getNickName());
        wxMaUser.setGender(wxMaUserInfo.getGender());
        wxMaUser.setLanguage(wxMaUserInfo.getLanguage());
        wxMaUser.setAvatarUrl(wxMaUserInfo.getAvatarUrl());
        wxMaUser.setProvince(wxMaUserInfo.getProvince());
        wxMaUser.setCity(wxMaUserInfo.getCity());
        wxMaUser.setUpdateTime(ZonedDateTime.now());

        wxMaUser = wxMaUserRepository.save(wxMaUser);
        return wxMaUserMapper.toDto(wxMaUser);
    }

    @Override
    public Optional<WxMaUserDTO> findByOpenId(String appId, String openId) {
        return wxMaUserRepository.findByAppIdAndOpenId(appId, openId)
            .map(wxMaUserMapper::toDto);
    }

    @Override
    public Optional<WxMaUserDTO> findByUserId(Long userId) {
        return wxMaUserRepository.findByUserId(userId)
            .map(wxMaUserMapper::toDto);
    }

    /**
     * Save a wxMaUser.
     *
     * @param wxMaUserDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public WxMaUserDTO save(WxMaUserDTO wxMaUserDTO) {
        log.debug("Request to save WxMaUser : {}", wxMaUserDTO);
        WxMaUser wxMaUser = wxMaUserMapper.toEntity(wxMaUserDTO);
        wxMaUser = wxMaUserRepository.save(wxMaUser);
        return wxMaUserMapper.toDto(wxMaUser);
    }

    /**
     * Get all the wxMaUsers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WxMaUserDTO> findAll(Pageable pageable) {
        log.debug("Request to get all WxMaUsers");
        return wxMaUserRepository.findAll(pageable)
            .map(wxMaUserMapper::toDto);
    }


    /**
     * Get one wxMaUser by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<WxMaUserDTO> findOne(Long id) {
        log.debug("Request to get WxMaUser : {}", id);
        return wxMaUserRepository.findById(id)
            .map(wxMaUserMapper::toDto);
    }

    /**
     * Delete the wxMaUser by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete WxMaUser : {}", id);
        wxMaUserRepository.deleteById(id);
    }
}
