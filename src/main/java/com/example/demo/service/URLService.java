package com.example.demo.service;

import com.example.demo.entity.URLMapping;
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
public class URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLService.class);
    private final MongoCollection<Document> urlCollection;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public URLService(MongoDatabase mongoDatabase, RedisTemplate<String, Object> redisTemplate) {
        this.urlCollection = mongoDatabase.getCollection("URLMapping");
        this.redisTemplate = redisTemplate;
    }

    public String getLongURL(String shortURL) {
        try {
            String cachedLongURL = (String) redisTemplate.opsForValue().get(shortURL);
            if (cachedLongURL != null) {
                return cachedLongURL;
            }

            Document doc = urlCollection.find(eq("shortURL", shortURL)).first();
            if (doc != null) {
                String longURL = doc.getString("longURL");
                redisTemplate.opsForValue().set(shortURL, longURL, 1, TimeUnit.DAYS);
                return longURL;
            }
        } catch (MongoException e) {
            logger.error("Error finding long URL for shortURL: {}", shortURL, e);
        }
        return null;
    }

    public boolean existsByLongURL(String longURL) {
        return redisTemplate.hasKey(longURL) || urlCollection.find(eq("longURL", longURL)).first() != null;
    }

    public void saveURLMapping(String longURL, String shortURL, String generatedBy, String type) {
        if (!existsByLongURL(longURL)) {
            Document doc = new Document("longURL", longURL)
                    .append("shortURL", shortURL).append("generatedBy", generatedBy).append("type", type);
            urlCollection.insertOne(doc);
            redisTemplate.opsForValue().set(longURL, shortURL, 1, TimeUnit.DAYS);
        }
    }

    public URLMapping findByLongURL(String longURL) {
        try {
            String cachedShortURL = (String) redisTemplate.opsForValue().get(longURL);
            if (cachedShortURL != null) {
                URLMapping urlMapping = new URLMapping();
                urlMapping.setLongURL(longURL);
                urlMapping.setShortURL(cachedShortURL);
                return urlMapping;
            }

            Document doc = urlCollection.find(eq("longURL", longURL)).first();
            if (doc != null) {
                URLMapping urlMapping = new URLMapping();
                urlMapping.setId(doc.getObjectId("_id").toString());
                urlMapping.setLongURL(doc.getString("longURL"));
                urlMapping.setShortURL(doc.getString("shortURL"));
                redisTemplate.opsForValue().set(longURL, doc.getString("shortURL"), 1, TimeUnit.DAYS);
                return urlMapping;
            }
        } catch (MongoException e) {
            logger.error("Error finding URL for longURL: {}", longURL, e);
        }
        return null;
    }

    public void deleteURL(String id) {
        try {
            Document doc = urlCollection.find(eq("_id", new org.bson.types.ObjectId(id))).first();
            if (doc != null) {
                urlCollection.deleteOne(eq("_id", new org.bson.types.ObjectId(id)));
                redisTemplate.delete(doc.getString("longURL"));
            }
        } catch (MongoException e) {
            logger.error("Error deleting URL with id: {}", id, e);
        }
    }

    public List<URLMapping> getUrlsByUser(String generatedBy) {
        List<URLMapping> urlList = new ArrayList<>();
        try {
            urlCollection.find(eq("generatedBy", generatedBy)).forEach(doc -> {
                URLMapping urlMapping = new URLMapping();
                urlMapping.setId(doc.getObjectId("_id").toString());
                urlMapping.setLongURL(doc.getString("longURL"));
                urlMapping.setShortURL(doc.getString("shortURL"));
                urlList.add(urlMapping);
            });
        } catch (MongoException e) {
            logger.error("Error finding URLs for user: {}", generatedBy, e);
        }
        return urlList;
    }
}