package com.shield.chepaipark.domain;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "CardValidDateRange")
@Data
public class CardValidDateRange implements Serializable {
    @Id
    @Column(name = "RangeID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rangeId;

    @Column(name = "CardNo")
    private String cardNo;

    @Column(name = "StartDate")
    private ZonedDateTime startDate;

    @Column(name = "EndDate")
    private ZonedDateTime endDate;

    @Column(name = "CreateTime")
    private ZonedDateTime createTime;

    @Column(name = "Field1")
    private String field1;

    @Column(name = "Field2")
    private String field2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardValidDateRange that = (CardValidDateRange) o;
        return rangeId.equals(that.rangeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rangeId);
    }
}
