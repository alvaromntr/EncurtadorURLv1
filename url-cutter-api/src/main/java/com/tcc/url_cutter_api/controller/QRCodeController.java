package com.tcc.url_cutter_api.controller;

import com.tcc.url_cutter_api.utils.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qr")
public class QRCodeController {

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping(value = "/qrgenerator/{shortCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generate(@PathVariable String shortCode) {
        try {
            // Exemplo: monta a URL encurtada
            String shortUrl = baseUrl + shortCode;

            byte[] qrCode = QRCodeGenerator.generateQRCode(shortUrl, 300, 300);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrCode);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}