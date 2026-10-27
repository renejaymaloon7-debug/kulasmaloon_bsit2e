package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== JaySetTrack ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> login();
                case "2" -> register();
                case "0" -> { running = false; System.out.println("Goodbye!"); }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- Register ---");
        System.out.println("Select role: 1. Customer  2. Installation Technician  3. Administrator");
        System.out.print("Role: ");
        String role = switch (scanner.nextLine().trim()) {
            case "1" -> "CUSTOMER";
            case "2" -> "TECHNICIAN";
            case "3" -> "ADMIN";
            default -> null;
        };
        if (role == null) { System.out.println("Invalid role."); return; }

        System.out.print("Full name: ");      String fullName = scanner.nextLine().trim();
        System.out.print("Username: ");       String username = scanner.nextLine().trim();
        System.out.print("Password: ");       String password = scanner.nextLine();
        System.out.print("Contact number: "); String contact  = scanner.nextLine().trim();
        System.out.print("Address: ");        String address  = scanner.nextLine().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.out.println("Full name, username and password are required.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("Username already taken.");
            return;
        }
        if (userDao.register(fullName, username, password, contact, address, role)) {
            System.out.println("Registration successful! Your account is PENDING approval by the administrator.");
        } else {
            System.out.println("Registration failed.");
        }
    }

    private void login() {
        System.out.println("\n--- Login ---");
        System.out.print("Username: "); String username = scanner.nextLine().trim();
        System.out.print("Password: "); String password = scanner.nextLine();

        User user = userDao.login(username, password);
        if (user == null) { System.out.println("Invalid username or password."); return; }

        switch (user.getAccountStatus()) {
            case "PENDING"  -> { System.out.println("Your account is still pending approval."); return; }
            case "INACTIVE" -> { System.out.println("Your account has been deactivated."); return; }
            default -> {}
        }

        System.out.println("Welcome, " + user.getFullName() + "!");
        switch (user.getRole()) {
            case "ADMIN"      -> new AdminMenu(scanner, userDao, user).show();
            case "CUSTOMER"   -> new CustomerMenu(userDao, scanner, user).show();
            case "TECHNICIAN" -> new TechnicianMenu(userDao, scanner, user).show();
            default           -> System.out.println("Unknown role.");
        }
    }
}
