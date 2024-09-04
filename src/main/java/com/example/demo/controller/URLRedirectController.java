package com.example.demo.controller;

import com.example.demo.service.URLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class URLRedirectController {

    private final URLService urlService;

    @Autowired
    public URLRedirectController(URLService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/http://tiny.url/{shortURL}")
    public void redirect(@PathVariable String shortURL, HttpServletResponse response) throws IOException {
        String longURL = urlService.getLongURL(shortURL);
        if (longURL != null) {
            response.sendRedirect(longURL);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}