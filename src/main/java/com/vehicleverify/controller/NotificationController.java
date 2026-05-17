package com.vehicleverify.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vehicleverify.model.User;
import com.vehicleverify.service.NotificationService;
import com.vehicleverify.service.UserService;
import com.vehicleverify.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NotificationController extends HttpServlet {
    private final NotificationService notificationService = new NotificationService();
    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = (User) req.getAttribute("user");
            if (user == null) { JsonUtil.sendError(resp, 401, "Unauthorized"); return; }
            JsonUtil.sendJson(resp, notificationService.getNotificationsForUser(user.getUserId()));
        } catch (Exception e) {
            System.err.println("ERROR in NotificationController.doGet: " + e.getMessage());
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getAttribute("user");
        if (user == null) { JsonUtil.sendError(resp, 401, "Unauthorized"); return; }
        
        String pathInfo = req.getPathInfo();
        if ("/pay-fine".equals(pathInfo)) {
            // Unfreeze account logic
            userService.updateFrozenStatus(user.getUserId(), false, 0.0);
            
            JsonObject result = new JsonObject();
            result.addProperty("message", "Payment successful. Account restored.");
            JsonUtil.sendJson(resp, result);
        } else if ("/read".equals(pathInfo)) {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            if (!body.has("id")) { JsonUtil.sendError(resp, 400, "Missing id"); return; }
            notificationService.markAsRead(body.get("id").getAsString());
            JsonUtil.sendJson(resp, new JsonObject());
        } else {
            JsonUtil.sendError(resp, 404, "Not found");
        }
    }
}
