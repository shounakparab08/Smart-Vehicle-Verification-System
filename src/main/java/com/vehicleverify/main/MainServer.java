package com.vehicleverify.main;

import com.vehicleverify.controller.*;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * MainServer is the entry point for the application.
 * It creates an embedded Tomcat server, registers all servlets and filters,
 * initializes Firebase, and starts listening on port 8080.
 *
 * Run: java -jar vehicle-verification-system-1.0-SNAPSHOT-jar-with-dependencies.jar
 */
public class MainServer {

    public static void main(String[] args) throws Exception {
        // Determine port from env or default
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null) {
            port = Integer.parseInt(portEnv);
        }

        // Initialize Firebase Admin SDK
        // Disabled for mock auth

        // Create Tomcat instance
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // trigger connector creation

        // Set up webapp directory (for static files: HTML, CSS, JS)
        String webappDir = new File("src/main/webapp").getAbsolutePath();
        Context ctx = tomcat.addWebapp("", webappDir);
        ctx.setParentClassLoader(MainServer.class.getClassLoader());
        ctx.addWelcomeFile("index.html");

        System.out.println("Serving static files from: " + webappDir);

        // ─────────────────────────────────────────────────
        // Register Filters
        // ─────────────────────────────────────────────────

        // CORS filter — applies to all requests
        org.apache.tomcat.util.descriptor.web.FilterDef corsFilterDef =
                new org.apache.tomcat.util.descriptor.web.FilterDef();
        corsFilterDef.setFilterName("CorsFilter");
        corsFilterDef.setFilterClass("com.vehicleverify.filter.CorsFilter");
        ctx.addFilterDef(corsFilterDef);

        org.apache.tomcat.util.descriptor.web.FilterMap corsFilterMap =
                new org.apache.tomcat.util.descriptor.web.FilterMap();
        corsFilterMap.setFilterName("CorsFilter");
        corsFilterMap.addURLPattern("/*");
        ctx.addFilterMap(corsFilterMap);

        // Auth filter — applies to /api/* requests
        org.apache.tomcat.util.descriptor.web.FilterDef authFilterDef =
                new org.apache.tomcat.util.descriptor.web.FilterDef();
        authFilterDef.setFilterName("AuthFilter");
        authFilterDef.setFilterClass("com.vehicleverify.filter.AuthFilter");
        ctx.addFilterDef(authFilterDef);

        org.apache.tomcat.util.descriptor.web.FilterMap authFilterMap =
                new org.apache.tomcat.util.descriptor.web.FilterMap();
        authFilterMap.setFilterName("AuthFilter");
        authFilterMap.addURLPattern("/api/*");
        ctx.addFilterMap(authFilterMap);

        // ─────────────────────────────────────────────────
        // Register Servlets (API Controllers)
        // ─────────────────────────────────────────────────

        // User API: /api/user/*
        Tomcat.addServlet(ctx, "UserController", new UserController());
        ctx.addServletMappingDecoded("/api/user/*", "UserController");

        // Vehicle API: /api/vehicle/*
        Tomcat.addServlet(ctx, "VehicleController", new VehicleController());
        ctx.addServletMappingDecoded("/api/vehicle/*", "VehicleController");

        // Report API: /api/report/*
        Tomcat.addServlet(ctx, "ReportController", new ReportController());
        ctx.addServletMappingDecoded("/api/report/*", "ReportController");

        // Admin API: /api/admin/*
        Tomcat.addServlet(ctx, "AdminController", new AdminController());
        ctx.addServletMappingDecoded("/api/admin/*", "AdminController");

        // Notification API: /api/notification/*
        Tomcat.addServlet(ctx, "NotificationController", new NotificationController());
        ctx.addServletMappingDecoded("/api/notification/*", "NotificationController");

        // ─────────────────────────────────────────────────
        // Start Server
        // ─────────────────────────────────────────────────
        tomcat.start();

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  Smart Vehicle Verification System                  ║");
        System.out.println("║  Server started on http://localhost:" + port + "            ║");
        System.out.println("║                                                      ║");
        System.out.println("║  Main Portal:      http://localhost:" + port + "/             ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        tomcat.getServer().await();
    }
}
