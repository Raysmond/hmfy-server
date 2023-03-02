package com.shield.web.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shield.service.dto.PlanDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
public class DeliverPlanReq {
    @NotBlank
    private String planStr;
    @NotBlank
    private String location;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private LocalDate workDay;
    @NotBlank
    private String stockName;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private ZonedDateTime loadingStartTime;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private ZonedDateTime loadingEndTime;
    @NotNull
    private Double weightSum;
    private String operator;
    private String operation;
    @NotBlank
    private String channel;
    private String opPosition;
    private String comment;

    public PlanDTO convertToPlanDto() {
        PlanDTO plan = new PlanDTO();
        plan.setPlanNumber(planStr);
        plan.setLocation(location);
        plan.setWorkDay(workDay);
        plan.setStockName(stockName);
        plan.setLoadingStartTime(loadingStartTime);
        plan.setLoadingEndTime(loadingEndTime);
        plan.setWeightSum(weightSum);
        plan.setOperation(operation);
        plan.setOpPosition(opPosition);
        plan.setOperator(operator);
        plan.setComment(comment);
        plan.setChannel(channel);
        return plan;
    }
}
