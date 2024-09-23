package com.example.demo.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.mongodb.client.model.Filters.eq;

@Service
public class HashShorteneService implements ShortenService {

    private final URLService urlService;

    @Autowired
    public HashShorteneService(URLService urlService) {
        this.urlService = urlService;
    }

    @Override
    public String shortenURL(String longURL) {
        // Check if the longURL already exists in the database
        if (urlService.existsByLongURL(longURL)) {
            return urlService.findByLongURL(longURL).getShortURL();
        }

        // Generate a new shortURL if the longURL does not exist
        String shortURL = "http://tiny.url/" + encode(longURL.hashCode()).substring(0, 7);
        return shortURL;
    }

    public String encode(long num) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(String.valueOf(num).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}