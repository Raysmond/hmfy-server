package com.shield.web.rest.vm;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class WeightStat {
    // 日期 2019-10-20
    private String region;
    private String date;
    private List<String> companies = Lists.newArrayList();
    private List<CompanyWeightStat> data = Lists.newArrayList();

    @Data
    @Builder
    public static class CompanyWeightStat {
        private String name;
        private String company;
        private String productName;
        private Long count;
        private Double weight;
    }
}
