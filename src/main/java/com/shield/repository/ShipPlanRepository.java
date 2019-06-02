package com.shield.repository;

import com.shield.domain.ShipPlan;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data  repository for the ShipPlan entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ShipPlanRepository extends JpaRepository<ShipPlan, Long>, JpaSpecificationExecutor<ShipPlan> {

    @Query("select shipPlan from ShipPlan shipPlan where shipPlan.user.login = ?#{principal.username}")
    List<ShipPlan> findByUserIsCurrentUser();

    @Query("select shipPlan from ShipPlan shipPlan where shipPlan.toUser.login = ?#{principal.username}")
    List<ShipPlan> findByToUserIsCurrentUser();

}
