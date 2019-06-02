package com.shield.web.rest.vm;

import lombok.Data;


@Data
public class ResponseDTO {
    private Integer code = 200;
    private String error;
    private Object data;

    public ResponseDTO() {
    }

    public ResponseDTO(Object data) {
        this.data = data;
    }

    public ResponseDTO(Integer code, String error) {
        this.code = code;
        this.error = error;
    }

    public ResponseDTO(Integer code, String error, Object data) {
        this.code = code;
        this.error = error;
        this.data = data;
    }
}
