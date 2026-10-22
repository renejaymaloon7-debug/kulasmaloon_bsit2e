package org.example;

import org.example.dao.DatabaseInitializer;
import org.example.dao.UserDao;
import org.example.ui.Authentication;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();             // 1. Initialize database
        System.out.println("Database initialized.");

        UserDao userDao = new UserDao();              // 2. Shared instances
        try (Scanner scanner = new Scanner(System.in)) {
            new Authentication(userDao, scanner).start();   // 3. Delegate
        }
    }
}
