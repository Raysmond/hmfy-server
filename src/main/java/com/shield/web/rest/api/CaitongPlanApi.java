package com.shield.web.rest.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.utils.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.shield.web.rest.api.CaitongPlanApi.LockPlanResponse.FAIL_STATUS;

@Component
@Slf4j
public class CaitongPlanApi {
    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    ObjectMapper objectMapper;

    private static final String LOCK_PLAN_API = "http://10.57.148.250:8188/applyinfo/vehiclePlanLocking";
    private static final String END_SHIPPING_API = "http://10.57.148.250:8188/applyinfo/endOfShipment";

    public LockPlanResponse unlockPlan(String applyNumber, String truckNumber) {
        LockPlanRequest request = new LockPlanRequest();
        request.setApplyNumber(applyNumber);
        request.setTruckNumber(truckNumber);
        request.setFlag("U");
        request.setTime(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            String payload = JsonUtils.toJson(request);
            log.info("unlockPlan api: {}, request: {}", LOCK_PLAN_API, payload);
            String response = restTemplate.postForObject(LOCK_PLAN_API, payload, String.class);
            log.info("unlockPlan response: {}", response);
            CTResponse<LockPlanResponse> ctResponse = objectMapper.readValue(response, new TypeReference<CTResponse<LockPlanResponse>>() {
            });
            return ctResponse.data;
        } catch (IOException e) {
            log.error("unlockPlan failed", e);
        }
        LockPlanResponse fail = new LockPlanResponse();
        fail.setStatus(FAIL_STATUS);
        return fail;
    }

    public LockPlanResponse lockPlan(String applyNumber, String truckNumber) {
        LockPlanRequest request = new LockPlanRequest();
        request.setApplyNumber(applyNumber);
        request.setTruckNumber(truckNumber);
        request.setFlag("L");
        request.setTime(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            String payload = JsonUtils.toJson(request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LockPlanRequest> requestHttpEntity = new HttpEntity<>(request, headers);

            log.info("lockPlan api: {}, request: {}", LOCK_PLAN_API, payload);
            ResponseEntity<String> response = restTemplate.exchange(LOCK_PLAN_API, HttpMethod.POST, requestHttpEntity, String.class);
            log.info("lockPlan response: {}", response.getBody());
            CTResponse<LockPlanResponse> ctResponse = objectMapper.readValue(response.getBody(), new TypeReference<CTResponse<LockPlanResponse>>() {
            });
            return ctResponse.data;
        } catch (IOException e) {
            log.error("lockPlan failed", e);
        }
        LockPlanResponse fail = new LockPlanResponse();
        fail.setStatus(FAIL_STATUS);
        return fail;
    }

    @Data
    public static class LockPlanRequest {
        String applyNumber;
        String truckNumber;
        String flag; // L:锁定  U:解锁
        String time; // YYYY-MM-DD HH:MI:SS
    }

    @Data
    public static class LockPlanResponse {
        @JsonProperty("STATUS")
        Integer status;
        @JsonProperty("ERRORMSG")
        String errorMsg;
        String destAddress;

        public static final Integer SUCCESS_STATUS = 1;
        public static final Integer FAIL_STATUS = 0;
    }


    @Data
    public static class CTResponse<T> {
        private Integer code;
        private String message;
        private T data;
    }


    public void endOfShipment(ShipPlanDTO shipPlanDTO) {
        EndOfShipmentRequest request = new EndOfShipmentRequest();
        request.setApplyNumber(shipPlanDTO.getApplyNumber());
        request.setTruckNumber(shipPlanDTO.getTruckNumber());
        request.setNetweight(shipPlanDTO.getNetWeight());
        request.setTareweight(Optional.ofNullable(shipPlanDTO.getTareWeight()).orElse(17.21)); // 皮重默认给 17.21 给老万的系统
        request.setCustomerName(shipPlanDTO.getCompany());
        request.setGoodsName(shipPlanDTO.getProductName());
        request.setPoundNo(shipPlanDTO.getWeightCode());

//        request.setPoundNo(String.format("%s%s2001", shipPlanDTO.getWeigherNo(),
//            ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(2)));
        request.setTareTime(shipPlanDTO.getLoadingStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        request.setCompleteTime(shipPlanDTO.getLoadingEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            String payload = JsonUtils.toJson(request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<EndOfShipmentRequest> requestHttpEntity = new HttpEntity<>(request, headers);

            log.info("endOfShipment api: {}, request: {}", END_SHIPPING_API, payload);
            ResponseEntity<String> response = restTemplate.exchange(END_SHIPPING_API, HttpMethod.POST, requestHttpEntity, String.class);
            log.info("endOfShipment response: {}", response);
//            CTResponse<LockPlanResponse> ctResponse = objectMapper.readValue(response, new TypeReference<CTResponse<LockPlanResponse>>() {
//            });
        } catch (Exception e) {
            log.error("endOfShipment failed", e);
        }
    }


    @Data
    public static class EndOfShipmentRequest {
        private String poundNo;
        private String applyNumber;
        private String truckNumber;
        private String customerName;
        private String goodsName;
        private Double netweight;
        private Double tareweight;
        private String tareTime;
        private String completeTime;
    }

}
