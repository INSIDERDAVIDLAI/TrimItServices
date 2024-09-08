package com.example.demo.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.mongodb.client.model.Filters.eq;

@Service
public class QRCodeService {

    private final MongoCollection<Document> qrCodeCollection;

    @Autowired
    public QRCodeService(MongoDatabase mongoDatabase) {
        this.qrCodeCollection = mongoDatabase.getCollection("QRCodeMapping");
    }

    public boolean existsByLongURL(String longURL) {
        Document doc = qrCodeCollection.find(eq("longURL", longURL)).first();
        return doc != null;
    }

    public void saveQRCode(String longURL, String qrCodeBase64) {
        if (!existsByLongURL(longURL)) {
            Document doc = new Document("longURL", longURL)
                    .append("qrCode", qrCodeBase64);
            qrCodeCollection.insertOne(doc);
        }
    }
}