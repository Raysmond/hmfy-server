package com.shield.web.rest;

import com.shield.domain.Region;
import com.shield.repository.RegionRepository;
import com.shield.service.UserService;
import com.shield.sqlserver.domain.VehDelivPlan;
import com.shield.sqlserver.repository.VehDelivPlanRepository;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired
    private VehDelivPlanRepository vehDelivPlanRepository;

    @Autowired
    private UserService userService;

//    @GetMapping("/veh-plans")
//    public List<VehDelivPlan> getVehPlans() {
//        return vehDelivPlanRepository.findAll();
//    }

    @GetMapping("/socket")
    public String testSocket() throws IOException, InterruptedException {
        Socket socket = SocketFactory.getDefault().createSocket("localhost", 9981);
        socket.getOutputStream().write(("helloworld:" + Math.random() + "\r\n").getBytes());
        Thread.sleep(1000
        );
        socket.close();
        return "ok";
    }

    @GetMapping("/change-default-password")
    public String changedDefaultPassword() {
        String password = "bt!888";
        userService.changeSystemUserPassword(password);
        return "ok";
    }

    @Autowired
    RegionRepository regionRepository;

    @GetMapping("test")
    public String test() {
        List<Region> regions = regionRepository.findAll();
        Region region = regions.get(0);
        System.out.println(ZoneId.systemDefault());
        System.out.println(ZonedDateTime.now());
        System.out.println(ZonedDateTime.now().getZone());
        region.setUpdateTime(ZonedDateTime.now());
        regionRepository.save(region);
        return "ok";
    }
}
