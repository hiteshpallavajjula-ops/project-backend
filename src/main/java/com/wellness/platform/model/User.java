package com.wellness.platform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    // Never serialize the password to JSON
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private boolean emailVerified = false;

    @Column(unique = true)
    private String phone;

    private boolean phoneVerified = false;

    // MFA Fields
    private boolean mfaEnabled = false;

    // Never expose the TOTP secret to the frontend
    @JsonIgnore
    private String mfaSecret;

    // Student's chosen trainer (which corresponds to an Admin User ID)
    private Long trainerId;

    // Base64 encoded profile photo
    @Column(columnDefinition = "LONGTEXT")
    private String profilePhoto;
}
