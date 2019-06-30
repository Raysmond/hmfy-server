package com.shield.service.tcp;


import lombok.Data;

/**
 * 注册/修改/删除固定车白名单 客户端返回结果
 * {
 * "service": "whitelist_sync",
 * "result_code": 0,
 * "card_id": "11221",
 * "car_number": "粤B99999",
 * "message": "增\删\改\成功"
 * }
 */
@Data
public class WhiteListMsgResponse extends ServiceResponse {
    private String car_number;
    private String card_id;
}
