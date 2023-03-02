package com.shield.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    // 区域管理员
    public static final String REGION_ADMIN = "ROLE_REGION_ADMIN";

    // 预约员
    public static final String APPOINTMENT = "ROLE_APPOINTMENT";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String ROLE_WEIGHT_USER = "ROLE_WEIGHT_USER";

    private AuthoritiesConstants() {
    }
}
