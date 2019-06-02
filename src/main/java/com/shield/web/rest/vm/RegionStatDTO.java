package com.shield.web.rest.vm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.service.dto.RegionDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RegionStatDTO {
    private String day;

    private RegionStatData data = new RegionStatData();

    @Data
    public static class RegionStatData {
        List<RegionStatItem> regions = Lists.newArrayList();
    }

    @Data
    public static class RegionStatItem {
        private RegionDTO region;
//        private Map<AppointmentStatus, Integer> status = Maps.newHashMap();
    }
}
