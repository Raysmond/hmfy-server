package com.shield.service.mapper;

import com.shield.domain.*;
import com.shield.service.dto.RegionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Region} and its DTO {@link RegionDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface RegionMapper extends EntityMapper<RegionDTO, Region> {


    @Mapping(target = "drivers", ignore = true)
    @Mapping(target = "remainQuota", ignore = true)
    @Mapping(target = "nextQuotaWaitTime", ignore = true)
    @Mapping(target = "nextQuotaNumber", ignore = true)
    @Mapping(target = "statusStart", ignore = true)
    @Mapping(target = "statusEnter", ignore = true)
    @Mapping(target = "userInWaitingList", ignore = true)
    @Mapping(target = "statusWaitBeforeUser", ignore = true)
    @Mapping(target = "statusWait", ignore = true)
    @Mapping(target = "waitTime", ignore = true)
    @Mapping(target = "statusStartCheck", ignore = true)
    RegionDTO toDto(Region region);

    default Region fromId(Long id) {
        if (id == null) {
            return null;
        }
        Region region = new Region();
        region.setId(id);
        return region;
    }
}
