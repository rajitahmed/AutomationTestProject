package com.test.AutomationTestProject.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConnectLinkRequest {
    private String partnerId;
    private String customerId;
    private String redirectUri;
}

