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
    //Next step to delete tiny url from parameter
    //Next step to consider how to handle if one link are shortened by two different methods and how to store it in db
    @GetMapping("/r/{shortURL}")
    public void redirect(@PathVariable String shortURL, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        String longURL = urlService.getLongURL("http://tiny.url/" + shortURL);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("Time taken: " + elapsedTime + " ms");
        if (longURL != null) {
            response.sendRedirect(longURL);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}