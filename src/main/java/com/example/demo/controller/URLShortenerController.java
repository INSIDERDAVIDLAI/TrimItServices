package com.example.demo.controller;

import com.example.demo.dto.ShortenRequest;
import com.example.demo.dto.RetrieveRequest;
import com.example.demo.entity.URLMapping;
import com.example.demo.service.Base62ShortenService;
import com.example.demo.service.HashShorteneService;
import com.example.demo.service.ShortenService;
import com.example.demo.service.URLService;
import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.io.IOException;
import java.util.List;



@RestController
@RequestMapping("/url")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class URLShortenerController {

    private static final Logger logger = LoggerFactory.getLogger(URLShortenerController.class);

    private final Base62ShortenService base62ShortenService;
    private final HashShorteneService hashShorteneService;
    private final URLService urlService;

    public URLShortenerController(Base62ShortenService base62ShortenService,
                                  HashShorteneService hashShorteneService,
                                  URLService urlService) {
        this.base62ShortenService = base62ShortenService;
        this.hashShorteneService = hashShorteneService;
        this.urlService = urlService;
    }

    @PostMapping(value="/shorten/", consumes = "application/json")
    public String shortenURL(@RequestBody ShortenRequest request, Authentication authentication) throws IOException, WriterException {
        if (authentication == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        logger.debug("Authentication object: {}", authentication);
        ShortenService shortenService = getService(request.getType());
        String url = request.getLongURL();
        String username = authentication.getName();
        logger.info("Authenticated user: {}", username);
        String shortURL = shortenService.shortenURL(request.getLongURL());

        if (urlService.existsByLongURL(url)) {
            URLMapping existingURL = urlService.findByLongURL(url);
            return existingURL.getShortURL() + "\n" + "This URL already exists in the database.";
        }

        urlService.saveURLMapping(url, shortURL, username, request.getType());
        return shortURL;
    }

    @GetMapping("/myUrls")
    public List<URLMapping> getMyUrls(Authentication authentication) {
        String username = authentication.getName();
        return urlService.getUrlsByUser(username);
    }

    @GetMapping("/retrieve/")
    public String getLongURL(@RequestBody RetrieveRequest request) {
        return urlService.getLongURL(request.getShortURL());
    }

    private ShortenService getService(String type) {
        return switch (type) {
            case "base62to10" -> base62ShortenService;
            case "hash" -> hashShorteneService;
            default -> throw new IllegalArgumentException("Unknown URL shortener type: " + type);
        };
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQRCode(@PathVariable String id) {
        urlService.deleteURL(id);
        return ResponseEntity.noContent().build();
    }
}