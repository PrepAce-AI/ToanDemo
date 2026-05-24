package com.example.demo.service;

import com.example.demo.entity.User;

public interface UserService {
    void register(User user, String siteUrl);
    boolean verify(String token);
    boolean sendForgotPasswordToken(String email, String siteUrl);
    boolean resetPassword(String token, String newPassword);
    void updateProfile(String email, String fullName, String avatarUrl);
    boolean changePassword(String email, String oldPassword, String newPassword);
    void processOAuthPostLogin(String email, String name);
}