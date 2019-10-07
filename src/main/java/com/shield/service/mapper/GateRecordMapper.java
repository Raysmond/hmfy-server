package com.shield.service.mapper;

import com.shield.domain.*;
import com.shield.service.dto.GateRecordDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link GateRecord} and its DTO {@link GateRecordDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface GateRecordMapper extends EntityMapper<GateRecordDTO, GateRecord> {



    default GateRecord fromId(Long id) {
        if (id == null) {
            return null;
        }
        GateRecord gateRecord = new GateRecord();
        gateRecord.setId(id);
        return gateRecord;
    }
}
