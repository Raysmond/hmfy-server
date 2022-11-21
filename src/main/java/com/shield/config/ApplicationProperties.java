package com.shield.config;

import lombok.Data;
import org.apache.commons.compress.utils.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Properties specific to Shield.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Data
public class ApplicationProperties {

    private List<MiniAppConfig> miniApps;

    private RegionConfig region = new RegionConfig();


    @Data
    public static class MiniAppConfig {
        /**
         * 设置微信小程序的appid
         */
        private String appid;

        /**
         * 设置微信小程序的Secret
         */
        private String secret;

        /**
         * 设置微信小程序消息服务器配置的token
         */
        private String token;

        /**
         * 设置微信小程序消息服务器配置的EncodingAESKey
         */
        private String aesKey;

        /**
         * 消息格式，XML或者JSON
         */
        private String msgDataFormat;
    }

    @Data
    public static class RegionConfig {
        /**
         * 区域固定配额，用于预约第二天的计划
         */
        private Map<Long, Integer> fixedQuotaForTomorrow = new HashMap<>();

        /**
         * 出门证配置
         */
        private OutApplicationConfig outApplicationConfig = new OutApplicationConfig();
    }

    /**
     * 出门证配置，目前只有化产区域使用
     */
    @Data
    public static class OutApplicationConfig {
        /**
         * 出门证有效时间，默认30min
         */
        private int validTimeInMinutes = 30;
        /**
         * 提货之后默认8分钟后作为出门证有效期开始时间
         */
        private int startTimeOffsetInMinutes = 8;

        private List<String> forbiddenTimePeriods = Lists.newArrayList();
    }
}
