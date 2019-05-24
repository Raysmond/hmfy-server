package com.shield.service.mapper;

import com.shield.domain.*;
import com.shield.service.dto.CarDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Car} and its DTO {@link CarDTO}.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CarMapper extends EntityMapper<CarDTO, Car> {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "userLogin")
    CarDTO toDto(Car car);

    @Mapping(source = "userId", target = "user")
    Car toEntity(CarDTO carDTO);

    default Car fromId(Long id) {
        if (id == null) {
            return null;
        }
        Car car = new Car();
        car.setId(id);
        return car;
    }
}
