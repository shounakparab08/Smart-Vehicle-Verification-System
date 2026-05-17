package com.vehicleverify.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vehicleverify.model.Notification;
import com.vehicleverify.model.Report;
import com.vehicleverify.model.User;
import com.vehicleverify.model.Vehicle;
import com.vehicleverify.service.NotificationService;
import com.vehicleverify.service.ReportService;
import com.vehicleverify.service.UserService;
import com.vehicleverify.service.VehicleService;
import com.vehicleverify.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

public class ReportController extends HttpServlet {
    private final ReportService reportService = new ReportService();
    private final UserService userService = new UserService();
    private final VehicleService vehicleService = new VehicleService();
    private final NotificationService notificationService = new NotificationService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = (User) req.getAttribute("user");
            if (user == null) { JsonUtil.sendError(resp, 401, "Unauthorized"); return; }
            Report report = gson.fromJson(req.getReader(), Report.class);
            if (report == null || report.getVehicleNo() == null) { JsonUtil.sendError(resp, 400, "Invalid data"); return; }
            report.setReportId(UUID.randomUUID().toString());
            report.setReportedBy(user.getUserId());
            report.setStatus("Pending");
            report.setCreatedAt(Instant.now().toString());
            report.setVehicleNo(report.getVehicleNo().toUpperCase());
            reportService.createReport(report);
            JsonUtil.sendJson(resp, report);
        } catch (Exception e) {
            System.err.println("ERROR in ReportController.doPost: " + e.getMessage());
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = (User) req.getAttribute("user");
            if (user == null) { JsonUtil.sendError(resp, 401, "Unauthorized"); return; }
            String pathInfo = req.getPathInfo();
            if ("/history".equals(pathInfo)) {
                JsonUtil.sendJson(resp, reportService.getReportsByUser(user.getUserId()));
            } else if ("/all".equals(pathInfo)) {
                if (!"admin".equals(user.getRole())) { JsonUtil.sendError(resp, 403, "Admin required"); return; }
                JsonUtil.sendJson(resp, reportService.getAllReports());
            } else if ("/by-vehicle".equals(pathInfo)) {
                String vno = req.getParameter("vehicleNo");
                if (vno == null) { JsonUtil.sendError(resp, 400, "Missing vehicleNo"); return; }
                JsonUtil.sendJson(resp, reportService.getReportsByVehicleNo(vno.toUpperCase()));
            } else { JsonUtil.sendError(resp, 404, "Not found"); }
        } catch (Exception e) {
            System.err.println("ERROR in ReportController.doGet: " + e.getMessage());
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = (User) req.getAttribute("user");
            if (user == null || !"admin".equals(user.getRole())) { JsonUtil.sendError(resp, 403, "Admin required"); return; }
            if ("/status".equals(req.getPathInfo())) {
                JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
                if (!body.has("reportId") || !body.has("status")) { JsonUtil.sendError(resp, 400, "Missing fields"); return; }
                
                String reportId = body.get("reportId").getAsString();
                String status = body.get("status").getAsString();
                
                // Resolve logic
                if ("Resolved".equalsIgnoreCase(status)) {
                    Report report = reportService.getReportById(reportId);
                    if (report != null) {
                        Vehicle vehicle = vehicleService.getVehicle(report.getVehicleNo());
                        if (vehicle != null) {
                            User owner = userService.getUserByName(vehicle.getOwnerName());
                            if (owner != null) {
                                // 1. Freeze Account & Set Fine
                                double fine = 500.0; // Default fine
                                userService.updateFrozenStatus(owner.getUserId(), true, fine);
                                
                                // 2. Send Notification (Email)
                                Notification n = new Notification();
                                n.setUserId(owner.getUserId());
                                n.setTitle("URGENT: Traffic Violation Reported — Account Restricted");
                                n.setMessage("Your vehicle " + vehicle.getVehicleNo() + " was reported for: " + report.getReason() + ". " +
                                           "Description: " + report.getDescription() + ". " +
                                           "Your account has been temporarily frozen. To restore access, please acknowledge the violation and pay the fine of ₹" + fine + ".");
                                n.setType("Penalty");
                                n.setFineAmount(fine);
                                n.setReportId(reportId);
                                notificationService.sendNotification(n);
                            }
                        }
                    }
                }
                
                reportService.updateReportStatus(reportId, status);
                JsonObject result = new JsonObject();
                result.addProperty("message", "Status updated and owner notified");
                JsonUtil.sendJson(resp, result);
            } else { JsonUtil.sendError(resp, 404, "Not found"); }
        } catch (Exception e) {
            System.err.println("ERROR in ReportController.doPut: " + e.getMessage());
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, e.getMessage());
        }
    }
}
