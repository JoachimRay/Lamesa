package main;

/**
 * Session class to manage the currently logged-in user and their role.
 * 
 * This class uses static fields and methods so the session is accessible
 * globally throughout the application. It supports role-based checks (RBAC)
 * and session clearing for logout.
 */
public class SessionManager {

    // Stores the username of the currently logged-in user
    private static String username;

    // Stores the role of the currently logged-in user (e.g., "manager", "employee")
    private static String role;

    /**
     * Sets the current logged-in user and their role.
     * Should be called immediately after a successful login.
     *
     * @param user     The username of the user
     * @param userRole The role of the user (manager/employee)
     */
    public static void setCurrentUser(String user, String userRole) {
        username = user;
        role = userRole;
    }

    /**
     * Returns the username of the currently logged-in user.
     *
     * @return username as a String, or null if no user is logged in
     */
    public static String getUsername() {
        return username;
    }

    /**
     * Returns the role of the currently logged-in user.
     *
     * @return role as a String (e.g., "manager", "employee"), or null if no user is logged in
     */
    public static String getRole() {
        return role;
    }

    /**
     * Checks if the currently logged-in user is a manager.
     * Can be used to control access to manager-only features.
     *
     * @return true if the user role is "manager", false otherwise
     */
    public static boolean isManager() {
        return "manager".equalsIgnoreCase(role);
    }

    /**
     * Checks if the currently logged-in user is an employee.
     * Can be used to control access to employee-only features.
     *
     * @return true if the user role is "employee", false otherwise
     */
    public static boolean isEmployee() {
        return "employee".equalsIgnoreCase(role);
    }

    /**
     * Clears the session by resetting username and role.
     * Should be called on logout to prevent unauthorized access.
     */
    public static void clear() {
        username = null;
        role = null;
    }
}
