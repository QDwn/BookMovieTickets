package com.example.bmtadr;

public class HelperClass {
    String phone;
    String email;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    String role;
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    String password;


    public HelperClass(String phone, String email, String password, String role) {
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.role = role;
    }


    public HelperClass() {
    }
}
