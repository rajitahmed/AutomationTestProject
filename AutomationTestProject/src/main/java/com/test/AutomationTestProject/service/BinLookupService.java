package com.test.AutomationTestProject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.developer.oauth.OAuth;
import com.mastercard.developer.utils.AuthenticationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BinLookupService {
    private static final String SINGLE_LOOKUP_ENDPOINT = "/bin-ranges/account-searches";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${mastercard.keyFile}")
    private String keyFilePath;

    @Value("${mastercard.keyAlias}")
    private String keyAlias;

    @Value("${mastercard.keyPassword}")
    private String keyPassword;

    @Value("${mastercard.consumerKey}")
    private String consumerKey;
    
    @Value("${mastercard.binlookup.baseUrl}")
    private String binLookupBaseUrl;
    
    private volatile PrivateKey signingKey;
    private volatile List<Map<String, Object>> binLookupResults = new ArrayList<>();
    
    public BinLookupService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    public Mono<String> loadSigningKey() {
        log.info("Loading signing key from: {}", keyFilePath);
        
        return Mono.fromCallable(() -> {
            try {
                this.signingKey = AuthenticationUtils.loadSigningKey(
                        keyFilePath,
                        keyAlias,
                        keyPassword);
                
                log.info("Successfully loaded the signing key");
                return "✅ Successfully loaded the signing key";
            } catch (Exception e) {
                log.error("Failed to load signing key: {}", e.getMessage(), e);
                return "❌ Failed to load signing key: " + e.getMessage();
            }
        });
    }
    
    public Mono<String> performBinLookups() {
        if (signingKey == null) {
            log.error("Cannot perform BIN lookups: signingKey is NULL!");
            return Mono.just("❌ Cannot perform BIN lookups: Missing signing key!");
        }
        
        List<String> binRanges = List.of("99875393", "29214303", "66757806");
        binLookupResults.clear();
        
        log.info("Starting BIN Lookup for {} BIN ranges", binRanges.size());
        
        return Mono.fromCallable(() -> {
            StringBuilder responseBuilder = new StringBuilder();
            boolean hasErrors = false;
            
            for (String binRange : binRanges) {
                log.info("Looking up BIN range: {}", binRange);
                
                try {
                    Map<String, Object> result = performSingleLookup(binRange);
                    if (result != null) {
                        binLookupResults.add(result);
                        log.info("Successfully retrieved information for BIN range: {}", binRange);
                        responseBuilder.append("✅ Successfully retrieved information for BIN range: ")
                                      .append(binRange).append("\n");
                    } else {
                        hasErrors = true;
                        log.error("Received null response for BIN range: {}", binRange);
                        responseBuilder.append("❌ Error looking up BIN range ")
                                      .append(binRange)
                                      .append(": Received null response from API\n");
                    }
                } catch (Exception e) {
                    hasErrors = true;
                    log.error("Error looking up BIN range {}: {}", binRange, e.getMessage(), e);
                    responseBuilder.append("❌ Error looking up BIN range ")
                                  .append(binRange)
                                  .append(": ")
                                  .append(e.getMessage())
                                  .append("\n");
                }
            }
            
            if (hasErrors) {
                log.warn("BIN Lookup completed with some errors");
                responseBuilder.append("⚠️ BIN Lookup completed with some errors\n");
            } else {
                log.info("BIN Lookup completed successfully");
                responseBuilder.append("✅ BIN Lookup completed successfully\n");
            }
            
            if (!binLookupResults.isEmpty()) {
                String resultsJson = objectMapper.writeValueAsString(binLookupResults);
                log.info("BIN Lookup results: {}", resultsJson);
                responseBuilder.append("✅ BIN Lookup results: ").append(resultsJson);
            } else {
                log.warn("No successful BIN lookup results to display");
                responseBuilder.append("⚠️ No successful BIN lookup results to display");
            }
            
            return responseBuilder.toString();
        });
    }
    
    private Map<String, Object> performSingleLookup(String accountRange) throws Exception {
        URI uri = URI.create(binLookupBaseUrl + SINGLE_LOOKUP_ENDPOINT);
        String method = "POST";
        
        // Create request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("accountRange", accountRange);
        String payload = objectMapper.writeValueAsString(requestBody);
        
        log.debug("Request payload for BIN {}: {}", accountRange, payload);
        
        // Generate OAuth header
        String authHeader = OAuth.getAuthorizationHeader(
                uri,
                method,
                payload,
                StandardCharsets.UTF_8,
                consumerKey,
                signingKey);
        
        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);
        
        // Create request entity
        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
        
        try {
            // Use String as response type to handle any JSON response
            ResponseEntity<String> response = restTemplate.postForEntity(
                    uri,
                    requestEntity,
                    String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse the JSON string
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                // Check if the response is an array
                if (jsonNode.isArray() && jsonNode.size() > 0) {
                    // Take the first element if it's an array
                    JsonNode firstElement = jsonNode.get(0);
                    Map<String, Object> resultMap = objectMapper.convertValue(firstElement, Map.class);
                    
                    log.debug("Received successful array response for BIN {}, using first element: {}", 
                            accountRange, resultMap);
                    return resultMap;
                } else if (jsonNode.isObject()) {
                    // Handle as object if it's an object
                    Map<String, Object> resultMap = objectMapper.convertValue(jsonNode, Map.class);
                    
                    log.debug("Received successful object response for BIN {}: {}", 
                            accountRange, resultMap);
                    return resultMap;
                } else {
                    log.error("Unexpected JSON structure for BIN {}: {}", accountRange, jsonNode);
                    throw new Exception("Unexpected JSON structure in API response");
                }
            } else {
                log.error("Received non-success status code for BIN {}: {}", accountRange, response.getStatusCode());
                throw new Exception("API returned status code: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("RestClientException for BIN {}: {}", accountRange, e.getMessage(), e);
            throw new Exception("API request failed: " + e.getMessage(), e);
        }
    }
    
    // Getter for results
    public List<Map<String, Object>> getBinLookupResults() {
        return binLookupResults;
    }
}