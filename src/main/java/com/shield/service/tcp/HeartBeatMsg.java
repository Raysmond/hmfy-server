package com.shield.service.tcp;

import lombok.Data;

@Data
public class HeartBeatMsg {
    private String service = "heartbeat";
    private String parkid;
    private String time;
}
