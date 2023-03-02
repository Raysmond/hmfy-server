package com.shield.service.tcp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class UploadCarInOutResponse extends ServiceResponse {
    private String order_id;

    public UploadCarInOutResponse(String service, int result_code, String message, String order_id) {
        super(service, result_code, message);
        this.order_id = order_id;
    }
}
