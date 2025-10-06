package controllers;

public class SessionManager {
    private static SessionManager instance;
    private Integer userId; // Use Integer for nullability

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void clearSession() {
        if (userId != null) {
            TokenStorage.clearToken(userId);
        }
        userId = null;
    }
}