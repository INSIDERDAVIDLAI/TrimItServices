package com.example.demo.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.mongodb.client.model.Filters.eq;

@Service
public class Base62ShortenService implements ShortenService {

    private final MongoCollection<Document> collection;

    @Autowired
    public Base62ShortenService(MongoDatabase mongoDatabase) {
        this.collection = mongoDatabase.getCollection("URLMapping");
    }

    @Override
    public String shortenURL(String longURL) {
        // Check if the longURL already exists in the database
        Document existingDoc = collection.find(eq("longURL", longURL)).first();
        if (existingDoc != null) {
            // Return the existing shortURL if the longURL already exists
            return existingDoc.getString("shortURL");
        }

        // Generate a new shortURL if the longURL does not exist
        String shortURL = "http://tiny.url/" + encode(System.currentTimeMillis());
        Document doc = new Document("longURL", longURL)
                .append("shortURL", shortURL)
                .append("type", "base62to10");
        collection.insertOne(doc);
        return shortURL;
    }

    @Override
    public String getLongURL(String shortURL) {
        Document doc = collection.find(eq("shortURL", shortURL)).first();
        return doc != null ? doc.getString("longURL") : null;
    }

    public String encode(long num) {
        char[] map = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder shortURL = new StringBuilder();
        while (num > 0) {
            shortURL.append(map[(int) (num % 62)]);
            num /= 62;
        }
        return shortURL.reverse().toString();
    }
}