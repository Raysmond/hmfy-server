package com.shield.web.rest.vm;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class AppointmentStat {
    private List<String> regions = Lists.newArrayList();
    private String date;
    private List<AppointmentStatItem> data = Lists.newArrayList();

    @Data
    public static class AppointmentStatItem {
        private String region;
        private String hour;
        private Long available = 0L;
        private Long wait = 0L;
        private Long start = 0L;
        private Long enter = 0L;
        private Long leave = 0L;
        private Long cancel = 0L;
        private Long expired = 0L;
    }
}
