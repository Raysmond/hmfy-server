package com.shield.repository;

import com.shield.domain.Plan;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Plan entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long>, JpaSpecificationExecutor<Plan> {

}
