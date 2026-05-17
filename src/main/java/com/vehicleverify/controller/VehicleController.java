package com.vehicleverify.controller;

import com.google.gson.Gson;
import com.vehicleverify.model.User;
import com.vehicleverify.model.Vehicle;
import com.vehicleverify.service.VehicleService;
import com.vehicleverify.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class VehicleController extends HttpServlet {
    private final VehicleService vehicleService = new VehicleService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String path = req.getPathInfo();
            if ("/search".equals(path)) {
                String vehicleNo = req.getParameter("vehicleNo");
                if (vehicleNo == null) { JsonUtil.sendError(resp, 400, "Missing vehicleNo"); return; }
                
                Vehicle vehicle = vehicleService.getVehicle(vehicleNo.toUpperCase());
                if (vehicle == null) { JsonUtil.sendError(resp, 404, "Vehicle not found"); return; }
                JsonUtil.sendJson(resp, vehicle);
            } else if ("/my-vehicles".equals(path)) {
                User user = (User) req.getAttribute("user");
                if (user == null) { JsonUtil.sendError(resp, 401, "Unauthorized"); return; }
                JsonUtil.sendJson(resp, vehicleService.getVehiclesByOwner(user.getName()));
            } else if ("/all".equals(path)) {
                JsonUtil.sendJson(resp, vehicleService.getAllVehicles());
            } else if ("/blacklisted".equals(path)) {
                JsonUtil.sendJson(resp, vehicleService.getBlacklistedVehicles());
            } else { JsonUtil.sendError(resp, 404, "Not found"); }
        } catch (Exception e) {
            System.err.println("ERROR in VehicleController.doGet: " + e.getMessage());
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // Customer vehicle registration — POST /api/vehicle/register
        if ("/register".equals(pathInfo)) {
            User user = (User) req.getAttribute("user");
            if (user == null) { JsonUtil.sendError(resp, 401, "Unauthorized"); return; }
            Vehicle vehicle = gson.fromJson(req.getReader(), Vehicle.class);
            if (vehicle == null || vehicle.getVehicleNo() == null) { JsonUtil.sendError(resp, 400, "Invalid vehicle data"); return; }
            vehicle.setVehicleNo(vehicle.getVehicleNo().toUpperCase());
            vehicle.setOwnerName(user.getName()); // auto-set owner from profile
            vehicle.setBlacklistStatus(false);
            if (vehicleService.getVehicle(vehicle.getVehicleNo()) != null) { JsonUtil.sendError(resp, 409, "Vehicle already registered"); return; }
            vehicleService.addVehicle(vehicle);
            JsonUtil.sendJson(resp, vehicle);
            return;
        }

        // Admin-only vehicle add (existing)
        if (!isAdmin(req)) { JsonUtil.sendError(resp, 403, "Admin access required"); return; }
        Vehicle vehicle = gson.fromJson(req.getReader(), Vehicle.class);
        if (vehicle == null || vehicle.getVehicleNo() == null) { JsonUtil.sendError(resp, 400, "Invalid data"); return; }
        vehicle.setVehicleNo(vehicle.getVehicleNo().toUpperCase());
        if (vehicleService.getVehicle(vehicle.getVehicleNo()) != null) { JsonUtil.sendError(resp, 409, "Vehicle exists"); return; }
        vehicleService.addVehicle(vehicle);
        JsonUtil.sendJson(resp, vehicle);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) { JsonUtil.sendError(resp, 403, "Admin access required"); return; }
        Vehicle vehicle = gson.fromJson(req.getReader(), Vehicle.class);
        if (vehicle == null || vehicle.getVehicleNo() == null) { JsonUtil.sendError(resp, 400, "Invalid data"); return; }
        vehicle.setVehicleNo(vehicle.getVehicleNo().toUpperCase());
        if (vehicleService.getVehicle(vehicle.getVehicleNo()) == null) { JsonUtil.sendError(resp, 404, "Not found"); return; }
        vehicleService.updateVehicle(vehicle);
        JsonUtil.sendJson(resp, vehicle);
    }

    private boolean isAdmin(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        return user != null && "admin".equals(user.getRole());
    }
}
