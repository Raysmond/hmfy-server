package com.shield.service.tcp;

import lombok.Data;

/**
 * {
 * "service": "checkKey",
 * "parkid": "20180001",
 * "parkkey": "C80FB9B8-73E8-4C03-B300-2037F14F42C6"
 * }
 */
@Data
public class AuthRequest {
    private String service = "checkKey";
    private String parkid;
    private String parkkey;
}
