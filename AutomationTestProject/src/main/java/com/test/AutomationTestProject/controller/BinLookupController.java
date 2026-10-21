package com.test.AutomationTestProject.controller;

import com.test.AutomationTestProject.service.BinLookupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/binlookup")
public class BinLookupController {
    private final BinLookupService binLookupService;
    
    public BinLookupController(BinLookupService binLookupService) {
        this.binLookupService = binLookupService;
    }
    
    @GetMapping("/execute")
    public Flux<String> executeBinLookupFlow() {
        return Flux.concat(
                // Initial message
                Mono.just("🚀 Starting BIN Lookup flow..."),
                
                // Loading signing key step
                Mono.just("⏳ Loading Mastercard signing key...")
                    .delayElement(Duration.ofMillis(500)),
                
                // Execute BIN lookup and handle result
                binLookupService.loadSigningKey()
                    .flatMapMany(result -> {
                        if (result.startsWith("❌")) {
                            // Loading key failed, stop the sequence
                            return Flux.just(result, 
                                "⚠️ BIN Lookup flow failed at signing key loading step");
                        }
                        
                        // Key loading succeeded, continue with BIN lookups
                        return Flux.just(result)
                            .concatWith(Mono.just("⏳ Performing BIN lookups...")
                                .delayElement(Duration.ofMillis(500)))
                            .concatWith(binLookupService.performBinLookups()
                                .flatMapMany(lookupResult -> {
                                    if (lookupResult.startsWith("❌")) {
                                        // BIN lookup failed
                                        return Flux.just(lookupResult, 
                                            "⚠️ BIN Lookup flow failed during lookup step");
                                    }
                                    
                                    // BIN lookup succeeded
                                    return Flux.just(lookupResult, 
                                        "✅ BIN Lookup flow completed successfully!");
                                }));
                    })
            )
            .onErrorResume(ex -> {
                String errorMessage = "❌ Unexpected error occurred: " + ex.getMessage();
                return Flux.just(
                    errorMessage,
                    "⚠️ BIN Lookup flow failed due to an unexpected error"
                );
            });
    }
}