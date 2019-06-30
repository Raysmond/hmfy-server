package com.shield.service.tcp;

import lombok.Data;

/**
 * {
 * "card_type": 0,
 * "gateinid": 1,
 * "in_time": "2019-06-29 15:05:50",
 * "operatorin": "管理员",
 * "service": "uploadcarin",
 * "parkid": "20180001",
 * "order_id": "118308",
 * "remark": "",
 * "car_number": "沪EB9862",
 * "gateinname": "入口",
 * "car_type": 1
 * }
 */
@Data
public class UploadCarInMsg {
    private String service = "uploadcarin";
    private String parkid;
    private String order_id;
    private String car_number;
    private String in_time;
    private Integer car_type;
    private Integer card_type;
    private Integer gateinid;
    private String gateinname;
    private String operatorin;
    private String remark;
}
