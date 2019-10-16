package com.shield.service.event;

import org.springframework.context.ApplicationEvent;

public class PlanStatusChangeEvent extends ApplicationEvent {
    private Long applyId;
    private Integer beforeStatus;
    private Integer afterStatus;

    public PlanStatusChangeEvent(Object source, Long applyId, Integer beforeStatus, Integer afterStatus) {
        super(source);
        this.applyId = applyId;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
    }

    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public Integer getBeforeStatus() {
        return beforeStatus;
    }

    public void setBeforeStatus(Integer beforeStatus) {
        this.beforeStatus = beforeStatus;
    }

    public Integer getAfterStatus() {
        return afterStatus;
    }

    public void setAfterStatus(Integer afterStatus) {
        this.afterStatus = afterStatus;
    }
}
