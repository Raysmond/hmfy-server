package com.shield.web.rest.vm;

import lombok.Builder;
import lombok.Data;
import org.apache.http.HttpStatus;

@Data
@Builder
public class ApiResponse<T> {
    private Integer status;
    private T data;
    private String message;
}
