package com.shield.repository;

import com.shield.domain.ParkMsg;
import com.shield.domain.enumeration.ParkMsgType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the ParkMsg entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ParkMsgRepository extends JpaRepository<ParkMsg, Long> {

    @Query("select p from ParkMsg p where p.service = ?1")
    Page<ParkMsg> findByService(String service, Pageable pageable);

    @Query("select p from ParkMsg p where p.service = ?1 and p.truckNumber = ?2 and p.type = ?3")
    Page<ParkMsg> findByServiceAndTruckNumber(String service, String truckNumber, ParkMsgType parkMsgType, Pageable pageable);
}
