package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS Users (
                    UserID        INTEGER PRIMARY KEY AUTOINCREMENT,
                    FullName      TEXT NOT NULL,
                    Username      TEXT NOT NULL UNIQUE,
                    PasswordHash  TEXT NOT NULL,
                    ContactNumber TEXT,
                    Address       TEXT,
                    Role          TEXT NOT NULL CHECK (Role IN ('ADMIN','CUSTOMER','TECHNICIAN')),
                    AccountStatus TEXT NOT NULL DEFAULT 'PENDING'
                                  CHECK (AccountStatus IN ('PENDING','APPROVED','INACTIVE'))
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS ComputerSets (
                    ComputerSetID INTEGER PRIMARY KEY AUTOINCREMENT,
                    PackageName   TEXT NOT NULL,
                    Processor     TEXT,
                    RAM           TEXT,
                    Storage       TEXT,
                    MonitorSize   TEXT,
                    Accessories   TEXT,
                    Price         REAL NOT NULL CHECK (Price >= 0),
                    StockQuantity INTEGER NOT NULL DEFAULT 0 CHECK (StockQuantity >= 0),
                    Availability  TEXT NOT NULL DEFAULT 'AVAILABLE'
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS Orders (
                    OrderID              INTEGER PRIMARY KEY AUTOINCREMENT,
                    UserID               INTEGER NOT NULL,
                    OrderDate            TEXT NOT NULL,
                    TotalAmount          REAL NOT NULL DEFAULT 0,
                    OrderStatus          TEXT NOT NULL DEFAULT 'PENDING',
                    DeliveryAddress      TEXT,
                    ExpectedDeliveryDate TEXT,
                    FOREIGN KEY (UserID) REFERENCES Users(UserID)
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS OrderDetails (
                    OrderDetailID INTEGER PRIMARY KEY AUTOINCREMENT,
                    OrderID       INTEGER NOT NULL,
                    ComputerSetID INTEGER NOT NULL,
                    Quantity      INTEGER NOT NULL CHECK (Quantity > 0),
                    UnitPrice     REAL NOT NULL,
                    Subtotal      REAL NOT NULL,
                    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
                    FOREIGN KEY (ComputerSetID) REFERENCES ComputerSets(ComputerSetID)
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS Installations (
                    InstallationID      INTEGER PRIMARY KEY AUTOINCREMENT,
                    OrderID             INTEGER NOT NULL,
                    TechnicianID        INTEGER,
                    InstallationDate    TEXT,
                    InstallationAddress TEXT,
                    InstallationStatus  TEXT NOT NULL DEFAULT 'SCHEDULED',
                    CompletionDate      TEXT,
                    Remarks             TEXT,
                    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
                    FOREIGN KEY (TechnicianID) REFERENCES Users(UserID)
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS ActivityLogs (
                    LogID      INTEGER PRIMARY KEY AUTOINCREMENT,
                    UserID     INTEGER,
                    Action     TEXT NOT NULL,
                    RecordType TEXT,
                    RecordID   INTEGER,
                    Timestamp  TEXT NOT NULL DEFAULT (datetime('now','localtime')),
                    FOREIGN KEY (UserID) REFERENCES Users(UserID)
                )""");

            seedDefaultAdmin(conn);

        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Users WHERE Role = 'ADMIN'")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        String sql = "INSERT INTO Users (FullName, Username, PasswordHash, Role, AccountStatus) "
                + "VALUES (?, ?, ?, 'ADMIN', 'APPROVED')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "System Administrator");
            ps.setString(2, "admin");
            ps.setString(3, "admin123");
            ps.executeUpdate();
        }
    }
}