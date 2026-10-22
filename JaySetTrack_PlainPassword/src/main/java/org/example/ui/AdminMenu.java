package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

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
        System.out.println("\n[Administrator Menu] (to be implemented)");
        System.out.print("Press Enter to log out...");
        scanner.nextLine();
    }
}
