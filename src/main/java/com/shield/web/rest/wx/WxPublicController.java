package com.shield.web.rest.wx;

import com.shield.service.RegionService;
import com.shield.service.dto.RegionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/wx/{appid}/public")
public class WxPublicController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RegionService regionService;

    @GetMapping("/regions")
    public List<RegionDTO> getRegions(@PathVariable String appid) {
        return regionService.findAll(PageRequest.of(0, 10000)).getContent();
    }

}
