package com.shield.web.rest;

import com.shield.service.UserService;
import com.shield.sqlserver.domain.VehDelivPlan;
import com.shield.sqlserver.repository.VehDelivPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.Socket;
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
}
