package com.example.demo.controller;

import com.example.demo.dto.QRCodeRequest;
import com.example.demo.entity.QRCodeMapping;
import com.example.demo.service.QRCodeGeneratorService;
import com.example.demo.service.QRCodeService;
import com.google.zxing.WriterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.util.List;

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
    public String generateQRCode(@RequestBody QRCodeRequest request, Authentication authentication) throws WriterException, IOException {
        String url = request.getUrl();
        String username = authentication.getName();

        // Check if the QR code already exists
        if (qrCodeService.existsByLongURL(url)) {
            // Fetch the existing QR code from the database
            QRCodeMapping existingQRCode = qrCodeService.findByLongURL(url);
            return existingQRCode.getQrCodeBase64();
        }

        // Generate a new QR code
        String qrCodeBase64 = qrCodeGeneratorService.generateQRCode(url, 200, 200);
        qrCodeService.saveQRCode(url, qrCodeBase64, username);
        return qrCodeBase64;
    }

    @GetMapping("/myQR")
    public List<QRCodeMapping> getMyQRCode(Authentication authentication) {
        String username = authentication.getName();
        return qrCodeService.getQRCodeByUser(username);
    }


}