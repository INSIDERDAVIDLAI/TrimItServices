package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Base62ShortenService implements ShortenService {

    private URLService urlService;

    @Autowired
    public void setUrlService(URLService urlService) {
        this.urlService = urlService;
    }

    @Override
    public String shortenURL(String longURL) {
        // Check if the longURL already exists in the database
        if (urlService.existsByLongURL(longURL)) {
            // Return the existing shortURL if the longURL already exists
            return urlService.findByLongURL(longURL).getShortURL();
        }

        // Generate a new shortURL if the longURL does not exist
        String shortURL = "http://tiny.url/" + encode(System.currentTimeMillis());
        return shortURL;
    }

    public String encode(long num) {
        char[] map = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder shortURL = new StringBuilder();
        while (num > 0) {
            shortURL.append(map[(int) (num % 62)]);
            num /= 62;
        }
        return shortURL.reverse().toString();
    }
}