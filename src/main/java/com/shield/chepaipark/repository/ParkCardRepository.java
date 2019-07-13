package com.shield.chepaipark.repository;

import com.shield.chepaipark.domain.ParkCard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParkCardRepository extends JpaRepository<ParkCard, Long> {
    @Query("select c from ParkCard c where c.cardNo = ?1 and c.ctid = 1 and c.phone = '18800000000'")
    List<ParkCard> findByCardNo(String cardNo);

    @Query("select c from ParkCard c")
    List<ParkCard> findLastParkCard(Pageable pageable);
}
