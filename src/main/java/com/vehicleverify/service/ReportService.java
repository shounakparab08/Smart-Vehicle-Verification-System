package com.vehicleverify.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.vehicleverify.database.MongoConnection;
import com.vehicleverify.model.Report;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private final MongoCollection<Document> collection;
    private static final java.util.List<Report> mockReports = new java.util.concurrent.CopyOnWriteArrayList<>();

    static {
        mockReports.add(new Report("REP001", "MH12AB1234", "user_uid", "Traffic Violation", "Spotted jumping red light", "Pending", "2024-05-10T10:00:00Z"));
        mockReports.add(new Report("REP002", "KA01XY5678", "user_uid", "Illegal Parking", "Parked in no-parking zone", "Verified", "2024-05-11T14:30:00Z"));
    }

    public ReportService() {
        com.mongodb.client.MongoDatabase db = MongoConnection.getInstance().getDatabase();
        this.collection = (db != null) ? db.getCollection("reports") : null;
    }

    public void createReport(Report r) {
        if (!MongoConnection.getInstance().isConnected()) {
            boolean exists = false;
            for (Report report : mockReports) {
                if (report.getReportId().equalsIgnoreCase(r.getReportId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                mockReports.add(r);
            }
            return;
        }
        collection.insertOne(new Document("reportId", r.getReportId()).append("vehicleNo", r.getVehicleNo())
                .append("reportedBy", r.getReportedBy()).append("reason", r.getReason())
                .append("description", r.getDescription()).append("status", r.getStatus()).append("createdAt", r.getCreatedAt()));
    }

    public List<Report> getReportsByUser(String userId) {
        if (!MongoConnection.getInstance().isConnected()) {
            List<Report> list = new ArrayList<>();
            for (Report report : mockReports) {
                if ("user_uid".equals(report.getReportedBy()) || report.getReportedBy().equalsIgnoreCase(userId)) {
                    list.add(new Report(report.getReportId(), report.getVehicleNo(), userId, report.getReason(),
                            report.getDescription(), report.getStatus(), report.getCreatedAt()));
                }
            }
            return list;
        }
        List<Report> list = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("reportedBy", userId)).sort(Sorts.descending("createdAt")))
            list.add(docToReport(doc));
        return list;
    }

    public List<Report> getAllReports() {
        if (!MongoConnection.getInstance().isConnected()) {
            return new ArrayList<>(mockReports);
        }
        List<Report> list = new ArrayList<>();
        for (Document doc : collection.find().sort(Sorts.descending("createdAt")))
            list.add(docToReport(doc));
        return list;
    }

    public void updateReportStatus(String reportId, String status) {
        if (!MongoConnection.getInstance().isConnected()) {
            for (Report report : mockReports) {
                if (report.getReportId().equalsIgnoreCase(reportId)) {
                    report.setStatus(status);
                    break;
                }
            }
            return;
        }
        collection.updateOne(Filters.eq("reportId", reportId), Updates.set("status", status));
    }

    public Report getReportById(String reportId) {
        if (!MongoConnection.getInstance().isConnected()) {
            for (Report report : mockReports) {
                if (report.getReportId().equalsIgnoreCase(reportId)) {
                    return report;
                }
            }
            return new Report(reportId, "MH12AB1234", "user_uid", "Traffic Violation", "Mock data", "Pending", "2024-05-10T10:00:00Z");
        }
        return docToReport(collection.find(Filters.eq("reportId", reportId)).first());
    }

    public long getReportCount() { 
        return MongoConnection.getInstance().isConnected() ? collection.countDocuments() : mockReports.size(); 
    }
    public long getReportCountByStatus(String status) { 
        if (!MongoConnection.getInstance().isConnected()) {
            long count = 0;
            for (Report report : mockReports) {
                if (report.getStatus().equalsIgnoreCase(status)) {
                    count++;
                }
            }
            return count;
        }
        return collection.countDocuments(Filters.eq("status", status)); 
    }

    public List<Report> getReportsByVehicleNo(String vehicleNo) {
        if (!MongoConnection.getInstance().isConnected()) {
            List<Report> list = new ArrayList<>();
            for (Report report : mockReports) {
                if (report.getVehicleNo().equalsIgnoreCase(vehicleNo)) {
                    list.add(report);
                }
            }
            return list;
        }
        List<Report> list = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("vehicleNo", vehicleNo)).sort(Sorts.descending("createdAt")))
            list.add(docToReport(doc));
        return list;
    }

    private Report docToReport(Document doc) {
        if (doc == null) return null;
        return new Report(doc.getString("reportId"), doc.getString("vehicleNo"), doc.getString("reportedBy"),
                doc.getString("reason"), doc.getString("description"), doc.getString("status"), doc.getString("createdAt"));
    }
}
