package com.shield.repository;

import com.shield.domain.Car;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data  repository for the Car entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {

    @Query("select car from Car car where car.user.login = ?#{principal.username}")
    List<Car> findByUserIsCurrentUser();

    List<Car> findByUserId(Long userId);
}
