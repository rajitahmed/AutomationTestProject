package com.rajit.AutomationTestProject.controller;

import com.rajit.AutomationTestProject.service.FinicityService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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
        return Flux.concat(
                finicityService.authenticate().delayElement(Duration.ofSeconds(1)),
                finicityService.createTestCustomer().delayElement(Duration.ofSeconds(1)),
                finicityService.generateConnectLink().delayElement(Duration.ofSeconds(1))
        );
    }
}
