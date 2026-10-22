package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentUser;

    public AdminMenu(UserDao userDao, Scanner scanner, User currentUser) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.currentUser = currentUser;
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Administrator Menu (" + currentUser.getUsername() + ") ===");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "0" -> { running = false; System.out.println("Logged out."); }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- All System Users ---");
        printUserTable(userDao.getAllUsers());
    }

    private void viewPendingUsers() {
        System.out.println("\n--- Pending Registrations ---");
        printUserTable(userDao.getPendingUsers());
    }

    private void approveUser() {
        List<User> pending = userDao.getPendingUsers();
        System.out.println("\n--- Approve User Account ---");
        if (pending.isEmpty()) {
            System.out.println("No pending accounts to approve.");
            return;
        }
        printUserTable(pending);

        while (true) {
            System.out.print("Enter User ID to approve (0 to cancel): ");
            String input = scanner.nextLine().trim();
            int id;
            try {
                id = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }
            if (id == 0) {
                System.out.println("Approval cancelled.");
                return;
            }
            boolean inPendingList = pending.stream().anyMatch(u -> u.getUserId() == id);
            if (!inPendingList) {
                System.out.println("User ID " + id + " is not in the pending list. Try again.");
                continue;
            }
            if (userDao.approveUser(id)) {
                System.out.println("Success! User ID " + id + " has been APPROVED.");
            } else {
                System.out.println("Approval failed. Please try again.");
            }
            return;
        }
    }

    /** Aligned table: ID, Username, Role, Status. */
    private void printUserTable(List<User> users) {
        String border = "+-" + "-".repeat(5) + "-+-" + "-".repeat(15) + "-+-"
                + "-".repeat(12) + "-+-" + "-".repeat(10) + "-+";
        System.out.println(border);
        System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n", "ID", "Username", "Role", "Status");
        System.out.println(border);
        if (users.isEmpty()) {
            System.out.printf("| %-47s |%n", "No records found.");
        } else {
            for (User u : users) {
                System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n",
                        u.getUserId(), cut(u.getUsername(), 15), u.getRole(), u.getAccountStatus());
            }
        }
        System.out.println(border);
        System.out.println("Total: " + users.size());
    }

    private String cut(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max - 1) + "~" : s;
    }
}