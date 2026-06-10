package com.collabpm.backend.user;

import com.collabpm.backend.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String keycloakUserId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemRole systemRole = SystemRole.TEAM_MEMBER;

    protected User() {
    }

    public User(String keycloakUserId, String fullName, String email, SystemRole systemRole) {
        this.keycloakUserId = keycloakUserId;
        this.fullName = fullName;
        this.email = email;
        this.systemRole = systemRole;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public SystemRole getSystemRole() {
        return systemRole;
    }
}
