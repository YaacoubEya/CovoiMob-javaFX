package controllers;

import tn.esprit.utils.MyDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TokenStorage {
    public static final long TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000; // 24 hours

    // Class to hold session data
    public static class UserTokenData {
        public long userId;
        public String email;
        public String firstName;
        public String lastName;
        public String telephone;
        public String role;
        public boolean verified;
        public String vehicule;
        public String token;

        public UserTokenData(long userId, String email, String firstName, String lastName, String telephone,
                             String role, boolean verified, String vehicule, String token) {
            this.userId = userId;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.telephone = telephone;
            this.role = role;
            this.verified = verified;
            this.vehicule = vehicule;
            this.token = token;
        }
    }

    // Store user token into database
    public static void storeUserToken(UserTokenData userTokenData, long expirationTime) throws Exception {
        String sql = "INSERT INTO sessions (user_id, token, expiration_time) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE token = ?, expiration_time = ?";
        Connection conn = MyDataBase.getInstance().getCnx();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userTokenData.userId);
            stmt.setString(2, userTokenData.token);
            stmt.setLong(3, expirationTime);
            stmt.setString(4, userTokenData.token);
            stmt.setLong(5, expirationTime);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("❌ Failed to store session: " + e.getMessage(), e);
        }
    }

    // Retrieve user token and data
    public static UserTokenData getUserToken(int userId) throws Exception {
        String sql = "SELECT s.user_id, s.token, s.expiration_time, u.email, u.first_name, u.last_name, " +
                "u.telephone, u.role, u.verified, u.vehicule " +
                "FROM sessions s JOIN user u ON s.user_id = u.id WHERE s.user_id = ?";
        Connection conn = MyDataBase.getInstance().getCnx();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long expirationTime = rs.getLong("expiration_time");
                    if (System.currentTimeMillis() > expirationTime) {
                        clearToken(userId); // Token expired
                        return null;
                    }
                    return new UserTokenData(
                            rs.getLong("user_id"),
                            rs.getString("email"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("telephone"),
                            rs.getString("role"),
                            rs.getBoolean("verified"),
                            rs.getString("vehicule"),
                            rs.getString("token")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("❌ Failed to retrieve session: " + e.getMessage(), e);
        }
    }

    // Clear token session
    public static void clearToken(long userId) {
        String sql = "DELETE FROM sessions WHERE user_id = ?";
        Connection conn = MyDataBase.getInstance().getCnx();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error clearing session: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
