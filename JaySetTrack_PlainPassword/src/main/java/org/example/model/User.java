package org.example.model;

public class User {
    private int userId;
    private String fullName, username, passwordHash, contactNumber, address, role, accountStatus;

    public User(int userId, String fullName, String username, String passwordHash,
                String contactNumber, String address, String role, String accountStatus) {
        this.userId = userId; this.fullName = fullName; this.username = username;
        this.passwordHash = passwordHash; this.contactNumber = contactNumber;
        this.address = address; this.role = role; this.accountStatus = accountStatus;
    }

    public int getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getContactNumber() { return contactNumber; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
    public String getAccountStatus() { return accountStatus; }
}
