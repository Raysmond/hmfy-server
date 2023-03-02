package com.shield.service.mapper;

import com.shield.domain.*;
import com.shield.service.dto.PlanDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Plan} and its DTO {@link PlanDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PlanMapper extends EntityMapper<PlanDTO, Plan> {



    default Plan fromId(Long id) {
        if (id == null) {
            return null;
        }
        Plan plan = new Plan();
        plan.setId(id);
        return plan;
    }
}
