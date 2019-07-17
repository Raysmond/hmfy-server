package com.shield.chepaipark.repository;

import com.shield.chepaipark.domain.GateIO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;

public interface GateIORepository extends JpaRepository<GateIO, Long> {

    @Query("select g from GateIO g where g.gateInTime >= ?1 or g.gateOutTime >= ?1")
    List<GateIO> findAllNewerThan(ZonedDateTime lastSyncTime);
}
