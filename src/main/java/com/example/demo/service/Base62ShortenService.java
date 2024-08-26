package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
//Test POST

@Service
public class Base62ShortenService implements ShortenService {
    private final Map<String, String> urlMap = new HashMap<>();
    private final AtomicLong counter = new AtomicLong();

    @Override
    public String shortenURL(String longURL) {
        String shortURL = "http://tiny.url/" + encode(counter.incrementAndGet());
        urlMap.put(shortURL, longURL);
        return shortURL;
    }

    @Override
    public String getLongURL(String shortURL) {
        return urlMap.get(shortURL);
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