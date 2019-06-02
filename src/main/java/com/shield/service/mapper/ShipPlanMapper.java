package com.shield.service.mapper;

import com.shield.domain.*;
import com.shield.service.dto.ShipPlanDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link ShipPlan} and its DTO {@link ShipPlanDTO}.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ShipPlanMapper extends EntityMapper<ShipPlanDTO, ShipPlan> {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "userLogin")
    @Mapping(source = "toUser.id", target = "toUserId")
    @Mapping(source = "toUser.login", target = "toUserLogin")
    ShipPlanDTO toDto(ShipPlan shipPlan);

    @Mapping(source = "userId", target = "user")
    @Mapping(source = "toUserId", target = "toUser")
    ShipPlan toEntity(ShipPlanDTO shipPlanDTO);

    default ShipPlan fromId(Long id) {
        if (id == null) {
            return null;
        }
        ShipPlan shipPlan = new ShipPlan();
        shipPlan.setId(id);
        return shipPlan;
    }
}
