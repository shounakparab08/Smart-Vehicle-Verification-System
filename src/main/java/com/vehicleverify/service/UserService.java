package com.vehicleverify.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.vehicleverify.database.MongoConnection;
import com.vehicleverify.model.User;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final MongoCollection<Document> collection;
    private static final java.util.Map<String, User> mockUsers = new java.util.concurrent.ConcurrentHashMap<>();

    static {
        mockUsers.put("admin_uid", new User("admin_uid", "System Admin", "admin@vehicleverify.com", "admin", "2024-01-01"));
        mockUsers.put("user-rohan", new User("user-rohan", "Rohan Sharma", "rohan.sharma88@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-priya", new User("user-priya", "Priya Patel", "priya.patel.vns@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-amit", new User("user-amit", "Amit Verma", "amitverma.official@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-sneha", new User("user-sneha", "Sneha Kulkarni", "sneha.kulkarni.92@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-vikram", new User("user-vikram", "Vikram Singh", "singh.vikram.pro@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-anjali", new User("user-anjali", "Anjali Gupta", "gupta.anjali.dev@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-rahul", new User("user-rahul", "Rahul Deshmukh", "rahul.deshmukh.main@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-pooja", new User("user-pooja", "Pooja Rao", "pooja.rao.v@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-sandeep", new User("user-sandeep", "Sandeep Mishra", "sandeep.mishra.work@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-neha", new User("user-neha", "Neha Joshi", "joshi.neha.27@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-aditya", new User("user-aditya", "Aditya Kadam", "aditya.kadam.vvs@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-meera", new User("user-meera", "Meera Nair", "nair.meera.life@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-kartik", new User("user-kartik", "Kartik Sawant", "kartik.sawant.85@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-shweta", new User("user-shweta", "Shweta Patil", "shwetapatil.v@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-sameer", new User("user-sameer", "Sameer Khan", "sameerkhan.786@gmail.com", "customer", "2024-01-15"));
        mockUsers.put("user-deepak", new User("user-deepak", "Deepak Kumar", "deepak.kumar.vvs@gmail.com", "customer", "2024-01-15"));
    }

    public UserService() {
        com.mongodb.client.MongoDatabase db = MongoConnection.getInstance().getDatabase();
        this.collection = (db != null) ? db.getCollection("users") : null;
    }

    public void createUser(User user) {
        if (!MongoConnection.getInstance().isConnected() || collection == null) {
            mockUsers.put(user.getUserId(), user);
            return;
        }
        collection.insertOne(new Document("userId", user.getUserId()).append("name", user.getName())
                .append("email", user.getEmail()).append("role", user.getRole()).append("createdAt", user.getCreatedAt()));
    }

    public User getUserById(String userId) {
        if (!MongoConnection.getInstance().isConnected() || collection == null) {
            return mockUsers.get(userId);
        }
        Document doc = collection.find(Filters.eq("userId", userId)).first();
        return (doc != null) ? documentToUser(doc) : null;
    }

    public User getUserByEmail(String email) {
        if (!MongoConnection.getInstance().isConnected() || collection == null) {
            for (User u : mockUsers.values()) {
                if (u.getEmail().equalsIgnoreCase(email)) return u;
            }
            return null;
        }
        Document doc = collection.find(Filters.eq("email", email)).first();
        return (doc != null) ? documentToUser(doc) : null;
    }

    public User getUserByName(String name) {
        if (!MongoConnection.getInstance().isConnected()) {
            for (User u : mockUsers.values()) {
                if (u.getName().equalsIgnoreCase(name)) return u;
            }
            return null;
        }
        return documentToUser(collection.find(Filters.eq("name", name)).first());
    }

    public void updateUser(User user) {
        if (!MongoConnection.getInstance().isConnected()) {
            mockUsers.put(user.getUserId(), user);
            return;
        }
        collection.updateOne(Filters.eq("userId", user.getUserId()),
                Updates.combine(
                    Updates.set("name", user.getName()), 
                     Updates.set("role", user.getRole()),
                    Updates.set("isFrozen", user.isFrozen()),
                    Updates.set("fineAmount", user.getFineAmount())
                ));
    }

    public void updateFrozenStatus(String userId, boolean isFrozen, double fineAmount) {
        if (!MongoConnection.getInstance().isConnected()) {
            User u = mockUsers.get(userId);
            if (u != null) {
                u.setFrozen(isFrozen);
                u.setFineAmount(fineAmount);
            }
            return;
        }
        collection.updateOne(Filters.eq("userId", userId),
                Updates.combine(Updates.set("isFrozen", isFrozen), Updates.set("fineAmount", fineAmount)));
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        if (!MongoConnection.getInstance().isConnected()) {
            for (User u : mockUsers.values()) {
                if (!u.getUserId().equals("admin_uid")) {
                    users.add(u);
                }
            }
            User admin = mockUsers.get("admin_uid");
            if (admin != null) {
                users.add(0, admin);
            }
            return users;
        }
        for (Document doc : collection.find()) users.add(documentToUser(doc));
        return users;
    }

    public void deleteUser(String userId) {
        if (!MongoConnection.getInstance().isConnected()) return;
        collection.deleteOne(Filters.eq("userId", userId));
    }

    private User documentToUser(Document doc) {
        if (doc == null) return null;
        User u = new User(doc.getString("userId"), doc.getString("name"), doc.getString("email"),
                doc.getString("role"), doc.getString("createdAt"));
        u.setFrozen(doc.getBoolean("isFrozen", false));
        Object fineObj = doc.get("fineAmount");
        u.setFineAmount(fineObj instanceof Number ? ((Number) fineObj).doubleValue() : 0.0);
        return u;
    }
}
