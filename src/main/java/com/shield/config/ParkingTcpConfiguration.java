package com.shield.config;

import com.shield.service.ParkingTcpHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.transformer.ObjectToStringTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;


@Configuration
@EnableIntegration
@IntegrationComponentScan
@Slf4j
public class ParkingTcpConfiguration {
    @Autowired
    private ParkingTcpHandlerService parkingTcpHandlerService;

    @Bean
    public TcpNetServerConnectionFactory cf() {
        TcpNetServerConnectionFactory connectionFactory = new TcpNetServerConnectionFactory(9981);
//        connectionFactory.setSerializer(ByteArrayCrLfSerializer.INSTANCE);
//        connectionFactory.setDeserializer(ByteArrayCrLfSerializer.INSTANCE);
        connectionFactory.setSerializer(CustomByteArrayCrlfSerializer.INSTANCE);
        connectionFactory.setDeserializer(CustomByteArrayCrlfSerializer.INSTANCE);
        return connectionFactory;
    }


    @Bean
    public TcpReceivingChannelAdapter inbound(AbstractServerConnectionFactory cf) {
        TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
        adapter.setConnectionFactory(cf);
        adapter.setOutputChannel(tcpIn());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "tcpOut")
    public TcpSendingMessageHandler outboundAdapter(AbstractServerConnectionFactory cf) {
        TcpSendingMessageHandler outbound = new TcpSendingMessageHandler();
        outbound.setConnectionFactory(cf);
        return outbound;
    }


    @Bean
    public MessageChannel tcpIn() {
        return new DirectChannel();
    }

    @Bean("tcpOut")
    public MessageChannel tcpOut() {
        return new DirectChannel();
    }

    @Transformer(inputChannel = "tcpIn", outputChannel = "serviceChannel")
    @Bean
    public ObjectToStringTransformer transformer() {
        return new ObjectToStringTransformer();
    }

    @ServiceActivator(inputChannel = "serviceChannel", outputChannel = "tcpOut")
    public String service(Message<String> msg) {
        return parkingTcpHandlerService.handle(msg);
    }
}
