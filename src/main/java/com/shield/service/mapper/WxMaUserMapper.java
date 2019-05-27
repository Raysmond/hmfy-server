package com.shield.service.mapper;

import com.shield.domain.*;
import com.shield.service.dto.WxMaUserDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link WxMaUser} and its DTO {@link WxMaUserDTO}.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface WxMaUserMapper extends EntityMapper<WxMaUserDTO, WxMaUser> {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "userLogin")
    WxMaUserDTO toDto(WxMaUser wxMaUser);

    @Mapping(source = "userId", target = "user")
    WxMaUser toEntity(WxMaUserDTO wxMaUserDTO);

    default WxMaUser fromId(Long id) {
        if (id == null) {
            return null;
        }
        WxMaUser wxMaUser = new WxMaUser();
        wxMaUser.setId(id);
        return wxMaUser;
    }
}
