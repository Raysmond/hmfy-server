package com.shield.chepaipark.repository;

import com.shield.chepaipark.domain.CardValidDateRange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardValidDateRangeRepository extends JpaRepository<CardValidDateRange, Long> {
    List<CardValidDateRange> findByCardNo(String truckNumber);
}
