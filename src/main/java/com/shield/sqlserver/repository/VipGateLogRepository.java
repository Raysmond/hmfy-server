package com.shield.sqlserver.repository;

import com.shield.sqlserver.domain.VipGateLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VipGateLogRepository extends JpaRepository<VipGateLog, Long> {

    Page<VipGateLog> findByTruckNumber(String truckNumber, Pageable pageable);

    List<VipGateLog> findByTruckNumber(String truckNumber);

    @Query("select max(c.rowId) from VipGateLog c")
    Long findMaxRowId();
}
