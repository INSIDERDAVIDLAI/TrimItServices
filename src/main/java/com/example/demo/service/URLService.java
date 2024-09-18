package com.example.demo.service;

import com.google.zxing.WriterException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.mongodb.client.model.Filters.eq;

@Service
public class URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLService.class);

    private final MongoCollection<Document> collection;
    private final Base62ShortenService base62ShortenService;

    @Autowired
    public URLService(MongoDatabase mongoDatabase, Base62ShortenService base62ShortenService) {
        this.collection = mongoDatabase.getCollection("URLMapping");
        this.base62ShortenService = base62ShortenService;
    }

    public String getLongURL(String shortURL) {
        Document doc = collection.find(eq("shortURL", shortURL)).first();
        return doc != null ? doc.getString("longURL") : null;
    }

    public void saveURLMapping(String longURL, String shortURL, String generatedBy) throws IOException, WriterException {
        // Check if the longURL already exists in the database
        Document existingDoc = collection.find(eq("longURL", longURL)).first();
        if (existingDoc != null) {
            logger.info("URL already exists: {}", longURL);
            return;
        }

        // Save longURL, shortURL, and generatedBy to the URLMapping collection
        Document doc = new Document("longURL", longURL)
                .append("shortURL", shortURL)
                .append("generatedBy", generatedBy);
        collection.insertOne(doc);
        logger.info("URL mapping saved: longURL={}, shortURL={}, generatedBy={}", longURL, shortURL, generatedBy);
    }

    public boolean existsByLongURL(String longURL) {
        logger.info("Checking existence of longURL: {}", longURL);
        Document doc = collection.find(eq("longURL", longURL)).first();
        boolean exists = doc != null;
        logger.info("Existence check result for longURL {}: {}", longURL, exists);
        return exists;
    }
}