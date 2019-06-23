package com.shield.service;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.messaging.Message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("parkingTcpService")
public class ParkingTcpService {

//    @Autowired
//    UnicastSendingMessageHandler unicastSendingMessageHandler;

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper objectMapper;

    @Data
    class AuthService {
        private String service;
        private String parkid;
        private String parkkey;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ServiceResponse {
        private String service;
        private Integer result_code;
        private String message;
    }

    public void receive(Message msg) {
        byte[] data = (byte[]) msg.getPayload();
        String dataStr = new String(data);
        log.info("Received TCP msg: {}", dataStr);


        ServiceResponse response = null;
        if (dataStr.contains("parkkey")) {
            try {
                AuthService auth = objectMapper.readValue(dataStr, AuthService.class);
                response = new ServiceResponse(auth.getService(), 0, "认证成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (null == response) {
            response = new ServiceResponse("", 0, "成功");
        }

        try {
            String payload = objectMapper.writeValueAsString(response);
            InetSocketAddress toAddress = (InetSocketAddress) msg.getHeaders().get(IpHeaders.PACKET_ADDRESS);
//            unicastSendingMessageHandler.handleMessage(MessageBuilder
//                .withPayload(payload)
//                .setHeader(IpHeaders.PACKET_ADDRESS, toAddress).build());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
