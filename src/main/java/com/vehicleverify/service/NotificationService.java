package com.vehicleverify.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.vehicleverify.model.Notification;
import com.vehicleverify.database.MongoConnection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

public class NotificationService {
    private final MongoCollection<Document> collection;
    private static final java.util.List<Notification> mockNotifications = new java.util.concurrent.CopyOnWriteArrayList<>();

    public NotificationService() {
        com.mongodb.client.MongoDatabase db = MongoConnection.getInstance().getDatabase();
        this.collection = (db != null) ? db.getCollection("notifications") : null;
    }

    public void sendNotification(Notification notification) {
        if (notification.getId() == null) notification.setId(UUID.randomUUID().toString());
        if (notification.getCreatedAt() == null) notification.setCreatedAt(Instant.now().toString());

        if (!MongoConnection.getInstance().isConnected()) {
            boolean exists = false;
            for (Notification n : mockNotifications) {
                if (n.getId().equalsIgnoreCase(notification.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                mockNotifications.add(notification);
            }
            return;
        }
        
        Document doc = new Document("id", notification.getId())
            .append("userId", notification.getUserId())
            .append("title", notification.getTitle())
            .append("message", notification.getMessage())
            .append("type", notification.getType())
            .append("createdAt", notification.getCreatedAt())
            .append("isRead", notification.isRead())
            .append("fineAmount", notification.getFineAmount())
            .append("reportId", notification.getReportId());
        
        collection.insertOne(doc);
    }

    public List<Notification> getNotificationsForUser(String userId) {
        if (!MongoConnection.getInstance().isConnected()) {
            List<Notification> list = new ArrayList<>();
            boolean hasWelcome = false;
            for (Notification n : mockNotifications) {
                if (n.getUserId().equalsIgnoreCase(userId)) {
                    list.add(n);
                    if ("NOTIF001".equals(n.getId())) {
                        hasWelcome = true;
                    }
                }
            }
            if (!hasWelcome) {
                Notification welcome = new Notification();
                welcome.setId("NOTIF001");
                welcome.setUserId(userId);
                welcome.setTitle("Welcome to VehicleVerify");
                welcome.setMessage("Your account is now active. You can report incidents and track vehicle status.");
                welcome.setType("INFO");
                welcome.setCreatedAt(Instant.now().toString());
                welcome.setRead(false);
                mockNotifications.add(welcome);
                list.add(welcome);
            }
            return list;
        }
        List<Notification> list = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("userId", userId))) {
            list.add(docToNotification(doc));
        }
        return list;
    }

    public void markAsRead(String notificationId) {
        if (!MongoConnection.getInstance().isConnected()) {
            for (Notification n : mockNotifications) {
                if (n.getId().equalsIgnoreCase(notificationId)) {
                    n.setRead(true);
                    break;
                }
            }
            return;
        }
        collection.updateOne(Filters.eq("id", notificationId), new Document("$set", new Document("isRead", true)));
    }

    private Notification docToNotification(Document doc) {
        if (doc == null) return null;
        Notification n = new Notification();
        n.setId(doc.getString("id"));
        n.setUserId(doc.getString("userId"));
        n.setTitle(doc.getString("title"));
        n.setMessage(doc.getString("message"));
        n.setType(doc.getString("type"));
        n.setCreatedAt(doc.getString("createdAt"));
        n.setRead(doc.getBoolean("isRead", false));
        Object fineObj = doc.get("fineAmount");
        n.setFineAmount(fineObj instanceof Number ? ((Number) fineObj).doubleValue() : 0.0);
        n.setReportId(doc.getString("reportId"));
        return n;
    }
}
