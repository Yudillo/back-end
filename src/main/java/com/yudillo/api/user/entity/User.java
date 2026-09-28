package com.yudillo.api.user.entity;

import com.yudillo.api.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /** BCrypt 해시. 평문은 어떤 경우에도 저장하지 않는다. */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Builder
    private User(String email, String password, String name, Role role, boolean emailVerified) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role == null ? Role.USER : role;
        this.emailVerified = emailVerified;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }
}
