package com.shield.service.mapper;

import com.shield.domain.*;
import com.shield.service.dto.ParkMsgDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link ParkMsg} and its DTO {@link ParkMsgDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ParkMsgMapper extends EntityMapper<ParkMsgDTO, ParkMsg> {



    default ParkMsg fromId(Long id) {
        if (id == null) {
            return null;
        }
        ParkMsg parkMsg = new ParkMsg();
        parkMsg.setId(id);
        return parkMsg;
    }
}
