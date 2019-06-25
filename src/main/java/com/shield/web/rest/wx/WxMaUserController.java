package com.shield.web.rest.wx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shield.config.WxMiniAppConfiguration;
import com.shield.domain.User;
import com.shield.domain.WxMaUser;
import com.shield.security.jwt.JWTFilter;
import com.shield.security.jwt.TokenProvider;
import com.shield.service.UserService;
import com.shield.service.WxMaUserService;
import com.shield.service.dto.WxMaUserDTO;
import com.shield.utils.JsonUtils;
import com.shield.web.rest.UserJWTController;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    }

    @PostMapping("/login")
    public ResponseEntity<JWTToken> login(@PathVariable String appid, @Valid @RequestBody LoginVm loginVm) {
        final WxMaService wxService = WxMiniAppConfiguration.getMaService(appid);

        try {
            WxMaJscode2SessionResult session = wxService.getUserService().getSessionInfo(loginVm.code);
            logger.info("WeChat user openid: {} login, session_key: {}", session.getOpenid(), session.getSessionKey());

            Optional<WxMaUserDTO> wxMaUser = wxMaUserService.findByOpenId(session.getOpenid());
            User user;
            Authentication authentication;
            if (wxMaUser.isPresent()) {
                Long userId = wxMaUser.get().getUserId();
                WxMaUserDTO wxMaUserDTO = wxMaUser.get();
                user = userService.getUserWithAuthorities(userId).get();
                wxMaUserDTO.setUpdateTime(ZonedDateTime.now());
                wxMaUserDTO.updateWithWxUserInfo(loginVm.userInfo);
                wxMaUserService.save(wxMaUserDTO);

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getLogin());
                authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            } else if (StringUtils.isNotBlank(loginVm.login) && StringUtils.isNotBlank(loginVm.password)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginVm.login, loginVm.password);
                authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
                WxMaUserDTO wxMaUserDTO = new WxMaUserDTO();
                wxMaUserDTO.updateWithWxUserInfo(loginVm.userInfo);
                wxMaUserDTO.setOpenId(session.getOpenid());
                wxMaUserDTO.setUnionId(session.getUnionid());
                wxMaUserDTO.setCreateTime(ZonedDateTime.now());
                wxMaUserDTO.setUpdateTime(ZonedDateTime.now());
                wxMaUserDTO.setUserId(userService.getUserWithAuthoritiesByLogin(loginVm.login).get().getId());
                wxMaUserService.save(wxMaUserDTO);
            } else {
                return new ResponseEntity<>(new JWTToken(null, "请先绑定预约员账号！"), HttpStatus.UNAUTHORIZED);
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