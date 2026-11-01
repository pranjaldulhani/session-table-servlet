package com.example;

import java.io.PrintWriter;
import java.util.List;

/** Builds the HTML page: form, optional message and users table. */
public final class HtmlView {

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

    private HtmlView() { }

    /** Writes the complete page to the given writer. */
    public static void write(PrintWriter out, String action,
                             List<User> users, String message) {
        out.println(PAGE_HEAD);
        out.println("<form method='post' action='" + action + "'>"
                + "<input name='name' placeholder='Name' required>"
                + "<input name='email' placeholder='Email'>"
                + "<input name='city' placeholder='City'>"
                + "<button type='submit'>Submit</button></form>");
        if (message != null) {
            out.println("<p class='msg'>" + escape(message) + "</p>");
        }
        writeTable(out, users);
        out.println("</body></html>");
        out.flush();
    }

    private static void writeTable(PrintWriter out, List<User> users) {
        if (users == null || users.isEmpty()) {
            out.println("<p>No data submitted yet.</p>");
            return;
        }
        out.println(TABLE_HEAD);
        int i = 1;
        for (User u : users) {
            out.println("<tr><td>" + i++ + "</td><td>" + escape(u.getName())
                    + "</td><td>" + escape(u.getEmail()) + "</td><td>"
                    + escape(u.getCity()) + "</td></tr>");
        }
        out.println("</table>");
    }

    /** Escapes HTML special characters to prevent XSS. */
    static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}