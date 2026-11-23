package com.training.featuretoggle;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_user")
@Data
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;   // stored hashed, never plain text
    private String fullName;
    private String village;
    private String role;      // "ADMIN" or "FARMER"
}