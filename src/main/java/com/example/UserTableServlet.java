package com.example;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Stores submitted users in the HttpSession and shows them as a table. */
public class UserTableServlet extends HttpServlet {

    /** Session idle timeout required by the task: 60 seconds. */
    private static final int TIMEOUT_SECONDS = 60;
    private static final long TIMEOUT_MS = TIMEOUT_SECONDS * 1000L;
    private static final String USERS_KEY = "users";
    private static final String LAST_ACTIVE_KEY = "lastActive";
    private static final String EXPIRED_MSG =
            "Your session has expired. Please submit again.";

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

        List<User> users = (session == null) ? null
                : (List<User>) session.getAttribute(USERS_KEY);

        resp.setContentType("text/html;charset=UTF-8");
        HtmlView.write(resp.getWriter(), req.getContextPath() + "/users",
                users, message);
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

        List<User> users = (List<User>) session.getAttribute(USERS_KEY);
        if (users == null) {
            users = new ArrayList<>();
        }

        String name = req.getParameter("name");
        if (name != null && !name.isBlank()) {
            users.add(new User(name,
                    nullToEmpty(req.getParameter("email")),
                    nullToEmpty(req.getParameter("city"))));
        }
        session.setAttribute(USERS_KEY, users);

        resp.sendRedirect(req.getContextPath() + "/users");
    }

    private boolean isIdle(HttpSession session) {
        Long last = (Long) session.getAttribute(LAST_ACTIVE_KEY);
        return last != null && System.currentTimeMillis() - last > TIMEOUT_MS;
    }

    private void touch(HttpSession session) {
        session.setAttribute(LAST_ACTIVE_KEY, System.currentTimeMillis());
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}