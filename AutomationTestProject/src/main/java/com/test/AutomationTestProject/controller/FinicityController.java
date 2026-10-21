package com.test.AutomationTestProject.controller;

import com.test.AutomationTestProject.service.FinicityService;
import com.test.AutomationTestProject.service.SeleniumService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

@RestController
@RequestMapping("/api/finicity")
public class FinicityController {
    private final FinicityService finicityService;
    private final SeleniumService seleniumService;

    public FinicityController(FinicityService finicityService, SeleniumService seleniumService) {
        this.finicityService = finicityService;
        this.seleniumService = seleniumService;
    }

    @GetMapping("/execute")
    public Flux<String> executeAPICalls() {
        return Flux.concat(
                // Initial message
                Mono.just("🚀 Starting API calls sequence..."),
                
                // Authentication step
                Mono.just("⏳ Authenticating with Finicity API...")
                    .delayElement(Duration.ofMillis(500)),
                
                // Execute authentication and handle result
                finicityService.authenticate()
                    .flatMapMany(result -> {
                        if (result.startsWith("❌")) {
                            // Authentication failed, stop the sequence
                            return Flux.just(result, 
                                "⚠️ API call sequence failed at authentication step");
                        }
                        
                        // Authentication succeeded, continue with customer creation
                        return Flux.just(result)
                            .concatWith(Mono.just("⏳ Creating test customer...")
                                .delayElement(Duration.ofMillis(500)))
                            .concatWith(finicityService.createTestCustomer()
                                .flatMapMany(customerResult -> {
                                    if (customerResult.startsWith("❌")) {
                                        // Customer creation failed, stop the sequence
                                        return Flux.just(customerResult, 
                                            "⚠️ API call sequence failed at customer creation step");
                                    }
                                    
                                    // Customer creation succeeded, continue with connect link generation
                                    return Flux.just(customerResult)
                                        .concatWith(Mono.just("⏳ Generating connect link...")
                                            .delayElement(Duration.ofMillis(500)))
                                        .concatWith(finicityService.generateConnectLink()
                                            .flatMapMany(linkResult -> {
                                                if (linkResult.startsWith("❌")) {
                                                    // Link generation failed, stop the sequence
                                                    return Flux.just(linkResult, 
                                                        "⚠️ API call sequence failed at connect link generation step");
                                                }
                                                
                                                // Link generation succeeded, continue with Selenium automation
                                                return Flux.just(linkResult)
                                                    .concatWith(Mono.just("⏳ Starting Selenium automation to process the connect link...")
                                                        .delayElement(Duration.ofMillis(500)))
                                                    .concatWith(seleniumService.processConnectLink()
                                                        .flatMapMany(seleniumResult -> {
                                                            if (seleniumResult.startsWith("❌")) {
                                                                // Selenium automation failed
                                                                return Flux.just(seleniumResult, 
                                                                    "⚠️ API call sequence failed at Selenium automation step");
                                                            }
                                                            
                                                            // Selenium automation succeeded, continue with account refresh
                                                            return Flux.just(seleniumResult)
                                                                .concatWith(Mono.just("⏳ Refreshing customer accounts...")
                                                                    .delayElement(Duration.ofMillis(500)))
                                                                .concatWith(finicityService.refreshCustomerAccounts()
                                                                    .flatMapMany(refreshResult -> {
                                                                        if (refreshResult.startsWith("❌")) {
                                                                            // Account refresh failed
                                                                            return Flux.just(refreshResult, 
                                                                                "⚠️ API call sequence failed at account refresh step");
                                                                        }
                                                                        
                                                                        // Account refresh succeeded, complete the sequence
                                                                        return Flux.just(refreshResult, 
                                                                            "✅ API call sequence completed successfully!");
                                                                    }));
                                                        }));
                                            }));
                                }));
                    })
            )
            .onErrorResume(ex -> {
                String errorMessage = "❌ Unexpected error occurred: " + ex.getMessage();
                return Flux.just(
                    errorMessage,
                    "⚠️ API call sequence failed due to an unexpected error"
                );
            });
    }
    
    @GetMapping("/get-connect-link")
    public Mono<String> getConnectLink() {
        String link = finicityService.getGeneratedLink();
        if (link == null || link.isEmpty()) {
            return Mono.just("No connect link available. Please run the API sequence first.");
        }
        return Mono.just(link);
    }
    
    @GetMapping("/get-customer-id")
    public Mono<String> getCustomerId() {
        String customerId = finicityService.getCustomerId();
        if (customerId == null || customerId.isEmpty()) {
            return Mono.just("No customer ID available. Please run the API sequence first.");
        }
        return Mono.just(customerId);
    }
}
