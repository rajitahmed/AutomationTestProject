package com.rajit.AutomationTestProject.model;

import lombok.Getter;

@Getter
public class AuthenticationRequest {
    private final String partnerId;
    private final String partnerSecret;

    public AuthenticationRequest(String partnerId, String partnerSecret) {
        this.partnerId = partnerId;
        this.partnerSecret = partnerSecret;
    }

}
