package com.guduri.azure.rest;

import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final QueueClient senderClient;

    private final QueueClient listenerClient;

    private final List<String> messages = new ArrayList<>();

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("Inside ping.");
        return new ResponseEntity<>("pong", HttpStatus.OK);
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(RequestEntity<String> requestEntity) {
        log.info("Inside sendMessage with RequestEntity : {}", requestEntity);
        try {
            log.info("Pushing message : {}, to the queue", requestEntity.getBody());
            senderClient.send(new Message(requestEntity.getBody().getBytes(StandardCharsets.UTF_8)));
        } catch (ServiceBusException | InterruptedException e) {
            log.error("Caught Exception while pushing message to Queue.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/listen")
    public ResponseEntity<List<String>> listen() {
        log.info("Inside listen.");
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PostConstruct
    public void init() throws ServiceBusException, InterruptedException {
        log.info("Inside init.");
        listenerClient.registerMessageHandler(new MessageHandler(messages), new MessageHandlerOptions());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Closing QueueClients");
                senderClient.close();
                listenerClient.close();
            } catch (ServiceBusException e) {
                log.error("Exception while closing QueueClient. ", e);
            }
        }));
    }

    @RequiredArgsConstructor
    static class MessageHandler implements IMessageHandler {
        private final List<String> messages;
        @Override
        public CompletableFuture<Void> onMessageAsync(IMessage iMessage) {
            Message message = (Message) iMessage;
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("Received message : {}", msg);
            messages.add(msg);
            return CompletableFuture.completedFuture(null);
        }
        @Override
        public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
            log.error("{} phase caught exception.", exceptionPhase, throwable);
        }
    }
}
