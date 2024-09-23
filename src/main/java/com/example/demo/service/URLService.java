package com.example.demo.service;

import com.example.demo.entity.QRCodeMapping;
import com.example.demo.entity.URLMapping;
import com.google.zxing.WriterException;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.eq;

@Service
public class URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLService.class);

    private final MongoCollection<Document> urlCollection;

    @Autowired
    public URLService(MongoDatabase mongoDatabase) {
        this.urlCollection = mongoDatabase.getCollection("URLMapping");
    }

    public String getLongURL(String shortURL) {
        Document doc = urlCollection.find(eq("shortURL", shortURL)).first();
        return doc != null ? doc.getString("longURL") : null;
    }

    public void saveURLMapping(String longURL, String shortURL, String generatedBy, String type) throws IOException, WriterException {
        // Check if the longURL already exists in the database
        if (!existsByLongURL(longURL)) {
            // Save longURL, shortURL, generatedBy, and type to the URLMapping collection
            Document doc = new Document("longURL", longURL)
                    .append("shortURL", shortURL)
                    .append("generatedBy", generatedBy)
                    .append("type", type);
            urlCollection.insertOne(doc);
        }
    }

    public URLMapping findByLongURL(String longURL) {
        try {
            Document doc = urlCollection.find(eq("longURL", longURL)).first();
            if (doc != null) {
                URLMapping urlMapping = new URLMapping();
                urlMapping.setId(doc.getObjectId("_id").toString());
                urlMapping.setLongURL(doc.getString("longURL"));
                urlMapping.setShortURL(doc.getString("shortURL"));
                urlMapping.setGeneratedBy(doc.getString("generatedBy"));
                urlMapping.setType(doc.getString("type"));
                return urlMapping;
            }
        } catch (MongoException e) {
            logger.error("Error finding URL mapping for longURL: {}", longURL, e);
        }
        return null;
    }

    public boolean existsByLongURL(String longURL) {
        Document doc = urlCollection.find(eq("longURL", longURL)).first();
        return doc != null;
    }
    public void deleteURL(String id) {
        try {
            urlCollection.deleteOne(eq("_id", new org.bson.types.ObjectId(id)));
        } catch (MongoException e) {
            logger.error("Error deleting URL with id: {}", id, e);
        }
    }

    public List<URLMapping> getUrlsByUser(String username) {
        List<URLMapping> urlList = new ArrayList<>();
        try{
            urlCollection.find(eq("generatedBy", username)).forEach(doc -> {
                URLMapping urlMapping = new URLMapping();
                urlMapping.setId(doc.getObjectId("_id").toString());
                urlMapping.setLongURL(doc.getString("longURL"));
                urlMapping.setShortURL(doc.getString("shortURL"));
                urlMapping.setGeneratedBy(doc.getString("generatedBy"));
                urlMapping.setType(doc.getString("type"));
                urlList.add(urlMapping);
            });
        } catch (MongoException e) {
            logger.error("Error finding URLs for user: {}", username, e);
        }
        System.out.print(urlList);
        return urlList;
    }
}