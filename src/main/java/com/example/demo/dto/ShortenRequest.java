package com.example.demo.dto;

public class ShortenRequest {
    private String longURL;
    private String type;

    // Getters and Setters
    public String getLongURL() {
        return longURL;
    }

    public void setLongURL(String longURL) {
        this.longURL = longURL;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

