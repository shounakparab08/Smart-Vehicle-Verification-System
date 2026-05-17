package com.vehicleverify.filter;


import com.vehicleverify.model.User;
import com.vehicleverify.service.UserService;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * AuthFilter intercepts all /api/* requests, extracts the Firebase ID token
 * from the Authorization header, verifies it, and attaches the User object
 * to the request for downstream controllers.
 *
 * Public endpoints (like /api/user/register) skip full user lookup but still
 * verify the token.
 */
public class AuthFilter implements Filter {

    private final UserService userService = new UserService();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String path = req.getRequestURI();

        // Allow OPTIONS pre-flight requests through
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        // Skip auth for registration endpoint (token is verified inside the controller)
        if (path.endsWith("/api/user/register")) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        try {
            // Extract token from Authorization: Bearer <token>
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                resp.setStatus(401);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Missing or invalid Authorization header\"}");
                return;
            }

            String idToken = authHeader.substring(7);

            // MOCK: Bypass Firebase verification and extract UID directly
            String uid = idToken.startsWith("mock_token_") ? idToken.substring("mock_token_".length()) : idToken;

            // Look up user in MongoDB (with mock fallback in service)
            User user = userService.getUserById(uid);
            
            // Fallback: try looking up by email (uid is email in mock mode)
            if (user == null && uid.contains("@")) {
                user = userService.getUserByEmail(uid);
            }

            // Attach user to request (may be null for first-time callers)
            if (user != null) {
                req.setAttribute("user", user);
                
                // Enforce account freezing (except for specific endpoints)
                if (user.isFrozen() && !"admin".equals(user.getRole())) {
                    boolean isAllowedEndpoint = path.endsWith("/api/user/profile") || 
                                              path.contains("/api/notification");
                    
                    if (!isAllowedEndpoint) {
                        resp.setStatus(403);
                        resp.setContentType("application/json");
                        resp.getWriter().write("{\"error\":\"Account frozen due to pending penalty. Please pay the fine to restore access.\", \"frozen\": true}");
                        return;
                    }
                }
            }

            // Continue the filter chain
            chain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            System.err.println("ERROR in AuthFilter: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No init needed
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
