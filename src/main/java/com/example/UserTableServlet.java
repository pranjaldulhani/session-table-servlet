package com.example;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores each submitted user record in the HttpSession and renders
 * all records of that session as an HTML table.
 */
public class UserTableServlet extends HttpServlet {

    /** Session idle timeout required by the task: 60 seconds. */
    private static final int TIMEOUT_SECONDS = 60;
    private static final long TIMEOUT_MS = TIMEOUT_SECONDS * 1000L;
    private static final String USERS_KEY = "users";
    private static final String LAST_ACTIVE_KEY = "lastActive";
    private static final String EXPIRED_MSG =
            "Your session has expired. Please submit again.";

    private static final String PAGE_HEAD =
            "<html><head><title>Users</title><style>"
            + "body{font-family:Arial;margin:30px}"
            + "table{border-collapse:collapse;margin-top:20px}"
            + "th,td{border:1px solid #444;padding:8px 14px}"
            + "th{background:#eee}"
            + ".msg{color:#b00020;font-weight:bold}"
            + "input{margin:4px;padding:6px}"
            + "</style></head><body><h2>Submit User Data</h2>";

    private static final String TABLE_HEAD =
            "<table><tr><th>#</th><th>Name</th><th>Email</th><th>City</th></tr>";

    /** Shows the form and the records stored in the session. */
    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        String message = null;

        if (session != null && isIdle(session)) {
            session.invalidate();
            session = null;
            message = EXPIRED_MSG;
        } else if (session != null) {
            touch(session);
        } else if (req.getRequestedSessionId() != null) {
            message = EXPIRED_MSG;
        }

        List<String[]> users = (session == null) ? null
                : (List<String[]>) session.getAttribute(USERS_KEY);
        render(req, resp, users, message);
    }

    /** Adds the submitted record to the session list, then redirects. */
    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession old = req.getSession(false);
        if (old != null && isIdle(old)) {
            old.invalidate();
        }

        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(TIMEOUT_SECONDS);
        touch(session);

        List<String[]> users = (List<String[]>) session.getAttribute(USERS_KEY);
        if (users == null) {
            users = new ArrayList<>();
        }

        String name = req.getParameter("name");
        if (name != null && !name.isBlank()) {
            users.add(new String[]{name,
                    nullToEmpty(req.getParameter("email")),
                    nullToEmpty(req.getParameter("city"))});
        }
        session.setAttribute(USERS_KEY, users);

        resp.sendRedirect(req.getContextPath() + "/users");
    }

    /** Returns true if the session was inactive longer than the timeout. */
    private boolean isIdle(HttpSession session) {
        Long last = (Long) session.getAttribute(LAST_ACTIVE_KEY);
        return last != null && System.currentTimeMillis() - last > TIMEOUT_MS;
    }

    /** Records the current time as the last activity of the session. */
    private void touch(HttpSession session) {
        session.setAttribute(LAST_ACTIVE_KEY, System.currentTimeMillis());
    }

    /** Writes the complete HTML page. */
    private void render(HttpServletRequest req, HttpServletResponse resp,
                        List<String[]> users, String message) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println(PAGE_HEAD);
        renderForm(out, req.getContextPath() + "/users");
        if (message != null) {
            out.println("<p class='msg'>" + message + "</p>");
        }
        renderTable(out, users);
        out.println("</body></html>");
        out.flush();
    }

    /** Writes the input form. */
    private void renderForm(PrintWriter out, String action) {
        out.println("<form method='post' action='" + action + "'>"
                + "<input name='name' placeholder='Name' required>"
                + "<input name='email' placeholder='Email'>"
                + "<input name='city' placeholder='City'>"
                + "<button type='submit'>Submit</button></form>");
    }

    /** Writes the table of users, or a message if there are none. */
    private void renderTable(PrintWriter out, List<String[]> users) {
        if (users == null || users.isEmpty()) {
            out.println("<p>No data submitted yet.</p>");
            return;
        }
        out.println(TABLE_HEAD);
        int i = 1;
        for (String[] u : users) {
            out.println("<tr><td>" + i++ + "</td><td>" + escape(u[0])
                    + "</td><td>" + escape(u[1]) + "</td><td>"
                    + escape(u[2]) + "</td></tr>");
        }
        out.println("</table>");
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** Basic HTML escaping to prevent XSS. */
    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}