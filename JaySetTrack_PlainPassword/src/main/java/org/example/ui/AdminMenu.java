package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private static final List<String> VALID_ROLES = List.of("ADMIN", "CUSTOMER", "TECHNICIAN");
    private static final List<String> VALID_STATUSES = List.of("APPROVED", "PENDING");

    private final Scanner scanner;
    private final UserDao userDao;
    private final User currentAdmin;

    public AdminMenu(Scanner scanner, UserDao userDao, User currentAdmin) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.currentAdmin = currentAdmin;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role/Status");
            System.out.println("5. Logout");

            int choice = readIntInput("Choose an option: ");
            switch (choice) {
                case 1 -> printUserTable(userDao.getAllUsers());
                case 2 -> printUserTable(userDao.getPendingUsers());
                case 3 -> approveUser();
                case 4 -> updateUser();
                case 5 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    // ---------- Table helper ----------

    private void printUserTable(List<User> users) {
        String border = "+-------+-----------------+--------------+------------+";
        System.out.println(border);
        System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n", "ID", "Username", "Role", "Status");
        System.out.println(border);
        if (users.isEmpty()) {
            System.out.printf("| %-51s |%n", "No users found.");
        }
        for (User u : users) {
            System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n",
                    u.getUserId(), u.getUsername(), u.getRole(), u.getAccountStatus());
        }
        System.out.println(border);
    }

    // ---------- Dev 5: approve ----------

    private void approveUser() {
        List<User> pending = userDao.getPendingUsers();
        printUserTable(pending);
        if (pending.isEmpty()) {
            System.out.println("There are no pending accounts to approve.");
            return;
        }

        int targetId = readIntInput("Enter User ID to approve (0 to cancel): ");
        if (targetId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        boolean inPending = pending.stream().anyMatch(u -> u.getUserId() == targetId);
        if (!inPending) {
            System.out.println("User ID " + targetId + " is not in the pending list.");
            return;
        }

        if (userDao.approveUser(targetId)) {
            System.out.println("SUCCESS: User ID " + targetId + " has been APPROVED.");
        } else {
            System.out.println("Approval failed. Please try again.");
        }
    }

    // ---------- Dev 7: update role/status ----------

    private void updateUser() {
        List<User> allUsers = userDao.getAllUsers();
        printUserTable(allUsers);

        int targetUserId = readIntInput("Enter User ID to update (0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        // Self-edit restriction
        if (targetUserId == currentAdmin.getUserId()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        if (allUsers.stream().noneMatch(u -> u.getUserId() == targetUserId)) {
            System.out.println("User ID " + targetUserId + " does not exist.");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus)) {
            System.out.println("SUCCESS: User " + targetUserId + " updated to role "
                    + newRole + " and status " + newStatus + ".");
        } else {
            System.out.println("Update failed. User ID " + targetUserId + " was not found.");
        }
    }

    // ---------- Input validation wrappers ----------

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Returns null if the admin enters 0 to cancel
    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role (" + String.join(", ", VALID_ROLES) + ") or 0 to cancel: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (VALID_ROLES.contains(input)) {
                return input;
            }
            System.out.println("Invalid role. Choose one of: " + String.join(", ", VALID_ROLES) + ".");
        }
    }

    // Returns null if the admin enters 0 to cancel
    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status (" + String.join(", ", VALID_STATUSES) + ") or 0 to cancel: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (VALID_STATUSES.contains(input)) {
                return input;
            }
            System.out.println("Invalid status. Choose one of: " + String.join(", ", VALID_STATUSES) + ".");
        }
    }
}