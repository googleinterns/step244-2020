package com.google.sps.data;

public class AuthData {
    private boolean isLoggedIn = false;
    private String authLink = ""; // Field for Login and Logout
    public AuthData(boolean isLoggedIn, String authLink) {
        this.isLoggedIn = isLoggedIn;
        this.authLink = authLink;
    }
}
