package com.shield.chepaipark.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "SameBarriarCard")
@Data
public class SameBarriarCard implements Serializable {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CardNo")
    private String cardNo;

    @Column(name = "LastTime")
    private ZonedDateTime lastTime;

    @Column(name = "CreateTime")
    private ZonedDateTime createTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SameBarriarCard that = (SameBarriarCard) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
