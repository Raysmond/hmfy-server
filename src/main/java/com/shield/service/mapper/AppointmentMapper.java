package com.shield.service.mapper;

import com.shield.domain.*;
import com.shield.service.dto.AppointmentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Appointment} and its DTO {@link AppointmentDTO}.
 */
@Mapper(componentModel = "spring", uses = {RegionMapper.class, UserMapper.class})
public interface AppointmentMapper extends EntityMapper<AppointmentDTO, Appointment> {

    @Mapping(source = "region.id", target = "regionId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "userLogin")
    AppointmentDTO toDto(Appointment appointment);

    @Mapping(source = "regionId", target = "region")
    @Mapping(source = "userId", target = "user")
    Appointment toEntity(AppointmentDTO appointmentDTO);

    default Appointment fromId(Long id) {
        if (id == null) {
            return null;
        }
        Appointment appointment = new Appointment();
        appointment.setId(id);
        return appointment;
    }
}
