package com.example.demo.service;

import com.example.demo.entity.QRCodeMapping;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

@Service
public class QRCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeService.class);
    private final MongoCollection<Document> qrCodeCollection;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public QRCodeService(MongoDatabase mongoDatabase, RedisTemplate<String, Object> redisTemplate) {
        this.qrCodeCollection = mongoDatabase.getCollection("QRCodeMapping");
        this.redisTemplate = redisTemplate;
    }

    public boolean existsByLongURL(String longURL) {
        return redisTemplate.hasKey(longURL) || qrCodeCollection.find(eq("longURL", longURL)).first() != null;
    }

    public void saveQRCode(String longURL, String qrCodeBase64, String generatedBy) {
        if (!existsByLongURL(longURL)) {
            Document doc = new Document("longURL", longURL)
                    .append("qrCode", qrCodeBase64).append("generatedBy", generatedBy);
            qrCodeCollection.insertOne(doc);
            redisTemplate.opsForValue().set(longURL, qrCodeBase64, 1, TimeUnit.DAYS);
        }
    }

    public QRCodeMapping findByLongURL(String longURL) {
        try {
            String cachedQRCode = (String) redisTemplate.opsForValue().get(longURL);
            if (cachedQRCode != null) {
                QRCodeMapping qrCodeMapping = new QRCodeMapping();
                qrCodeMapping.setUrl(longURL);
                qrCodeMapping.setQrCodeBase64(cachedQRCode);
                return qrCodeMapping;
            }

            Document doc = qrCodeCollection.find(eq("longURL", longURL)).first();
            if (doc != null) {
                QRCodeMapping qrCodeMapping = new QRCodeMapping();
                qrCodeMapping.setId(doc.getObjectId("_id").toString());
                qrCodeMapping.setUrl(doc.getString("longURL"));
                qrCodeMapping.setQrCodeBase64(doc.getString("qrCode"));
                redisTemplate.opsForValue().set(longURL, doc.getString("qrCode"), 1, TimeUnit.DAYS);
                redisTemplate.opsForValue().set(doc.getString("qrCode"), longURL, 1, TimeUnit.DAYS);
                return qrCodeMapping;
            }
        } catch (MongoException e) {
            logger.error("Error finding QR code for longURL: {}", longURL, e);
        }
        return null;
    }

    public void deleteQRCode(String id) {
        try {
            Document doc = qrCodeCollection.find(eq("_id", new org.bson.types.ObjectId(id))).first();
            if (doc != null) {
                qrCodeCollection.deleteOne(eq("_id", new org.bson.types.ObjectId(id)));
                redisTemplate.delete(doc.getString("longURL"));
            }
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
        return qrCodeList;
    }
}