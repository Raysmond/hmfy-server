package com.shield.domain;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * A ParkMsg.
 */
@Entity
@Table(name = "park_msg")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ParkMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 64)
    @Column(name = "parkid", length = 64, nullable = false)
    private String parkid;

    @NotNull
    @Size(max = 64)
    @Column(name = "service", length = 64, nullable = false)
    private String service;

    @NotNull
    @Column(name = "create_time", nullable = false)
    private ZonedDateTime createTime;

    @NotNull
    @Size(max = 4096)
    @Column(name = "body", length = 4096, nullable = false)
    private String body;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParkid() {
        return parkid;
    }

    public ParkMsg parkid(String parkid) {
        this.parkid = parkid;
        return this;
    }

    public void setParkid(String parkid) {
        this.parkid = parkid;
    }

    public String getService() {
        return service;
    }

    public ParkMsg service(String service) {
        this.service = service;
        return this;
    }

    public void setService(String service) {
        this.service = service;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public ParkMsg createTime(ZonedDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public String getBody() {
        return body;
    }

    public ParkMsg body(String body) {
        this.body = body;
        return this;
    }

    public void setBody(String body) {
        this.body = body;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParkMsg)) {
            return false;
        }
        return id != null && id.equals(((ParkMsg) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ParkMsg{" +
            "id=" + getId() +
            ", parkid='" + getParkid() + "'" +
            ", service='" + getService() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", body='" + getBody() + "'" +
            "}";
    }
}
