package com.shield.service.tcp;

import lombok.Data;

/**
 * 手动开闸记录上传
 * {
 * "service": "uploadopenstrobe",
 * "result_code": 0,
 * "recordid": "11551",
 * "message": "上传成功"
 * }
 */
@Data
public class UploadOpenStrobeResponse extends ServiceResponse {
    private String recordid;

    public UploadOpenStrobeResponse(String service, int result_code, String message, String recordid) {
        super(service, result_code, message);
        this.recordid = recordid;
    }
}
