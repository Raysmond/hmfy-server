package com.shield.web.rest;

import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.service.UserService;
import com.shield.sqlserver.repository.VehDelivPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired
    private VehDelivPlanRepository vehDelivPlanRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CarWhiteListService carWhiteListService;

    @GetMapping("/")
    public String test() {
        carWhiteListService.delayPutSyncShipPlanIdQueue(109000L);
        return "ok";
    }

//    @GetMapping("/veh-plans")
//    public List<VehDelivPlan> getVehPlans() {
//        return vehDelivPlanRepository.findAll();
//    }
//
//    @GetMapping("/set-driver-passwords")
//    public String updateDriverPassword(@RequestParam Long startId) {
//        userService.changePasswordForAllDrivers(startId);
//        return "ok";
//    }

//    @GetMapping("/whitelist/register")
//    public String registerCarWhiteList(@RequestParam String truckNumber) {
//        carWhiteListService.testRegisterCarWhiteLis(truckNumber);
//        return "ok";
//    }
//
//    @GetMapping("/whitelist/delete")
//    public String deleteCarWhiteList(@RequestParam String truckNumber) {
//        carWhiteListService.deleteCarWhiteList(truckNumber);
//        return "ok";
//    }

//    @GetMapping("/socket")
//    public String testSocket() throws IOException, InterruptedException {
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

//        return "ok";
//    }

//    @GetMapping("/get-menu")
//    public String getMenu() throws WxErrorException {
//        WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();
//        config.setAppId("wxb862ec87afd34ff1"); // 设置微信公众号的appid
//        config.setSecret("b2b41c90b7e9399895a397f15f3602dd"); // 设置微信公众号的app corpSecret
////        config.setToken("..."); // 设置微信公众号的token
////        config.setAesKey("..."); // 设置微信公众号的EncodingAESKey
//
//        WxMpService wxService = new WxMpServiceImpl();
//        wxService.setWxMpConfigStorage(config);
//
//        WxMpMenu menu = wxService.getMenuService().menuGet();
//        System.out.println(menu.toJson());
//
//        WxMpMenu.WxMpConditionalMenu addMenu = new WxMpMenu.WxMpConditionalMenu();
//        addMenu.setMenuId("appointment");
//        addMenu.setButtons(Lists.newArrayList());
//        WxMenuButton button = new WxMenuButton();
//        button.setType("miniprogram");
//        button.setName("车辆取号");
//        button.setAppId("wx32a67eb90d6d98e9");
//        button.setUrl("https://dp.meowpapa.com");
//        button.setPagePath("pages/index/index");
//
//        WxMenu wxMenu = WxMenu.fromJson(menu.toJson());
//        System.out.println(wxMenu.toJson());
////        wxMenu.getButtons().add(button);
//        wxMenu.getButtons().set(1, button);
//
//        System.out.println(wxMenu.toJson());
//
//        return wxMenu.toJson();
//    }
//
//    //
//    @GetMapping("/add-menu")
//    public String addMenu() throws WxErrorException {
//        WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();
//        config.setAppId("wxb862ec87afd34ff1"); // 设置微信公众号的appid
//        config.setSecret("b2b41c90b7e9399895a397f15f3602dd"); // 设置微信公众号的app corpSecret
////        config.setToken("..."); // 设置微信公众号的token
////        config.setAesKey("..."); // 设置微信公众号的EncodingAESKey
//
//        WxMpService wxService = new WxMpServiceImpl();
//        wxService.setWxMpConfigStorage(config);
//
//        WxMpMenu menu = wxService.getMenuService().menuGet();
//        WxMpMenu.WxMpConditionalMenu addMenu = new WxMpMenu.WxMpConditionalMenu();
//
//        addMenu.setMenuId("appointment");
//        addMenu.setButtons(Lists.newArrayList());
//        WxMenuButton button = new WxMenuButton();
//        button.setType("miniprogram");
//        button.setName("车辆取号");
//        button.setAppId("wx32a67eb90d6d98e9");
//        button.setUrl("https://dp.meowpapa.com");
//        button.setPagePath("pages/index/index");
//
//        WxMenu wxMenu = WxMenu.fromJson(menu.toJson());
//        System.out.println(wxMenu.toJson());
////        wxMenu.getButtons().add(button);
//        wxMenu.getButtons().set(1, button);
//
//        System.out.println(wxMenu.toJson());
//
//        wxService.getMenuService().menuCreate(wxMenu);
//        return wxMenu.toJson();
//    }

}
