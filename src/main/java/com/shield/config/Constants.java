package com.shield.config;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";
    
    public static final String SYSTEM_ACCOUNT = "system";
    public static final String DEFAULT_LANGUAGE = "zh-cn";
    public static final String ANONYMOUS_USER = "anonymoususer";

    public static final int AUTO_SET_LEAVE_TIME_AFTER_FINISH_HOURS = 1;

    public static final int LOADING_START_ALERT_EXPIRED_TIMES_HOURS_AFTER_ENTER = 3;

    // 预约取消后，30min之内不能重新预约
    public static final long PENALTY_TIME_MINUTES_CANCEL = 30L;
    public static final String PENALTY_TIME_MINUTES_CANCEL_USER_ID_KEY = "penalty_cancel_user_id:%d";

    // 排队取消后，30min之内不能重新预约
    public static final long PENALTY_TIME_MINUTES_CANCEL_WAIT = 30L;
    public static final String PENALTY_TIME_MINUTES_CANCEL_WAIT_USER_ID_KEY = "penalty_cancel_wait_user_id:%d";

    // 自动过期的，60min之内不能重新预约
    public static final long PENALTY_TIME_MINUTES_EXPIRE = 60L;
    public static final String PENALTY_TIME_MINUTES_EXPIRE_USER_ID_KEY = "penalty_expire_user_id:%d";

    // 四期区域ID
    public static final Long REGION_ID_HUACHAN = 2L;

    private Constants() {
    }
}
