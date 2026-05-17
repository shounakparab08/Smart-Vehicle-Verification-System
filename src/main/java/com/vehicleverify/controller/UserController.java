package com.vehicleverify.controller;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vehicleverify.model.User;
import com.vehicleverify.service.UserService;

import com.vehicleverify.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

public class UserController extends HttpServlet {
    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if ("/register".equals(req.getPathInfo())) {
                JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
                String idToken = body.has("token") ? body.get("token").getAsString() : null;
                String name = body.has("name") ? body.get("name").getAsString() : "";
                String role = body.has("role") ? body.get("role").getAsString() : "customer";
                if (idToken == null) { JsonUtil.sendError(resp, 400, "Missing mock token"); return; }
                
                // MOCK: Extract UID and Email directly from request
                String uid = idToken.startsWith("mock_token_") ? idToken.substring("mock_token_".length()) : idToken;
                String email = body.has("email") ? body.get("email").getAsString() : "mock@example.com";
                
                // Check if user already exists (by UID or email)
                User existing = userService.getUserById(uid);
                if (existing == null && email != null) {
                    existing = userService.getUserByEmail(email);
                }
                if (existing != null) { JsonUtil.sendJson(resp, existing); return; }
                
                // Create new user with the name they typed in the signup form
                User newUser = new User(uid, name, email, role, Instant.now().toString());
                userService.createUser(newUser);
                JsonUtil.sendJson(resp, newUser);
            } else { JsonUtil.sendError(resp, 404, "Not found"); }
        } catch (Exception e) {
            System.err.println("ERROR in UserController.doPost: " + e.getMessage());
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/profile".equals(req.getPathInfo())) {
            User user = (User) req.getAttribute("user");
            if (user == null) { JsonUtil.sendError(resp, 401, "Unauthorized"); return; }
            
            // Prevent caching of profile data to fix account switching issues
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Expires", "0");
            
            JsonUtil.sendJson(resp, user);
        } else { JsonUtil.sendError(resp, 404, "Not found"); }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/profile".equals(req.getPathInfo())) {
            User user = (User) req.getAttribute("user");
            if (user == null) { JsonUtil.sendError(resp, 401, "Unauthorized"); return; }
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            if (body.has("name")) { user.setName(body.get("name").getAsString()); userService.updateUser(user); }
            JsonUtil.sendJson(resp, user);
        } else { JsonUtil.sendError(resp, 404, "Not found"); }
    }
}
