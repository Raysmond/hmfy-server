package com.shield.repository;

import com.shield.domain.ParkMsg;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the ParkMsg entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ParkMsgRepository extends JpaRepository<ParkMsg, Long> {

}
