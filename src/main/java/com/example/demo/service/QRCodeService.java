package com.example.demo.service;

import com.example.demo.entity.QRCodeMapping;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

@Service
public class QRCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeService.class);
    private final MongoCollection<Document> qrCodeCollection;


    @Autowired
    public QRCodeService(MongoDatabase mongoDatabase) {
        this.qrCodeCollection = mongoDatabase.getCollection("QRCodeMapping");

    }

    public boolean existsByLongURL(String longURL) {
        Document doc = qrCodeCollection.find(eq("longURL", longURL)).first();
        return doc != null;
    }

    public void saveQRCode(String longURL, String qrCodeBase64, String generatedBy) {
        if (!existsByLongURL(longURL)) {
            Document doc = new Document("longURL", longURL)
                    .append("qrCode", qrCodeBase64).append("generatedBy", generatedBy);
            qrCodeCollection.insertOne(doc);
        }
    }

    public QRCodeMapping findByLongURL(String longURL) {
        try {
            Document doc = qrCodeCollection.find(eq("longURL", longURL)).first();
            if (doc != null) {
                QRCodeMapping qrCodeMapping = new QRCodeMapping();
                qrCodeMapping.setId(doc.getObjectId("_id").toString());
                qrCodeMapping.setUrl(doc.getString("longURL"));
                qrCodeMapping.setQrCodeBase64(doc.getString("qrCode"));
                return qrCodeMapping;
            }
        } catch (MongoException e) {
            logger.error("Error finding QR code for longURL: {}", longURL, e);
        }
        return null;
    }

    public void deleteQRCode(String id) {
        try {
            qrCodeCollection.deleteOne(eq("_id", new org.bson.types.ObjectId(id)));
        } catch (MongoException e) {
            logger.error("Error deleting QR code with id: {}", id, e);
        }
    }



    public List<QRCodeMapping> getQRCodeByUser(String generatedBy) {
        List<QRCodeMapping> qrCodeList = new ArrayList<>();
        try {
            qrCodeCollection.find(eq("generatedBy", generatedBy)).forEach(doc -> {
                QRCodeMapping qrCodeMapping = new QRCodeMapping();
                qrCodeMapping.setId(doc.getObjectId("_id").toString());
                qrCodeMapping.setUrl(doc.getString("longURL"));
                qrCodeMapping.setQrCodeBase64(doc.getString("qrCode"));
                qrCodeList.add(qrCodeMapping);
            });
        } catch (MongoException e) {
            logger.error("Error finding QR codes for user: {}", generatedBy, e);
        }
        System.out.print(qrCodeList);
        return qrCodeList;
    }

}