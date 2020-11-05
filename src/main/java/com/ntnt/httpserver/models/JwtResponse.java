package com.ntnt.httpserver.models;

public class JwtResponse {
    private String token;
    private String additionalData;

    public JwtResponse() {
    }

    public JwtResponse(String token, String additionalData) {
        this.token = token;
        this.additionalData = additionalData;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }
}
