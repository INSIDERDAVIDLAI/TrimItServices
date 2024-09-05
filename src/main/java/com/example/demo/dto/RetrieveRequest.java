package com.example.demo.dto;

public class RetrieveRequest {
    private String shortURL;
    private String type;

    // Getters and Setters
    public String getShortURL() {
        return shortURL;
    }

    public void setShortURL(String shortURL) {
        this.shortURL = shortURL;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

