package com.vehicleverify.model;

public class Report {
    private String reportId;
    private String vehicleNo;
    private String reportedBy;
    private String reason;
    private String description;
    private String status;
    private String createdAt;

    public Report() {}

    public Report(String reportId, String vehicleNo, String reportedBy, String reason, String description, String status, String createdAt) {
        this.reportId = reportId;
        this.vehicleNo = vehicleNo;
        this.reportedBy = reportedBy;
        this.reason = reason;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
