package com.example;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

private static final long TIMEOUT_MS = 60_000;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        String message = null;

        if (session != null) {
            Long last = (Long) session.getAttribute("lastActive");
            long now = System.currentTimeMillis();
            if (last != null && now - last > TIMEOUT_MS) {
                session.invalidate();
                session = null;
                message = "Your session has expired. Please submit again.";
            } else {
                session.setAttribute("lastActive", now);
            }
        } else if (req.getRequestedSessionId() != null) {
            message = "Your session has expired. Please submit again.";
        }
        render(resp, session, message);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String city = req.getParameter("city");

        HttpSession old = req.getSession(false);
        if (old != null) {
            Long last = (Long) old.getAttribute("lastActive");
            if (last != null && System.currentTimeMillis() - last > TIMEOUT_MS) {
                old.invalidate();
            }
        }

        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(60);
        session.setAttribute("lastActive", System.currentTimeMillis());

        List<String[]> users = (List<String[]>) session.getAttribute("users");
        if (users == null) {
            users = new ArrayList<>();
        }
        if (name != null && !name.isBlank()) {
            users.add(new String[]{esc(name), esc(email), esc(city)});
        }
        session.setAttribute("users", users);

        resp.sendRedirect("/users");
    }

    @SuppressWarnings("unchecked")
    private void render(HttpServletResponse resp, HttpSession session, String message)
            throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<html><head><title>Users</title><style>"
                + "body{font-family:Arial;margin:30px}"
                + "table{border-collapse:collapse;margin-top:20px}"
                + "th,td{border:1px solid #444;padding:8px 14px}"
                + "th{background:#eee}"
                + ".msg{color:#b00020;font-weight:bold}"
                + "input{margin:4px;padding:6px}"
                + "</style></head><body>");
        out.println("<h2>Submit User Data</h2>");
        out.println("<form method='post' action='/users'>"
                + "<input name='name' placeholder='Name' required>"
                + "<input name='email' placeholder='Email'>"
                + "<input name='city' placeholder='City'>"
                + "<button type='submit'>Submit</button></form>");

        if (message != null) {
            out.println("<p class='msg'>" + message + "</p>");
        }

        List<String[]> users = (session == null) ? null
                : (List<String[]>) session.getAttribute("users");

        if (users == null || users.isEmpty()) {
            out.println("<p>No data submitted yet.</p>");
        } else {
            out.println("<table><tr><th>#</th><th>Name</th><th>Email</th><th>City</th></tr>");
            int i = 1;
            for (String[] u : users) {
                out.println("<tr><td>" + i++ + "</td><td>" + u[0] + "</td><td>"
                        + u[1] + "</td><td>" + u[2] + "</td></tr>");
            }
            out.println("</table>");
        }
        out.println("</body></html>");
    }

    // basic HTML escape (XSS se bachne ke liye)
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}