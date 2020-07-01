package com.shield.config;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public static final long LEAVE_ALERT_TIME_AFTER_LOAD_END = 30L;

    public static final Set<String> VIP_CUSTOMER_COMPANIES = Sets.newHashSet("上海宝龙建材有限公司");
    // 手动VIP的预约 把出入场记录写到单独的表中
    public static final String REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS = "sync_vip_gate_log_appointment_ids";

    public static final String REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN = "sync_ship_plan_ids";

    public static final Long REGION_ID_BAOTIAN = 1L;
    public static final Long REGION_ID_HUACHAN = 2L;
    public static final Long REGION_ID_WUQI = 3L;

    public static Map<Long, Integer> REGION_ID_2_AREA_ID = new HashMap<Long, Integer>() {{
        put(REGION_ID_BAOTIAN, 1);
        put(REGION_ID_WUQI, 4);
    }};

    public static Map<Integer, Long> AREA_ID_2_REGION_ID = new HashMap<Integer, Long>() {{
        put(1, REGION_ID_BAOTIAN);
        put(4, REGION_ID_WUQI);
    }};

    private Constants() {
    }
}
