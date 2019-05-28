package com.shield.repository;

import com.shield.domain.WxMaUser;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.DoubleStream;


/**
 * Spring Data  repository for the WxMaUser entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WxMaUserRepository extends JpaRepository<WxMaUser, Long>, JpaSpecificationExecutor<WxMaUser> {
    WxMaUser findOneByUserId(Long userId);

    Optional<WxMaUser> findByOpenId(String openId);

    Optional<WxMaUser> findByUserId(Long userId);
}
