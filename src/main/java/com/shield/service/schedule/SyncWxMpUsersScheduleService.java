package com.shield.service.schedule;

import com.shield.service.WxMaUserService;
import com.shield.service.dto.WxMaUserDTO;
import io.github.jhipster.config.JHipsterConstants;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.bean.result.WxMpUserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@Slf4j
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class SyncWxMpUsersScheduleService {
    private final WxMpService wxMpService;

    private final WxMaUserService wxMaUserService;

    @Autowired
    public SyncWxMpUsersScheduleService(WxMaUserService wxMaUserService, WxMpService wxMpService) {
        this.wxMaUserService = wxMaUserService;
        this.wxMpService = wxMpService;
    }

//    @Scheduled(fixedRate = 6 * 3600 * 1000)
    public void syncMpUsers() {
        try {
            int count = 0;
            String nextOpenid = null;
            while (true) {
                WxMpUserList userList = wxMpService.getUserService().userList(nextOpenid);
                count += userList.getCount();
                log.info("Fetched {} / {} wechat user openids, app_id: {}", count, userList.getTotal(), wxMpService.getWxMpConfigStorage().getAppId());

                for (String openid : userList.getOpenids()) {
                    syncMpUser(openid);
                }
                if (count < userList.getTotal()) {
                    nextOpenid = userList.getNextOpenid();
                } else {
                    break;
                }
            }

        } catch (WxErrorException e) {
            log.error("failed to pull wechat users", e);
            e.printStackTrace();
        }
    }

    private void syncMpUser(String openid) {
        log.info("Start to sync mp user openid: {}", openid);
        try {
            WxMaUserDTO wxMaUser = wxMaUserService.findByOpenId(wxMpService.getWxMpConfigStorage().getAppId(), openid)
                .map(user -> {
                    // no need to update user info
                    user.setUpdateTime(ZonedDateTime.now());
                    return user;
                }).orElseGet(() -> {
                    try {
                        WxMpUser wxMpUser = wxMpService.getUserService().userInfo(openid, "zh_CN");
                        WxMaUserDTO user = new WxMaUserDTO();
                        user.setCreateTime(ZonedDateTime.now());
                        user.setUpdateTime(ZonedDateTime.now());
                        user.setOpenId(openid);
                        user.setUnionId(wxMpUser.getUnionId());
                        user.setNickName(wxMpUser.getNickname());
                        user.setCountry(wxMpUser.getCountry());
                        user.setCity(wxMpUser.getCity());
                        user.setProvince(wxMpUser.getProvince());
                        user.setAvatarUrl(wxMpUser.getHeadImgUrl());
                        user.setLanguage(wxMpUser.getLanguage());
                        user.setAppId(wxMpService.getWxMpConfigStorage().getAppId());
                        if (wxMpUser.getSex() != null) {
                            user.setGender(wxMpUser.getSex().toString());
                        }
                        return user;
                    } catch (WxErrorException e) {
                        e.printStackTrace();
                        return null;
                    }
                });

            if (wxMaUser != null && wxMaUser.getId() == null) {
                // new user
                log.info("Save new mp user openid: {}, unionid: {}, nickName: {}", openid, wxMaUser.getUnionId(), wxMaUser.getNickName());
                wxMaUserService.save(wxMaUser);
            }
        } catch (Exception e) {
            log.error("failed to sync mp user openid: {}", openid, e);
            e.printStackTrace();
        }
    }
}
