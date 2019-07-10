package com.shield.service;

import com.google.common.collect.Lists;
import com.shield.config.Constants;
import com.shield.domain.Authority;
import com.shield.domain.Region;
import com.shield.domain.User;
import com.shield.domain.WxMaUser;
import com.shield.repository.AuthorityRepository;
import com.shield.repository.UserRepository;
import com.shield.repository.WxMaUserRepository;
import com.shield.security.AuthoritiesConstants;
import com.shield.security.SecurityUtils;
import com.shield.service.dto.UserDTO;
import com.shield.service.dto.WxMaUserDTO;
import com.shield.service.mapper.WxMaUserMapper;
import com.shield.service.util.RandomUtil;
import com.shield.web.rest.errors.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    private final CacheManager cacheManager;

    @Autowired
    private WxMaUserRepository wxMaUserRepository;

    @Autowired
    private WxMaUserService wxMaUserService;

    @Autowired
    private WxMaUserMapper wxMaUserMapper;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository, CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                this.clearUserCaches(user);
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository.findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                this.clearUserCaches(user);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmailIgnoreCase(mail)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                this.clearUserCaches(user);
                return user;
            });
    }

    public User registerUser(UserDTO userDTO, String password) {
        userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new LoginAlreadyUsedException();
            }
        });
        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new EmailAlreadyUsedException();
            }
        });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        newUser.setEmail(userDTO.getEmail().toLowerCase());
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        this.clearUserCaches(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.getActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        this.clearUserCaches(existingUser);
        return true;
    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail().toLowerCase());
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }

        if (StringUtils.isNotBlank(userDTO.getRawPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getRawPassword()));
        } else {
            // String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
            String encryptedPassword = passwordEncoder.encode("bt!888");
            user.setPassword(encryptedPassword);
        }
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        user.setTruckNumber(userDTO.getTruckNumber());
        user.setCompany(userDTO.getCompany());
        user.setCarCompany(userDTO.getCarCompany());
        user.setCarCapacity(userDTO.getCarCapacity());
        user.setMemo(userDTO.getMemo());
        user.setPhone(userDTO.getPhone());
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO.getAuthorities().stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        if (userDTO.getRegionId() != null) {
            Region region = new Region();
            region.setId(userDTO.getRegionId());
            user.setRegion(region);
        }
        userRepository.save(user);
        this.clearUserCaches(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email.toLowerCase());
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                this.clearUserCaches(user);
                log.debug("Changed Information for User: {}", user);
            });
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        return Optional.of(userRepository
            .findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                this.clearUserCaches(user);
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setEmail(userDTO.getEmail().toLowerCase());
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                if (userDTO.getRegionId() != null) {
                    Region region = new Region();
                    region.setId(userDTO.getRegionId());
                    user.setRegion(region);
                } else {
                    user.setRegion(null);
                }
                if (StringUtils.isNotBlank(userDTO.getRawPassword())) {
                    // 管理员可以更新密码
                    user.setPassword(passwordEncoder.encode(userDTO.getRawPassword()));
                }
                user.setCompany(userDTO.getCompany());
                user.setTruckNumber(userDTO.getTruckNumber());
                user.setCarCompany(userDTO.getCarCompany());
                user.setCarCapacity(userDTO.getCarCapacity());
                user.setMemo(userDTO.getMemo());
                user.setPhone(userDTO.getPhone());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO.getAuthorities().stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                this.clearUserCaches(user);
                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(UserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            this.clearUserCaches(user);
            log.debug("Deleted User: {}", user);
        });
    }

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                this.clearUserCaches(user);
                log.debug("Changed password for User: {}", user);
            });
    }

    public void changeSystemUserPassword(String newPassword) {
        List<User> users = userRepository.findAllById(Lists.newArrayList(1L, 3L, 4L));
        for (User user : users) {
            user.setPassword(passwordEncoder.encode(newPassword));
            this.clearUserCaches(user);
        }
        userRepository.saveAll(users);
    }

    public void changePasswordForAllDrivers(Long startUserId) {
        List<User> users = userRepository.findAll().stream().filter(it -> it.getId() > startUserId).collect(Collectors.toList());
        for (User user : users) {
            System.out.println(user.toString());
            System.out.println("password:" + user.getTruckNumber().substring(3) + user.getPhone().substring(7));
            user.setPassword(passwordEncoder.encode(user.getTruckNumber().substring(3) + user.getPhone().substring(7)));
            this.clearUserCaches(user);
        }
        userRepository.saveAll(users);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> searchAllManagedUsers(Pageable pageable, String query) {
        return userRepository.searchAll("%" + query + "%", pageable).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> searchAllManagedUsersByRegionId(Pageable pageable, String query, Long regionId) {
        return userRepository.searchAllByRegionId("%" + query + "%", regionId, pageable).map(UserDTO::new);
    }


    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsersByRegionId(Pageable pageable, Long regionId) {
        return userRepository.findAllByRegionId(pageable, regionId).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(Long id) {
        return userRepository.findOneWithAuthoritiesById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                log.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
                this.clearUserCaches(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     *
     * @return a list of all the authorities.
     */
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

    public void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin());
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
    }

    public void cancelWeChatAccountBind(User user) {
        if (user.getWxMaUser() != null) {
            log.info("unbind wx ma user openid: {}, user_id: {}, login: {}", user.getWxMaUser().getOpenId(), user.getId(), user.getLogin());
            WxMaUser wxMaUser = user.getWxMaUser();
            user.setWxMaUser(null);
            userRepository.save(user);
            wxMaUser.setUser(null);
            wxMaUser.setUpdateTime(ZonedDateTime.now());
            wxMaUserRepository.save(wxMaUser);
            this.clearUserCaches(user);
        }
    }

    public void bindWeChatAccount(User user, WxMaUserDTO wxMaUserDTO) {
        log.info("bind wx ma user openid: {}, user_id: {}, login: {}", wxMaUserDTO.getOpenId(), user.getId(), user.getLogin());
        wxMaUserDTO.setUserId(user.getId());
        wxMaUserDTO.setUserLogin(user.getLogin());
        WxMaUser wxMaUser = wxMaUserMapper.toEntity(wxMaUserDTO);
        wxMaUser = wxMaUserRepository.save(wxMaUser);
        user.setWxMaUser(wxMaUser);
        userRepository.save(user);
        this.clearUserCaches(user);
    }
}
