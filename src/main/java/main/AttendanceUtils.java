package main;

import java.sql.*;

/**
 * AttendanceUtils
 *
 * - Handles recording login and logout times for employees.
 * - Calculates worked hours and determines if a full shift (>=8 hours) was completed.
 * - All database interactions use SQLite.
 */
public class AttendanceUtils {

    // SQLite database file location
    private static final String DB_URL = "jdbc:sqlite:Database/lamesa.db";

    /**
     * Record login time for a user.
     * Creates a new attendance record with current timestamp.
     *
     * @param username The username of the employee logging in
     */
    public static void recordLogin(String username) {
        String sql = "INSERT INTO attendance (username, login_time) VALUES (?, datetime('now'))";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.executeUpdate();
            System.out.println("[AttendanceUtils] Login recorded for user: " + username);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[AttendanceUtils] Failed to record login: " + e.getMessage());
        }
    }

    /**
     * Record logout time for a user.
     * Updates the most recent attendance record that has NULL logout_time.
     * Computes worked hours and marks full_shift if >= 8 hours.
     *
     * @param username The username of the employee logging out
     */
    public static void recordLogout(String username) {
        String sql = "UPDATE attendance " +
                     "SET logout_time = datetime('now'), " +
                     "    work_hours = ROUND((JULIANDAY('now') - JULIANDAY(login_time)) * 24, 2), " +
                     "    full_shift = CASE WHEN (JULIANDAY('now') - JULIANDAY(login_time)) * 24 >= 8 THEN 1 ELSE 0 END " +
                     "WHERE username = ? AND logout_time IS NULL " +
                     "ORDER BY id DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            int rows = ps.executeUpdate();
            System.out.println("[AttendanceUtils] Logout recorded for user: " + username + ". Rows updated: " + rows);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[AttendanceUtils] Failed to record logout: " + e.getMessage());
        }
    }
}
