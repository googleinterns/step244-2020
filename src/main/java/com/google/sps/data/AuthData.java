package com.google.sps.data;

public class AuthData {
    private boolean isLoggedIn = false;
    private String authLink = ""; // Field for Login and Logout
    private String userId = "";
    public AuthData(boolean isLoggedIn, String authLink, String userId) {
        this.isLoggedIn = isLoggedIn;
        this.authLink = authLink;
        this.userId = userId;
    }
}
