package com.tolpp.sample;

import com.tolpp.sample.service.KafkaService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;

@SpringBootTest
class KafkaServiceTest {

    @Value("${resilience4j.circuitbreaker.instances.kafka.slowCallDurationThreshold}")
    private int slowCallDurationThreshold;

    @Value("${resilience4j.circuitbreaker.instances.kafka.slowCallRateThreshold}")
    private int slowCallRateThreshold;

    @Autowired
    private KafkaService kafkaService;

    @Test
    void whenTimeoutDurationExceededExpectCircuitBreakerActivatedAndNoExceptionThrown() throws Exception {
        int upToMillis = 400;
        int expectedMinFailCount = upToMillis - slowCallDurationThreshold - slowCallRateThreshold;
        int failCount = 0;
        for(int i = 0; i < upToMillis; i++) {
            String result = kafkaService.sendMessageDelayed(String.format("i=%d expectCircuitBreakerActivatedWhenTimeoutDurationExceeded", i), i);
            if("NOK".equals(result)) {
                failCount++;
            }
        }
        Assertions.assertTrue(failCount > expectedMinFailCount, "Fails less then expected");
    }

}
