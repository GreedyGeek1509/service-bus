package com.guduri.azure.module;

import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceBusModule {

    @Bean
    public QueueClient senderClient(@Value("${servicebus.sender-endpoint}") String endpoint) throws ServiceBusException, InterruptedException {
        return new QueueClient(new ConnectionStringBuilder(endpoint), ReceiveMode.PEEKLOCK);
    }


    @Bean
    public QueueClient listenerClient(@Value("${servicebus.listener-endpoint}") String endpoint) throws ServiceBusException, InterruptedException {
        return new QueueClient(new ConnectionStringBuilder(endpoint), ReceiveMode.PEEKLOCK);
    }
}
