package com.example.demo.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.mongodb.client.model.Filters.eq;

@Service
public class URLService {

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

    public String saveURLMapping(String longURL, String type) {
        // Check if the longURL already exists in the database
        Document existingDoc = collection.find(eq("longURL", longURL)).first();
        if (existingDoc != null) {
            // Return the existing shortURL if the longURL already exists
            return existingDoc.getString("shortURL");
        }

        // Generate a new shortURL if the longURL does not exist
        String shortURL = base62ShortenService.shortenURL(longURL);
        Document doc = new Document("longURL", longURL)
                .append("shortURL", shortURL)
                .append("type", type);
        collection.insertOne(doc);
        return shortURL;
    }
}