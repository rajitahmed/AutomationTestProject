package com.rajit.AutomationTestProject.model;

import lombok.Getter;

@Getter
public class CustomerRequest {
    private String username;

    public CustomerRequest(String username) {
        this.username = username;
    }

}
