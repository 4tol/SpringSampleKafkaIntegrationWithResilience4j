package com.tolpp.sample.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class KafkaService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Value("${app.kafka.default-topic}")
    private String topic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @CircuitBreaker(name = "kafka", fallbackMethod = "doNothing")
    public String sendMessageSuccess(String message) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(topic, message).get();
        return "OK";
    }

    @CircuitBreaker(name = "kafka", fallbackMethod = "sendMessageDelayedFallback")
    public String sendMessageDelayed(String message, int delayInMillis) throws ExecutionException, InterruptedException {
        Thread.sleep(delayInMillis);
        kafkaTemplate.send(topic, message).get();
        return "OK";
    }

    public String sendMessageDelayedFallback(String message, int delayInMillis, Throwable error){
        logger.info("Short-circuiting sendMessageDelayed calls. Message: " + message);
        return "NOK";
    }
}
