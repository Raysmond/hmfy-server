package com.shield.web.rest.wx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shield.config.WxMiniAppConfiguration;
import com.shield.domain.User;
import com.shield.domain.WxMaUser;
import com.shield.security.SecurityUtils;
import com.shield.security.jwt.JWTFilter;
import com.shield.security.jwt.TokenProvider;
import com.shield.service.UserService;
import com.shield.service.WxMaUserService;
import com.shield.service.dto.WxMaUserDTO;
import com.shield.utils.JsonUtils;
import com.shield.web.rest.UserJWTController;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import me.chanjar.weixin.common.error.WxErrorException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 微信小程序用户接口
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@RestController
@RequestMapping("/api/wx/{appid}/user")
public class WxMaUserController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private WxMaUserService wxMaUserService;

    @Data
    static class JWTToken {

        private String token;

        private String reason;

        JWTToken(String token) {
            this.token = token;
        }

        JWTToken(String token, String reason) {
            this.token = token;
            this.reason = reason;
        }
    }

    @Data
    static class LoginVm {
        @NotBlank
        String code;
        WxMaUserInfo userInfo;
        String login;
        String password;
        String encryptedData;
        String iv;
        String errMsg;
        String signature;
    }

    @PostMapping("/login")
    public ResponseEntity<JWTToken> login(@PathVariable String appid, @Valid @RequestBody LoginVm loginVm) {
        logger.info("Wechat user login: {}", loginVm.toString());
        final WxMaService wxService = WxMiniAppConfiguration.getMaService(appid);
        if (SecurityUtils.isAuthenticated()) {
            return ResponseEntity.badRequest().body(new JWTToken(null, "您已登录!"));
        }

        try {
            WxMaJscode2SessionResult session = wxService.getUserService().getSessionInfo(loginVm.code);
            logger.info("WeChat user openid: {} login, session_key: {}, unionid: {}", session.getOpenid(), session.getSessionKey(), session.getUnionid());

            String unionId = session.getUnionid();
            if (StringUtils.isBlank(unionId) && StringUtils.isNotBlank(loginVm.encryptedData)) {
                WxMaUserInfo wxMaUserInfo = wxService.getUserService().getUserInfo(session.getSessionKey(), loginVm.getEncryptedData(), loginVm.getIv());
                logger.info("Wechat userInfo from encryptedData: openid: {}, unionid: {}", wxMaUserInfo.getOpenId(), wxMaUserInfo.getUnionId());
                unionId = wxMaUserInfo.getUnionId();
            }

            List<WxMaUserDTO> wxMaUsers = wxMaUserService.findAllByOpenId(appid, session.getOpenid());
            WxMaUserDTO wxMaUser = wxMaUsers.isEmpty() ? null : wxMaUsers.get(0);
            User user;
            Authentication authentication;
            if (wxMaUser != null && wxMaUser.getUserId() != null) {
                // 微信号已经绑定预约员，自动登录
                user = userService.getUserWithAuthorities(wxMaUser.getUserId()).get();
                wxMaUser.setUpdateTime(ZonedDateTime.now());
                wxMaUser.updateWithWxUserInfo(loginVm.userInfo);
                if (StringUtils.isNotBlank(unionId)) {
                    wxMaUser.setUnionId(unionId);
                }
                wxMaUser.setAppId(appid);
                wxMaUserService.save(wxMaUser);
                userService.clearUserCaches(user);
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getLogin());
                authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            } else if (StringUtils.isNotBlank(loginVm.login) && StringUtils.isNotBlank(loginVm.password)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginVm.login, loginVm.password);
                authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
                user = userService.getUserWithAuthoritiesByLogin(loginVm.login).get();
                if (user.getWxMaUser() != null) {
                    // 预约员已被其他人绑定
                    userService.cancelWeChatAccountBind(user);
                }
                if (wxMaUser == null) {
                    wxMaUser = new WxMaUserDTO();
                }
                if (StringUtils.isNotBlank(unionId)) {
                    wxMaUser.setUnionId(unionId);
                }
                wxMaUser.setOpenId(session.getOpenid());
                wxMaUser.setAppId(appid);
                wxMaUser.setUserId(user.getId());
                wxMaUser.updateWithWxUserInfo(loginVm.userInfo);
                wxMaUser.setCreateTime(ZonedDateTime.now());
                wxMaUser.setUpdateTime(ZonedDateTime.now());
                userService.bindWeChatAccount(user, wxMaUser);
            } else {
                return new ResponseEntity<>(new JWTToken(null, "请先登录！"), HttpStatus.UNAUTHORIZED);
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.createToken(authentication, false);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
            return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
        } catch (WxErrorException e) {
            this.logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(new JWTToken(null, e.getMessage()));
        }
    }

    @Data
    @NoArgsConstructor
    static class ResultResponse {
        private String message;
        private Integer code = 0;

        public ResultResponse(String message) {
            this.message = message;
        }

        public ResultResponse(String message, Integer code) {
            this.message = message;
            this.code = code;
        }
    }

    @PostMapping("/cancel_wechat_account_binding")
    public ResponseEntity<ResultResponse> cancelWeChatAccountBind() {
        User user = userService.getUserWithAuthorities().get();
        if (user.getWxMaUser() == null) {
            return ResponseEntity.badRequest().body(new ResultResponse("未绑定微信账号", 1));
        } else {
            logger.info("User [id={}, login={}] cancel binding wechat user, openid: {}", user.getId(), user.getLogin(), user.getWxMaUser().getOpenId());
            userService.cancelWeChatAccountBind(user);
            return ResponseEntity.ok(new ResultResponse("ok"));
        }
    }

    @GetMapping("/check-wechat-user-bind")
    public ResponseEntity<ResultResponse> checkWechatUserBind() {
        User user = userService.getUserWithAuthorities().get();
        if (user.getWxMaUser() == null) {
            return ResponseEntity.ok(new ResultResponse("未绑定微信账号", 1));
        } else {
            return ResponseEntity.ok(new ResultResponse("已绑定微信账号", 0));
        }
    }

    /**
     * <pre>
     * 获取用户信息接口
     * </pre>
     */
    @GetMapping("/info")
    public ResponseEntity<WxMaUserDTO> info(@PathVariable String appid, String sessionKey,
                                            String signature, String rawData, String encryptedData, String iv) {
        final WxMaService wxService = WxMiniAppConfiguration.getMaService(appid);

        // 用户信息校验
        if (!wxService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
            return ResponseEntity.badRequest().body(null);
        }

        User user = userService.getUserWithAuthorities().get();

        // 解密用户信息
        WxMaUserInfo userInfo = wxService.getUserService().getUserInfo(sessionKey, encryptedData, iv);
        WxMaUserDTO wxMaUserDTO = wxMaUserService.createOrUpdateWxUserInfo(userInfo, user.getId());
        return ResponseEntity.ok(wxMaUserDTO);
    }

    /**
     * <pre>
     * 获取用户绑定手机号信息
     * </pre>
     */
    @GetMapping("/phone")
    public String phone(@PathVariable String appid, String sessionKey, String signature,
                        String rawData, String encryptedData, String iv) {
        final WxMaService wxService = WxMiniAppConfiguration.getMaService(appid);

        // 用户信息校验
        if (!wxService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
            return "user check failed";
        }

        // 解密
        WxMaPhoneNumberInfo phoneNoInfo = wxService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);

        return JsonUtils.toJson(phoneNoInfo);
    }

}
