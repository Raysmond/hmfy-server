package com.shield.web.rest.api;

import com.shield.service.PlanQueryService;
import com.shield.service.PlanService;
import com.shield.service.dto.PlanCriteria;
import com.shield.service.dto.PlanDTO;
import com.shield.web.rest.api.dto.DeliverPlanReq;
import com.shield.web.rest.errors.BadRequestAlertException;
import com.shield.web.rest.vm.ApiResponse;
import io.github.jhipster.service.filter.StringFilter;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/plan")
public class PlanApi {
    @Autowired
    PlanService planService;

    @Autowired
    PlanQueryService planQueryService;

    /**
     * erp 系统推送工单到预约排队系统
     *
     * @param req
     * @return
     */
    @PostMapping("/deliver_plan")
    @Transactional
    public ResponseEntity<ApiResponse<PlanDTO>> deliverPlan(@Valid @RequestBody DeliverPlanReq req) {
        PlanDTO planDTO = req.convertToPlanDto();
        PlanCriteria criteria = new PlanCriteria();
        criteria.setPlanNumber(new StringFilter());
        criteria.getPlanNumber().setEquals(req.getPlanStr());
        if (planQueryService.countByCriteria(criteria) > 0) {
            throw new BadRequestAlertException(String.format("工单 %s 已存在", req.getPlanStr()), "plan", "");
        }
        PlanDTO created = planService.save(planDTO);
        return ResponseEntity.ok()
            .body(ApiResponse.<PlanDTO>builder().status(HttpStatus.SC_OK).data(created).build());
    }
}
