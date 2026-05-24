package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String fullName;
    private String avatarUrl;

    private String provider; // "LOCAL" hoặc "GOOGLE"
    private boolean enabled; // Kích hoạt tài khoản qua email

    private String verificationToken;
    private String resetPasswordToken;
}