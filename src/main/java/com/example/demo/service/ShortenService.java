package com.example.demo.service;

public interface ShortenService {


    String shortenURL(String longURL);

    String getLongURL(String shortURL);

}
