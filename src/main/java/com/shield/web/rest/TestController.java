package com.shield.web.rest;

import com.shield.service.UserService;
import com.shield.sqlserver.repository.VehDelivPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
//        Socket socket = SocketFactory.getDefault().createSocket("localhost", 9981);
//        socket.getOutputStream().write(("from remote:" + Math.random() + "\r\n").getBytes());
//        Thread.sleep(1000 );
//
//        Writer out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//        for (int i = 1; i < 3; ++i) {
//            String msg =  "hello" + i;
//
//            out.write(msg+"\r\n");
//            out.flush();
//            //System.out.print(msg+"\r\n");
//
//            System.out.println("Waiting for message ...");
//
//            StringBuffer str = new StringBuffer();
//            int c;
//            while ((c = in.read()) != -1) {
//                str.append((char) c);
//            }
//
//            String response = str.toString();
//            System.out.println("got message: " + response);
//
//            Thread.sleep(1000);
//        }
//
//        socket.close();

        return "ok";
    }

}
