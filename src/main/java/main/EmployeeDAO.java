package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeDAO
 *
 * Simple Data Access Object to read users and attendance info from the SQLite DB
 * and to update a user's role.
 *
 * - This centralizes SQL so controllers stay cleaner.
 * - Adjust SQL/table names here if your schema differs.
 */
public class EmployeeDAO {

    private static final String DB_URL = "jdbc:sqlite:database/lamesa.db";
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Lightweight DTO used by controllers.
     */
    public static class EmployeeRow {
        public final String username;
        public final String role;
        public final String lastLogin;   // may be null
        public final String lastLogout;  // may be null

        public EmployeeRow(String username, String role, String lastLogin, String lastLogout) {
            this.username = username;
            this.role = role;
            this.lastLogin = lastLogin;
            this.lastLogout = lastLogout;
        }
    }

    /**
     * Return all users with their most recent attendance row (if any).
     * This method returns one row per user, with the latest attendance timestamps.
     *
     * Uses LEFT JOIN + a subquery to pick the latest attendance per user.
     */
    public List<EmployeeRow> fetchAllUsersWithLastAttendance() {
        List<EmployeeRow> out = new ArrayList<>();

        // This query:
        //  - gets every user (username, role)
        //  - LEFT JOINs a subquery that returns the latest attendance row per username
        String sql = ""
            + "SELECT u.username, u.role, a.login_time, a.logout_time "
            + "FROM users u "
            + "LEFT JOIN ( "
            + "  SELECT username, login_time, logout_time "
            + "  FROM attendance "
            + "  WHERE id IN ( "
            + "    SELECT id FROM attendance a2 WHERE a2.username = attendance.username ORDER BY login_time DESC LIMIT 1 "
            + "  ) "
            + ") a ON a.username = u.username "
            + "ORDER BY u.username COLLATE NOCASE ASC;";

        // Note: the subquery style above is SQLite-friendly. If you prefer you can replace
        // it with a different approach (window functions) if using a newer SQLite build.

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String role = rs.getString("role");
                String loginTime = rs.getString("login_time");   // may be null
                String logoutTime = rs.getString("logout_time"); // may be null
                out.add(new EmployeeRow(username, role, loginTime, logoutTime));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out;
    }

    /**
     * Update the role for a given username.
     * Returns true when update affected at least one row.
     */
    public boolean updateRoleByUsername(String username, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newRole);
            ps.setString(2, username);
            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

/**
 * Delete a user from the database by username.
 * Returns true if deletion was successful.
 */
public boolean deleteUserByUsername(String username) {
    String sql = "DELETE FROM users WHERE username = ?";
    
    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, username);
        int deleted = ps.executeUpdate();
        return deleted > 0; 
        
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
}