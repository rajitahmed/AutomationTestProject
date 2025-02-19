package com.test.AutomationTestProject.controller;

import com.test.AutomationTestProject.service.FinicityService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

@RestController
@RequestMapping("/api/finicity")
public class FinicityController {
    private final FinicityService finicityService;

    public FinicityController(FinicityService finicityService) {
        this.finicityService = finicityService;
    }

    @GetMapping("/execute")
    public Flux<String> executeAPICalls() {
        return Mono.just("🚀 Starting API calls...\n")
                .delayElement(Duration.ofSeconds(1))
                .flatMap(startMessage -> finicityService.authenticate()
                        .flatMap(token -> Mono.just("✅ Authentication Successful: Token Acquired -> " + token + "\n")
                                .delayElement(Duration.ofSeconds(1))
                                .then(finicityService.createTestCustomer()))
                        .flatMap(customerId -> Mono.just("✅ Customer Created: ID = " + customerId + "\n")
                                .delayElement(Duration.ofSeconds(1))
                                .then(finicityService.generateConnectLink()))
                        .flatMap(connectLink -> Mono.just("✅ Connect Link Generated: " + connectLink + "\n")
                                .delayElement(Duration.ofSeconds(1)))
                )
                .onErrorResume(ex -> Mono.just("❌ Internal Server Error: " + ex.getMessage()))
                .flux();
    }
}
