package main;

import java.sql.*;

/**
 * AttendanceUtils
 *
 * - Handles recording login and logout times for employees.
 * - Calculates worked hours and determines if a full shift (>=10 seconds for testing) was completed.
 * - All database interactions use SQLite.
 * 
 * FIXED ISSUES:
 * - Fixed SQL syntax error: SQLite doesn't support ORDER BY/LIMIT in UPDATE directly
 * - Added explicit commit after INSERT/UPDATE
 * - Changed database path to lowercase "database/lamesa.db" for consistency
 * - Fixed column name from "work_hours" to "worked_hours" to match schema
 * - Changed shift completion threshold from 8 hours to 10 seconds for testing
 */
public class AttendanceUtils {

    // SQLite database file location
    private static final String DB_URL = "jdbc:sqlite:database/lamesa.db";

    /**
     * Record login time for a user.
     * Creates a new attendance record with current timestamp.
     *
     * @param username The username of the employee logging in
     */
    public static void recordLogin(String username) {
        System.out.println("[AttendanceUtils] ===== RECORD LOGIN START =====");
        System.out.println("[AttendanceUtils] Username: " + username);
        
        String sql = "INSERT INTO attendance (username, login_time) VALUES (?, datetime('now'))";
        
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(false);
            
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            int rowsAffected = ps.executeUpdate();
            
            conn.commit();
            
            System.out.println("[AttendanceUtils] INSERT executed. Rows affected: " + rowsAffected);
            System.out.println("[AttendanceUtils] Transaction committed successfully");
            
            // Verify the insert
            String verifySql = "SELECT * FROM attendance WHERE username = ? ORDER BY id DESC LIMIT 1";
            try (PreparedStatement verifyPs = conn.prepareStatement(verifySql)) {
                verifyPs.setString(1, username);
                try (ResultSet rs = verifyPs.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("[AttendanceUtils] VERIFIED - Record exists:");
                        System.out.println("  ID: " + rs.getInt("id"));
                        System.out.println("  Username: " + rs.getString("username"));
                        System.out.println("  Login Time: " + rs.getString("login_time"));
                    } else {
                        System.err.println("[AttendanceUtils] ERROR: Record not found after insert!");
                    }
                }
            }
            
            System.out.println("[AttendanceUtils] Login recorded for user: " + username);

        } catch (SQLException e) {
            System.err.println("[AttendanceUtils] SQL Exception:");
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[AttendanceUtils] Transaction rolled back");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("[AttendanceUtils] ===== RECORD LOGIN END =====");
    }

    /**
     * Record logout time for a user.
     * Updates the most recent attendance record that has NULL logout_time.
     * Computes worked hours and marks full_shift if >= 10 seconds (testing threshold).
     *
     * CRITICAL FIX: SQLite doesn't support ORDER BY in UPDATE, so we use a subquery to get the ID first
     *
     * @param username The username of the employee logging out
     */
    public static void recordLogout(String username) {
        System.out.println("[AttendanceUtils] ===== RECORD LOGOUT START =====");
        System.out.println("[AttendanceUtils] Username: " + username);
        
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(false);
            
            System.out.println("[AttendanceUtils] Database connection successful");
            
            // First, find the ID of the most recent active session
            int activeSessionId = -1;
            String findIdSql = "SELECT id, login_time FROM attendance WHERE username = ? AND logout_time IS NULL ORDER BY id DESC LIMIT 1";
            try (PreparedStatement findPs = conn.prepareStatement(findIdSql)) {
                findPs.setString(1, username);
                try (ResultSet rs = findPs.executeQuery()) {
                    if (rs.next()) {
                        activeSessionId = rs.getInt("id");
                        System.out.println("[AttendanceUtils] Found active session:");
                        System.out.println("  ID: " + activeSessionId);
                        System.out.println("  Login Time: " + rs.getString("login_time"));
                    } else {
                        System.err.println("[AttendanceUtils] WARNING: No active session found for user: " + username);
                        conn.close();
                        System.out.println("[AttendanceUtils] ===== RECORD LOGOUT END =====");
                        return;
                    }
                }
            }
            
            // Now update that specific record using the ID
            // FIXED: Use WHERE id = ? instead of ORDER BY/LIMIT which SQLite doesn't support in UPDATE
            String updateSql = "UPDATE attendance " +
                             "SET logout_time = datetime('now'), " +
                             "    worked_hours = ROUND((JULIANDAY(datetime('now')) - JULIANDAY(login_time)) * 24, 2), " +
                             "    full_shift = CASE WHEN ((JULIANDAY(datetime('now')) - JULIANDAY(login_time)) * 24 * 3600) >= 10 THEN 1 ELSE 0 END " +
                             "WHERE id = ?";
            
            ps = conn.prepareStatement(updateSql);
            ps.setInt(1, activeSessionId);
            int rows = ps.executeUpdate();
            
            conn.commit();
            
            System.out.println("[AttendanceUtils] UPDATE executed. Rows updated: " + rows);
            System.out.println("[AttendanceUtils] Transaction committed successfully");
            
            if (rows > 0) {
                // Verify the update
                String verifySql = "SELECT * FROM attendance WHERE id = ?";
                try (PreparedStatement verifyPs = conn.prepareStatement(verifySql)) {
                    verifyPs.setInt(1, activeSessionId);
                    try (ResultSet rs = verifyPs.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("[AttendanceUtils] VERIFIED - Record updated:");
                            System.out.println("  ID: " + rs.getInt("id"));
                            System.out.println("  Login Time: " + rs.getString("login_time"));
                            System.out.println("  Logout Time: " + rs.getString("logout_time"));
                            System.out.println("  Worked Hours: " + rs.getDouble("worked_hours"));
                            System.out.println("  Full Shift: " + rs.getBoolean("full_shift"));
                        }
                    }
                }
            } else {
                System.err.println("[AttendanceUtils] ERROR: No rows updated!");
            }

        } catch (SQLException e) {
            System.err.println("[AttendanceUtils] SQL Exception:");
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[AttendanceUtils] Transaction rolled back");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("[AttendanceUtils] ===== RECORD LOGOUT END =====");
    }
}