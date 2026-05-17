package com.vehicleverify.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vehicleverify.model.User;
import com.vehicleverify.model.Vehicle;
import com.vehicleverify.service.UserService;
import com.vehicleverify.service.VehicleService;
import com.vehicleverify.service.ReportService;
import com.vehicleverify.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminController handles all admin-specific API endpoints.
 * All endpoints require admin role verification.
 */
public class AdminController extends HttpServlet {
    private final UserService userService = new UserService();
    private final VehicleService vehicleService = new VehicleService();
    private final ReportService reportService = new ReportService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Verify admin role
        if (!isAdmin(req)) {
            JsonUtil.sendError(resp, 403, "Admin access required");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "/";

        switch (pathInfo) {
            case "/users":
                // GET /api/admin/users — List all users
                List<User> users = userService.getAllUsers();
                JsonUtil.sendJson(resp, users);
                break;

            case "/stats":
                // GET /api/admin/stats — Analytics data
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalUsers", userService.getAllUsers().size());
                stats.put("totalVehicles", vehicleService.getVehicleCount());
                stats.put("totalReports", reportService.getReportCount());
                stats.put("blacklistedVehicles", vehicleService.getBlacklistedCount());
                stats.put("pendingReports", reportService.getReportCountByStatus("Pending"));
                stats.put("inProgressReports", reportService.getReportCountByStatus("In Progress"));
                stats.put("resolvedReports", reportService.getReportCountByStatus("Resolved"));
                stats.put("rejectedReports", reportService.getReportCountByStatus("Rejected"));
                JsonUtil.sendJson(resp, stats);
                break;

            case "/blacklisted":
                // GET /api/admin/blacklisted — List blacklisted vehicles
                List<Vehicle> blacklisted = vehicleService.getBlacklistedVehicles();
                JsonUtil.sendJson(resp, blacklisted);
                break;

            case "/vehicles":
                // GET /api/admin/vehicles — List all vehicles
                List<Vehicle> allVehicles = vehicleService.getAllVehicles();
                JsonUtil.sendJson(resp, allVehicles);
                break;

            default:
                JsonUtil.sendError(resp, 404, "Endpoint not found");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            JsonUtil.sendError(resp, 403, "Admin access required");
            return;
        }

        String pathInfo = req.getPathInfo();

        if ("/vehicle/blacklist".equals(pathInfo)) {
            // PUT /api/admin/vehicle/blacklist — Toggle blacklist status
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            if (!body.has("vehicleNo") || !body.has("blacklistStatus")) {
                JsonUtil.sendError(resp, 400, "Missing vehicleNo or blacklistStatus");
                return;
            }

            String vehicleNo = body.get("vehicleNo").getAsString().toUpperCase();
            boolean status = body.get("blacklistStatus").getAsBoolean();

            Vehicle v = vehicleService.getVehicle(vehicleNo);
            if (v == null) {
                JsonUtil.sendError(resp, 404, "Vehicle not found");
                return;
            }

            vehicleService.blacklistVehicle(vehicleNo, status);

            JsonObject result = new JsonObject();
            result.addProperty("vehicleNo", vehicleNo);
            result.addProperty("blacklistStatus", status);
            result.addProperty("message", status ? "Vehicle blacklisted" : "Vehicle removed from blacklist");
            JsonUtil.sendJson(resp, result);

        } else {
            JsonUtil.sendError(resp, 404, "Endpoint not found");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            JsonUtil.sendError(resp, 403, "Admin access required");
            return;
        }

        String pathInfo = req.getPathInfo();

        // DELETE /api/admin/users/USER_ID
        if (pathInfo != null && pathInfo.startsWith("/users/")) {
            String userId = pathInfo.substring("/users/".length());
            if (userId.isEmpty()) {
                JsonUtil.sendError(resp, 400, "Missing userId");
                return;
            }

            User existing = userService.getUserById(userId);
            if (existing == null) {
                JsonUtil.sendError(resp, 404, "User not found");
                return;
            }

            userService.deleteUser(userId);

            JsonObject result = new JsonObject();
            result.addProperty("message", "User deleted successfully");
            JsonUtil.sendJson(resp, result);
        } else {
            JsonUtil.sendError(resp, 404, "Endpoint not found");
        }
    }

    /**
     * Checks if the current request user has admin role.
     */
    private boolean isAdmin(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        return user != null && "admin".equals(user.getRole());
    }
}
