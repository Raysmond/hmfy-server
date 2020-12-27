package com.shield.repository;

import com.shield.domain.User;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    String USERS_BY_LOGIN_CACHE = "usersByLogin";

    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(Instant dateTime);

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmailIgnoreCase(String email);

    Optional<User> findOneByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesById(Long id);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_LOGIN_CACHE)
    Optional<User> findOneWithAuthoritiesByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE)
    Optional<User> findOneWithAuthoritiesByEmail(String email);

    Page<User> findAllByLoginNot(Pageable pageable, String login);

//    @Query(value = "select u.* from jhi_user u join jhi_user_authority ju on ju.user_id = u.id and ju.authority_name not in ('ROLE_ADMIN','ROLE_REGION_ADMIN') " +
//        "where u.region_id = :regionId group by u.id", nativeQuery = true)
//    Page<User> findAllByRegionId(Pageable pageable, @Param("regionId") Long regionId);

    Page<User> findAllByRegionId(Pageable pageable, Long regionId);

    @Query("select u from User u where u.login <> 'anonymoususer' and (u.truckNumber like ?1 or u.firstName like ?1 or u.phone like ?1 or u.company like ?1 or u.carCompany like ?1)")
    Page<User> searchAll(String query, Pageable pageable);

    List<User> findByTruckNumber(String truckNumber);

    @Query("select u from User u where u.region.id = ?2 and u.login <> 'anonymoususer' and (u.truckNumber like ?1 or u.firstName like ?1 or u.phone like ?1)")
    Page<User> searchAllByRegionId(String query, Long regionId, Pageable pageable);

    @EntityGraph(attributePaths = "authorities")
    Optional<User>  findOneWithAuthoritiesByUnionId(Long unionId);
}
