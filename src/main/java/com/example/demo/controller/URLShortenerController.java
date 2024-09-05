package com.example.demo.controller;

import com.example.demo.dto.ShortenRequest;
import com.example.demo.dto.RetrieveRequest;
import com.example.demo.dto.QRCodeRequest;
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
    private final QRCodeGeneratorService qrCodeGeneratorService;
    private final URLService urlService;

    public URLShortenerController(Base62ShortenService base62ShortenService,
                                  HashShorteneService hashShorteneService,
                                  QRCodeGeneratorService qrCodeGeneratorService,
                                  URLService urlService) {
        this.base62ShortenService = base62ShortenService;
        this.hashShorteneService = hashShorteneService;
        this.qrCodeGeneratorService = qrCodeGeneratorService;
        this.urlService = urlService;
    }

    @PostMapping("/shorten/")
    public String shortenURL(@RequestBody ShortenRequest request) {
    ShortenService shortenService = getService(request.getType());
    String shortURL = shortenService.shortenURL(request.getLongURL());
    urlService.saveURLMapping(request.getLongURL(), shortURL);
    return shortURL;
}

    @GetMapping("/retrieve/")
    public String getLongURL(@RequestBody RetrieveRequest request) {
        return urlService.getLongURL(request.getShortURL());
    }

    @GetMapping("/generateQR/")
    public String generateQRCode(@RequestBody QRCodeRequest request) throws WriterException, IOException {
        return qrCodeGeneratorService.generateQRCode(request.getUrl(), 200, 200);
    }

    private ShortenService getService(String type) {
        return switch (type) {
            case "base62to10" -> base62ShortenService;
            case "hash" -> hashShorteneService;
            default -> throw new IllegalArgumentException("Unknown URL shortener type: " + type);
        };
    }
}