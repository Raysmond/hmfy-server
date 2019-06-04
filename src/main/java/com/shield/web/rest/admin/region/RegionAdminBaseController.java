package com.shield.web.rest.admin.region;


import com.shield.domain.Region;
import com.shield.domain.User;
import com.shield.service.UserService;
import com.shield.web.rest.errors.BadRequestAlertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/region-admin/api")
public class RegionAdminBaseController {
    @Autowired
    private UserService userService;

    protected Region requireGetManagerRegion(Long regionId) {
        User user = userService.getUserWithAuthorities().get();
        Region region = user.getRegion();
        if (region == null) {
            throw new BadRequestAlertException("未绑定管理区域", "userManagement", null);
        }
        if (null != regionId && !region.getId().equals(regionId)) {
            throw new BadRequestAlertException("只能管理指定的区域", "userManagement", null);
        }
        return region;
    }
}
