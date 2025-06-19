package com.example.movieee;

public class HelperClass {
    String username; // Đã thêm trường username
    String phone;
    String email;
    String password;
    String role;


    public HelperClass() {

    }


    public HelperClass(String username, String phone, String email, String password, String role) {
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}