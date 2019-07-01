package com.shield.service.tcp;

import lombok.Data;

/**
 * {
 * "service": "whitelist_sync",
 * "parkid": "20180001",
 * "car_number": car_number,
 * "car_type": 0,
 * "card_type": 1,
 * "startdate": "2019-06-29 00:00:01",
 * "validdate": "2019-06-29 15:00:00",
 * "cardmoney": 230.50,
 * "period": u"月",
 * "carusername": u"老陈",
 * "carusertel": "13822220222",
 * "drive_no": "NO111111222",
 * "address": u"xxxx市xxx区xxx路112号",
 * "carlocate": "A-1-10",
 * "create_time": "2019-06-29 11:00:00",
 * "modify_time": "2019-06-29 11:00:00",
 * "operator": u"李四",
 * "operate_type": 1,
 * "limitdaytype": 0,
 * "remark": ""
 * }
 */

@Data
public class UploadCarWhiteListMsg {
    private String service = "whitelist_sync";
    private String parkid;
    private String car_number;
    private Integer car_type = 1; // 0小车/1大车
    private Integer card_type = 1; // 1月租车/2充值车(operate_type=2时不需,因为车牌类型不能修改)
    private String card_id; // 车牌记录号	11221 (operate_type=2时必需)
    private String startdate;
    private String validdate;
    private Double cardmoney = 230.50;
    private String period = "月";
    private String carusername = "取号"; // 车主姓名	老陈
    private String carusertel = "18800000000"; // 车主电话	13822220222
    private String drive_no = "NO000000000"; // 驾驶证	NO111111222
    private String address = "上海市宝山区"; // xxxx市xxx区xxx路112号
    private String carlocate = "A-1-10"; // 车位	A-1-10
    private String create_time;
    private String modify_time;
    private String operator = "管理员";
    private Integer operate_type; // 1.添加注册 2.修改 3.删除
    private Integer limitdaytype = 0; // 0不限行,1单日限行,2双日限行
    private String remark = "";
}
