package com.example.demo.rest;

import com.example.demo.service.Base62ShortenService;
import com.example.demo.service.HashShorteneService;
import com.example.demo.service.QRCodeGeneratorService;
import com.example.demo.service.ShortenService;
import com.google.zxing.WriterException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/url")
public class URLShortenerController {

    private final Base62ShortenService base62ShortenService;
    private final HashShorteneService hashShorteneService;
    private final QRCodeGeneratorService qrCodeGeneratorService;

    public URLShortenerController(Base62ShortenService base62ShortenService,
                                  HashShorteneService hashShorteneService,
                                  QRCodeGeneratorService qrCodeGeneratorService) {
        this.base62ShortenService = base62ShortenService;
        this.hashShorteneService = hashShorteneService;
        this.qrCodeGeneratorService = qrCodeGeneratorService;
    }

    @PostMapping("/shorten/{longURL}/{type}")
    public String shortenURL(@PathVariable String longURL, @PathVariable String type) {
        ShortenService shortenService = getService(type);
        return shortenService.shortenURL(longURL);
    }

    @GetMapping("/retrieve/{shortURL}/{type}")
    public String getLongURL(@PathVariable String shortURL, @PathVariable String type) {
        ShortenService shortenService = getService(type);
        return shortenService.getLongURL(shortURL);
    }

    @GetMapping("/generateQR/{url}")
    public String generateQRCode(@PathVariable String url) throws WriterException, IOException {
        return qrCodeGeneratorService.generateQRCode(url, 200, 200);
    }

    private ShortenService getService(String type) {
        return switch (type) {
            case "base62to10" -> base62ShortenService;
            case "hash" -> hashShorteneService;
            default -> throw new IllegalArgumentException("Unknown URL shortener type: " + type);
        };
    }
}