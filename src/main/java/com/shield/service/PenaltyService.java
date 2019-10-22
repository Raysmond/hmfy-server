package com.shield.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.shield.config.Constants.*;
import static com.shield.config.Constants.PENALTY_TIME_MINUTES_EXPIRE_USER_ID_KEY;

@Service
@Slf4j
public class PenaltyService {
    @Autowired
    @Qualifier("redisLongTemplate")
    private RedisTemplate<String, Long> redisLongTemplate;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public boolean isUserInCancelPenalty(Long userId) {
        String k = String.format(PENALTY_TIME_MINUTES_CANCEL_USER_ID_KEY, userId);
        return redisLongTemplate.hasKey(k);
    }

    /**
     * 预约成功后取消惩罚
     */
    public void putUserInCancelPenalty(Long userId) {
        String k = String.format(PENALTY_TIME_MINUTES_CANCEL_USER_ID_KEY, userId);
        redisTemplate.opsForValue().set(k, "1");
        redisTemplate.expire(k, PENALTY_TIME_MINUTES_CANCEL, TimeUnit.MINUTES);
        log.info("Put userId {} in cancel penalty", userId);
    }

    /**
     * 取消排队惩罚
     */
    public void putUserInCancelWaitPenalty(Long userId) {
        String k = String.format(PENALTY_TIME_MINUTES_CANCEL_WAIT_USER_ID_KEY, userId);
        redisTemplate.opsForValue().set(k, "1");
        redisTemplate.expire(k, PENALTY_TIME_MINUTES_CANCEL_WAIT, TimeUnit.MINUTES);
        log.info("Put userId {} in cancel wait penalty", userId);
    }

    public boolean isUserInCancelWaitPenalty(Long userId) {
        String k = String.format(PENALTY_TIME_MINUTES_CANCEL_WAIT_USER_ID_KEY, userId);
        return redisLongTemplate.hasKey(k);
    }

    public boolean isUserInExpirePenalty(Long userId) {
        String k = String.format(PENALTY_TIME_MINUTES_EXPIRE_USER_ID_KEY, userId);
        return redisLongTemplate.hasKey(k);
    }

    public void putUserInExpirePenalty(Long userId) {
        String k = String.format(PENALTY_TIME_MINUTES_EXPIRE_USER_ID_KEY, userId);
        redisTemplate.opsForValue().set(k, "1");
        redisTemplate.expire(k, PENALTY_TIME_MINUTES_EXPIRE, TimeUnit.MINUTES);
        log.info("Put userId {} in expire wait penalty", userId);
    }

}
