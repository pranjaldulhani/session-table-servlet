package com.example;

/** Simple model for one submitted user record. */
public class User {
    private final String name;
    private final String email;
    private final String city;

    public User(String name, String email, String city) {
        this.name = name;
        this.email = email;
        this.city = city;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getCity() { return city; }
}