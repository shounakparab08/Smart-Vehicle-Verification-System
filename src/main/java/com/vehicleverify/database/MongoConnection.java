package com.vehicleverify.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {
    private static MongoConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    private boolean connected = false;

    private MongoConnection() {
        String uri = System.getenv("MONGODB_URI");
        if (uri == null || uri.isEmpty()) {
            uri = "mongodb://localhost:27017";
            System.out.println("MONGODB_URI not set. Using local: " + uri);
        }
        
        try {
            com.mongodb.ConnectionString connString = new com.mongodb.ConnectionString(uri);
            com.mongodb.MongoClientSettings settings = com.mongodb.MongoClientSettings.builder()
                .applyConnectionString(connString)
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
                           .readTimeout(3, java.util.concurrent.TimeUnit.SECONDS))
                .applyToClusterSettings(builder -> 
                    builder.serverSelectionTimeout(3, java.util.concurrent.TimeUnit.SECONDS))
                .build();
            
            mongoClient = MongoClients.create(settings);
            String dbName = System.getenv("MONGODB_DB");
            if (dbName == null || dbName.isEmpty()) dbName = "vehicleDB";
            database = mongoClient.getDatabase(dbName);
            
            // Try a quick ping to verify connection
            database.runCommand(new org.bson.Document("ping", 1));
            connected = true;
            System.out.println("Successfully connected to MongoDB.");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to connect to MongoDB: " + e.getMessage());
            connected = false;
            // Use a fallback client with strict timeouts to avoid hanging the app
            try {
                com.mongodb.MongoClientSettings fallbackSettings = com.mongodb.MongoClientSettings.builder()
                    .applyConnectionString(new com.mongodb.ConnectionString("mongodb://localhost:27017"))
                    .applyToSocketSettings(builder -> 
                        builder.connectTimeout(1, java.util.concurrent.TimeUnit.SECONDS)
                               .readTimeout(1, java.util.concurrent.TimeUnit.SECONDS))
                    .applyToClusterSettings(builder -> 
                        builder.serverSelectionTimeout(1, java.util.concurrent.TimeUnit.SECONDS))
                    .build();
                mongoClient = MongoClients.create(fallbackSettings);
                database = mongoClient.getDatabase("vehicleDB");
            } catch (Exception ex) {
                mongoClient = null;
                database = null;
            }
        }
    }

    public static synchronized MongoConnection getInstance() {
        if (instance == null) instance = new MongoConnection();
        return instance;
    }

    public boolean isConnected() { return connected; }
    public MongoDatabase getDatabase() { return database; }

    public void close() { if (mongoClient != null) mongoClient.close(); }
}
