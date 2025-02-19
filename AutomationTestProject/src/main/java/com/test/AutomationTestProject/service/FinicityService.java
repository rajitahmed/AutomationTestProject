package com.test.AutomationTestProject.service;



import com.test.AutomationTestProject.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class FinicityService {
    private final WebClient webClient;

    @Value("${finicity.partnerId}")
    private String partnerId;

    @Value("${finicity.partnerSecret}")
    private String partnerSecret;

    private volatile String appToken;
    private volatile String customerId;
    private volatile String generatedLink;

    public FinicityService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> authenticate() {
        log.info("🔄 Starting authentication...");

        AuthenticationRequest request = new AuthenticationRequest(partnerId, partnerSecret);

        return webClient.post()
                .uri("/aggregation/v2/partners/authentication")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthenticationResponse.class)
                .map(response -> {
                    this.appToken = response.getToken();
                    log.info("✅ Authentication Successful: Token Acquired -> {}", this.appToken);
                    return "✅ Authentication Successful: Token Acquired";
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("❌ Authentication Failed: {}", ex.getResponseBodyAsString());
                    return Mono.just("❌ Authentication Failed: " + ex.getResponseBodyAsString());
                });
    }

    public Mono<String> createTestCustomer() {
        if (appToken == null) {
            log.error("❌ Cannot create customer: appToken is NULL!");
            return Mono.just("❌ Cannot create customer: Missing appToken!");
        }

        // Generate a unique username to avoid duplicate errors
        String uniqueUsername = "TESTER_" + UUID.randomUUID().toString().substring(0, 8);

        log.info("🔄 Creating test customer with username: {}", uniqueUsername);

        CustomerRequest request = new CustomerRequest(uniqueUsername);

        return webClient.post()
                .uri("/aggregation/v2/customers/testing")
                .header("Finicity-App-Token", appToken)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CustomerResponse.class)
                .map(response -> {
                    this.customerId = response.getId();
                    log.info("✅ Customer Created: ID = {}", this.customerId);
                    return "✅ Customer Created: ID = " + customerId;
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("❌ Customer Creation Failed: {}", ex.getResponseBodyAsString());
                    return Mono.just("❌ Customer Creation Failed: " + ex.getResponseBodyAsString());
                });
    }

    public Mono<String> generateConnectLink() {
        if (customerId == null) {
            log.error("❌ Cannot generate link: customerId is NULL!");
            return Mono.just("❌ Cannot generate link: Missing customerId!");
        }

        log.info("🔄 Generating Connect Link...");

        // Create request payload as JSON
        ConnectLinkRequest request = new ConnectLinkRequest(
                partnerId,
                customerId,
                "https://www.finicity.com/connect/" // Redirect URI
        );

        return webClient.post()
                .uri("/connect/v2/generate")
                .header("Finicity-App-Token", appToken)
                .bodyValue(request) // Send JSON payload
                .retrieve()
                .bodyToMono(ConnectLinkResponse.class)
                .map(response -> {
                    this.generatedLink = response.getLink();
                    log.info("✅ Connect Link Generated: {}", this.generatedLink);
                    return "✅ Connect Link Generated: " + generatedLink;
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("❌ Connect Link Generation Failed: {}", ex.getResponseBodyAsString());
                    return Mono.just("❌ Connect Link Generation Failed: " + ex.getResponseBodyAsString());
                });
    }
}
