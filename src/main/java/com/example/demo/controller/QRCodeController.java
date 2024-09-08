package com.example.demo.controller;

import com.example.demo.dto.QRCodeRequest;
import com.example.demo.service.QRCodeGeneratorService;
import com.example.demo.service.QRCodeService;
import com.google.zxing.WriterException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/qr")
public class QRCodeController {

    private final QRCodeGeneratorService qrCodeGeneratorService;
    private final QRCodeService qrCodeService;

    public QRCodeController(QRCodeGeneratorService qrCodeGeneratorService, QRCodeService qrCodeService) {
        this.qrCodeGeneratorService = qrCodeGeneratorService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/generateQR/")
    public String generateQRCode(@RequestBody QRCodeRequest request) throws WriterException, IOException {
        String url = request.getUrl();
        String qrCodeBase64 = qrCodeGeneratorService.generateQRCode(url, 200, 200);
        qrCodeService.saveQRCode(url, qrCodeBase64);
        return qrCodeBase64;
    }
}