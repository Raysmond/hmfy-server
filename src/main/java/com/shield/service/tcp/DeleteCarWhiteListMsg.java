package com.shield.service.tcp;

import lombok.Data;

/**
 * {
 * "service": "whitelist_sync",
 * "parkid": "20180001",
 * "card_id": "11221",
 * "operate_type": 3,
 * "car_number": "粤B99999"
 * }
 */
@Data
public class DeleteCarWhiteListMsg {
    private String service = "whitelist_sync";
    private String parkid;
    private String car_number;
    private String card_id; // 车牌记录号	11221 (operate_type=2时必需)
    private Integer operate_type = 3;
}
