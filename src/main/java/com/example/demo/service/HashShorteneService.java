package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
public class HashShorteneService implements ShortenService {
    private final Map<String, String> urlMap = new HashMap<>();

    @Override
    public String shortenURL(String longURL) {
        String hash = encode(longURL.hashCode()).substring(0, 7);
        urlMap.put(hash, longURL);
        return "http://tiny.url/" + hash;
    }

    @Override
    public String getLongURL(String shortURL) {
        String hash = shortURL.replace("http://tiny.url/", "");
        return urlMap.get(hash);
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