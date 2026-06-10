package com.collabpm.backend.project.model;

import com.collabpm.backend.common.model.BaseEntity;
import com.collabpm.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "project_members")
public class ProjectMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole projectRole = ProjectRole.MEMBER;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt;

    protected ProjectMember() {
    }

    public ProjectMember(Project project, User user, ProjectRole projectRole, Instant joinedAt) {
        this.project = project;
        this.user = user;
        this.projectRole = projectRole;
        this.joinedAt = joinedAt;
    }

    public Project getProject() {
        return project;
    }

    public User getUser() {
        return user;
    }

    public ProjectRole getProjectRole() {
        return projectRole;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
