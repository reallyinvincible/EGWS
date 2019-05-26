package com.exuberant.egws.models;


import java.util.List;

public class User {

    String displayName;
    String userId;
    String email;
    boolean allowedToApp;
    List<String> reports;

    public User() {
    }

    public User(String displayName, String userId, String email, boolean allowedToApp, List<String> reports) {
        this.displayName = displayName;
        this.userId = userId;
        this.email = email;
        this.allowedToApp = allowedToApp;
        this.reports = reports;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getAllowedToApp() {
        return allowedToApp;
    }

    public void setAllowedToApp(boolean allowedToApp) {
        this.allowedToApp = allowedToApp;
    }

    public List<String> getReports() {
        return reports;
    }

    public void setReports(List<String> reports) {
        this.reports = reports;
    }
}


