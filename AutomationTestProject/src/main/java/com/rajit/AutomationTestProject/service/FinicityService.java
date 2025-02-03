package com.rajit.AutomationTestProject.service;

import com.rajit.AutomationTestProject.model.AuthenticationRequest;
import com.rajit.AutomationTestProject.model.AuthenticationResponse;
import com.rajit.AutomationTestProject.model.CustomerRequest;
import com.rajit.AutomationTestProject.model.CustomerResponse;
import com.rajit.AutomationTestProject.model.ConnectLinkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class FinicityService {
    private final WebClient webClient;

    @Value("${finicity.partnerId}")
    private String partnerId;

    @Value("${finicity.partnerSecret}")
    private String partnerSecret;

    private String appToken;
    private String customerId;
    private String generatedLink;

    public FinicityService(WebClient webClient) {
        this.webClient = webClient;
    }

    // Authenticate and get token
    public Mono<String> authenticate() {
        AuthenticationRequest request = new AuthenticationRequest(partnerId, partnerSecret);

        return webClient.post()
                .uri("/aggregation/v2/partners/authentication")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthenticationResponse.class)
                .map(response -> {
                    this.appToken = response.getToken();
                    return "✅ Authentication Successful: Token Acquired";
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("❌ Authentication Failed: {}", ex.getResponseBodyAsString());
                    return Mono.just("❌ Authentication Failed: " + ex.getResponseBodyAsString());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("❌ Unexpected Error during Authentication: {}", ex.getMessage());
                    return Mono.just("❌ Unexpected Error during Authentication: " + ex.getMessage());
                });
    }

    // Create a test customer
    public Mono<String> createTestCustomer() {
        CustomerRequest request = new CustomerRequest("TESTER");

        return webClient.post()
                .uri("/aggregation/v2/customers/testing")
                .header("Finicity-App-Token", appToken)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CustomerResponse.class)
                .map(response -> {
                    this.customerId = response.getId();
                    return "✅ Customer Created: ID = " + customerId;
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("❌ Customer Creation Failed: {}", ex.getResponseBodyAsString());
                    return Mono.just("❌ Customer Creation Failed: " + ex.getResponseBodyAsString());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("❌ Unexpected Error during Customer Creation: {}", ex.getMessage());
                    return Mono.just("❌ Unexpected Error during Customer Creation: " + ex.getMessage());
                });
    }

    // Generate Connect Link
    public Mono<String> generateConnectLink() {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/connect/v2/generate")
                        .queryParam("partnerId", partnerId)
                        .queryParam("customerId", customerId)
                        .build())
                .header("Finicity-App-Token", appToken)
                .retrieve()
                .bodyToMono(ConnectLinkResponse.class)
                .map(response -> {
                    this.generatedLink = response.getLink();
                    return "✅ Connect Link Generated: " + generatedLink;
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("❌ Connect Link Generation Failed: {}", ex.getResponseBodyAsString());
                    return Mono.just("❌ Connect Link Generation Failed: " + ex.getResponseBodyAsString());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("❌ Unexpected Error during Connect Link Generation: {}", ex.getMessage());
                    return Mono.just("❌ Unexpected Error during Connect Link Generation: " + ex.getMessage());
                });
    }
}
