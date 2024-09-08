package com.example.demo.controller;


import com.example.demo.dto.ShortenRequest;
import com.example.demo.dto.RetrieveRequest;
import com.example.demo.service.QRCodeService;
import com.example.demo.service.Base62ShortenService;
import com.example.demo.service.HashShorteneService;
import com.example.demo.service.QRCodeGeneratorService;
import com.example.demo.service.ShortenService;
import com.example.demo.service.URLService;
import com.google.zxing.WriterException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/url")
public class URLShortenerController {

    private final Base62ShortenService base62ShortenService;
    private final HashShorteneService hashShorteneService;
    private final URLService urlService;


    public URLShortenerController(Base62ShortenService base62ShortenService,
                                  HashShorteneService hashShorteneService,
                                  QRCodeGeneratorService qrCodeGeneratorService,
                                  URLService urlService,
                                  QRCodeService qrCodeService) {
        this.base62ShortenService = base62ShortenService;
        this.hashShorteneService = hashShorteneService;
        this.urlService = urlService;

    }

    @PostMapping(value="/shorten/", consumes = "application/json")
    public String shortenURL(@RequestBody ShortenRequest request) throws IOException, WriterException {
        ShortenService shortenService = getService(request.getType());
        String shortURL = shortenService.shortenURL(request.getLongURL());
        if (urlService.existsByLongURL(request.getLongURL())) {
            return shortURL + "\n" + "This URL already exists in the database.";
        }

        urlService.saveURLMapping(request.getLongURL(), shortURL);
        return shortURL;
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
}