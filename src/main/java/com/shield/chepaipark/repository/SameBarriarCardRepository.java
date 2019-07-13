package com.shield.chepaipark.repository;

import com.shield.chepaipark.domain.SameBarriarCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SameBarriarCardRepository extends JpaRepository<SameBarriarCard, Long> {

    List<SameBarriarCard> findByCardNo(String cardNo);
}
