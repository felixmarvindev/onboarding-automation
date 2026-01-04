package com.onboarding.provisioning;

import com.onboarding.events.ErrorTriggerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableConfigurationProperties(ErrorTriggerProperties.class)
public class ProvisioningServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProvisioningServiceApplication.class, args);
    }
}
