package com.shield.web.rest.api;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.shield.domain.enumeration.WeightSource;
import com.shield.service.ShipPlanQueryService;
import com.shield.service.ShipPlanService;
import com.shield.service.UserService;
import com.shield.service.dto.ShipPlanCriteria;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.web.rest.vm.ApiResponse;
import com.shield.web.rest.vm.ShipPlanVo;
import io.github.jhipster.service.filter.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 对外（沉重系统）的发运计划接口
 */
@RestController
@RequestMapping("/api/v1/plan")
public class ShipPlanApi {
    private final Logger log = LoggerFactory.getLogger(ShipPlanApi.class);


    @Autowired
    private ShipPlanService shipPlanService;

    @Autowired
    private ShipPlanQueryService shipPlanQueryService;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("redisLongTemplate")
    RedisTemplate<String, Long> redisLongTemplate;

    private static final int MAX_RETURN_RECORDS = 20;

    private static final String ENTITY_NAME = "shipPlan";


    @Data
    private class FindPlanQuery {
        @NotBlank(message = "参数 truckNumber (车牌号码) 不能为空")
        String truckNumber;

//        @NotBlank(message = "发运日期不能为空")
//        @Pattern(regexp = "\\d\\d\\d\\d-\\d\\d-\\d\\d", message = "发运日期格式需满足：yyyy-MM-dd，如 2022-09-01")
//        String deliverDate;

//        @Min(value = 1, message = "参数 planId （计划ID）需大于 0")
//        Long planId;

        @NotBlank(message = "参数 deliverPosition (发货区域) 不能为空")
        String deliverPosition;
    }

    /**
     * 获取有效的发运计划 （待提货），如果有多个，只返回时间较早的那个
     */
    @GetMapping("/find_undelivered_plan")
    public ResponseEntity<ApiResponse<List<ShipPlanVo>>> findValidPlan(@Valid FindPlanQuery query) {
        log.debug("REST request to get findPlans by truckNumber: {}", query);
        ShipPlanCriteria criteria = convertPlanCriteria(query);
        Page<ShipPlanDTO> page = shipPlanQueryService.findByCriteria(criteria, PageRequest.of(0, 1, Sort.Direction.ASC, "applyId"));
        for (ShipPlanDTO planDto : page.getContent()) {
            if (planDto.getAuditStatus().equals(1)
                && StringUtils.isBlank(planDto.getDestinationAddress())
                && StringUtils.isNotBlank(planDto.getUniqueQrcodeNumber())) {
                // TODO
            }

        }
        return ResponseEntity.ok()
            .body(ApiResponse.<List<ShipPlanVo>>builder()
                .status(HttpStatus.SC_OK)
                .data(page.getContent().stream().map(ShipPlanVo::new).collect(Collectors.toList())).build());
    }

    ShipPlanCriteria convertPlanCriteria(FindPlanQuery query) {
        ShipPlanCriteria criteria = new ShipPlanCriteria();
        StringFilter truckNumberFilter = new StringFilter();
        truckNumberFilter.setEquals(query.getTruckNumber());
        criteria.setTruckNumber(truckNumberFilter);

//        LocalDate t = LocalDate.parse(query.getDeliverDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        ZonedDateTimeFilter f = new ZonedDateTimeFilter();
//        f.setEquals(t.atStartOfDay(ZoneId.systemDefault()));
//        f.setGreaterThan(ZonedDateTime.now().minusHours(24));
        criteria.setDeliverTime(f);

//        if (query.planId != null) {
//            LongFilter planIdFilter = new LongFilter();
//            planIdFilter.setEquals(query.planId);
//            criteria.setId(planIdFilter);
//        }

        if (query.deliverPosition != null) {
            StringFilter deliverPositionFilter = new StringFilter();
            deliverPositionFilter.setEquals(query.deliverPosition);
            criteria.setDeliverPosition(deliverPositionFilter);
        }

        IntegerFilter statusFilter = new IntegerFilter();
        statusFilter.setEquals(1);
        criteria.setAuditStatus(statusFilter);


        BooleanFilter validFilter = new BooleanFilter();
        validFilter.setEquals(true);
        criteria.setValid(validFilter);

        return criteria;
    }

    /**
     * 车辆锁定（上报称重前）
     */
    @PostMapping("/lock_plan")
    public ResponseEntity<ApiResponse<ShipPlanVo>> lockPlan(@Valid @RequestBody LockPlanRequest request) {
        log.info("lockPlan, request: {}", request);
        ShipPlanDTO planDto = shipPlanService.findOne(request.planId)
            .orElseThrow(() -> new BadRequestAlertException("未找到计划 " + request.planId, ENTITY_NAME, ""));
        if (!Objects.equals(planDto.getAuditStatus(), 1)) {
            throw new BadRequestAlertException(String.format(
                "计划 %s 状态为%s，无法锁定",
                request.planId, planDto.getAuditStatus()), ENTITY_NAME, "");
        }
        if (!Objects.equals(request.getTruckNumber(), planDto.getTruckNumber())) {
            throw new BadRequestAlertException(String.format(
                "车牌号 %s 和计划上的不一致，无法锁定",
                request.truckNumber), ENTITY_NAME, "");
        }
        //TODO
//
        return ResponseEntity.ok()
            .body(ApiResponse.<ShipPlanVo>builder().status(HttpStatus.SC_OK).data(new ShipPlanVo(planDto)).build());
    }

    @Data
    public static class LockPlanRequest {
        @NotNull(message = "计划号 planId 不能为空")
        private Long planId;

        @NotBlank(message = "车牌号 truckNumber 不能为空")
        private String truckNumber;
    }


    /**
     * 上报称重数据
     *
     * @param request
     * @return
     */
    @PostMapping("/update_weight")
    public ResponseEntity<ApiResponse<ShipPlanVo>> updatePlanDelivery(@Valid @RequestBody UpdatePlanWeightRequest request) {
        log.info("updatePlanDelivery, request: {}", request);
        ShipPlanDTO planDto = shipPlanService.findOne(request.planId)
            .orElseThrow(() -> new BadRequestAlertException("未找到计划 " + request.planId, ENTITY_NAME, ""));
        if (!Objects.equals(planDto.getAuditStatus(), 1)) {
            throw new BadRequestAlertException(String.format(
                "计划 %s 状态为%s，无法再更新称重信息",
                request.planId, planDto.getAuditStatus()), ENTITY_NAME, "");
        }
        planDto.setNetWeight(request.netWeight);
        planDto.setTareWeight(request.tareWeight);
        planDto.setWeigherNo(request.weigherNo);
        planDto.setLoadingStartTime(request.loadingStartTime.withFixedOffsetZone());
        planDto.setLoadingEndTime(request.loadingEndTime.withFixedOffsetZone());
        planDto.setAuditStatus(3);
        planDto.setWeightCode(request.getWeightCode());

        if (StringUtils.isNotBlank(planDto.getWeightCode())
            && planDto.getWeightCode().length() > 8
            && planDto.getWeightCode().charAt(8) != '2') {
            // 系统标识位置，必须是2，如果不是，则改成2
            planDto.setWeightCode(planDto.getWeightCode().substring(0, 8) + "2" + planDto.getWeightCode().substring(9));
        }

        planDto.setTruckOn(request.getTruckOn());
        planDto.setTruckOnCname(request.getTruckOnCname());
        planDto.setUpdateTime(ZonedDateTime.now());
        planDto.setWeightSource(WeightSource.SANYI);
        shipPlanService.save(planDto);
        ShipPlanDTO updated = shipPlanService.findOne(request.planId).get();

        // 同步计划到 SQL_SERVER
//        redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, planDto.getId());
        try {
            // TODO
//            caitongPlanApi.endOfShipment(updated);
        } catch (Exception e) {
            log.error("updatePlanDelivery fail", e);
        }

        return ResponseEntity.ok()
            .body(ApiResponse.<ShipPlanVo>builder().status(HttpStatus.SC_OK).data(new ShipPlanVo(updated)).build());
    }

    @Data
    public static class UpdatePlanWeightRequest {
        @NotNull(message = "计划号 planId 不能为空")
        private Long planId;
        @NotNull(message = "净重 netWeight 不能为空")
        private Double netWeight;
        private Double tareWeight;
        private String weigherNo;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
        private ZonedDateTime loadingStartTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
        private ZonedDateTime loadingEndTime;
        /**
         * 磅单编号
         */
        @NotBlank(message = "磅单编号 weightCode 不能为空")
        private String weightCode;
        /**
         * 出库仓库代码
         */
        @NotBlank(message = "出库仓库代码 truckOn 不能为空")
        private String truckOn;
        /**
         * 出库仓库名称
         */
        @NotBlank(message = "出库仓库名称 truckOnCname 不能为空")
        private String truckOnCname;
    }


}
