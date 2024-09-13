package com.example.demo.service;

import com.google.zxing.WriterException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.mongodb.client.model.Filters.eq;

@Service
public class URLService {

    private final MongoCollection<Document> collection;
    private final Base62ShortenService base62ShortenService;
    private final QRCodeGeneratorService qrCodeGeneratorService;

    @Autowired
    public URLService(MongoDatabase mongoDatabase, Base62ShortenService base62ShortenService, QRCodeGeneratorService qrCodeGeneratorService) {
        this.collection = mongoDatabase.getCollection("URLMapping");
        this.base62ShortenService = base62ShortenService;
        this.qrCodeGeneratorService = qrCodeGeneratorService;
    }

    public String getLongURL(String shortURL) {
        Document doc = collection.find(eq("shortURL", shortURL)).first();
        return doc != null ? doc.getString("longURL") : null;
    }

    public void saveURLMapping(String longURL, String type, String generatedBy) throws IOException, WriterException {
        // Check if the longURL already exists in the database
        Document existingDoc = collection.find(eq("longURL", longURL)).first();
        if (existingDoc != null) {
            // Return the existing shortURL if the longURL already exists
            existingDoc.getString("shortURL");
            return;
        }

        // Generate a new shortURL if the longURL does not exist
        String shortURL = base62ShortenService.shortenURL(longURL);

        // Generate QR code
        String qrCodeBase64 = qrCodeGeneratorService.generateQRCode(longURL, 200, 200);

        // Save longURL, shortURL, qrCode, type, and generatedBy to the URLMapping collection
        Document doc = new Document("longURL", longURL)
                .append("shortURL", shortURL)
                .append("qrCode", qrCodeBase64)
                .append("type", type)
                .append("generatedBy", generatedBy);
        collection.insertOne(doc);

    }

    public boolean existsByLongURL(String longURL) {
        return collection.find(eq("longURL", longURL)).first() != null;
    }
}