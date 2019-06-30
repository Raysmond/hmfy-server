package com.shield.service.tcp;

import lombok.Data;

/**
 * {
 * "service": "uploadcarout",
 * "parkid": "20180001",
 * "order_id": "118881",
 * "car_number": "粤B99999",
 * "in_time": "2018-07-25 19:35:40",
 * "out_time": "2018-07-25 22:14:11",
 * "car_type": 1,
 * "card_type": 0,
 * "gateinid": 1,
 * "gateinname": "南门入口",
 * "gateoutid": 2,
 * "gateoutname": "南门出口",
 * "operatorin": "张三",
 * "operatorout": "李四",
 * "paycharge": "20",
 * "realcharge": "10",
 * "breaks_amount": "4.5",
 * "discount_amount": "5.5",
 * "discount_no": "YHQ1234554",
 * "discount_reason": "购物后打折",
 * "pay_type": "现金",
 * "payed": 0,
 * "remark": ""
 * }
 */
@Data
public class UploadCarOutMsg {
    private String service = "uploadcarout";
    private String parkid;
    private String order_id;
    private String car_number;
    private String in_time;
    private String out_time;
    private Integer car_type;
    private Integer card_type;
    private Integer gateinid;
    private String gateinname;
    private Integer gateoutid;
    private String gateoutname;
    private String operatorin;
    private String operatorout;
    private String paycharge;
    private String realcharge;
    private String breaks_amount;
    private String discount_amount;
    private String discount_no;
    private String discount_reason;
    private String pay_type;
    private Integer payed;
    private String remark;
}
