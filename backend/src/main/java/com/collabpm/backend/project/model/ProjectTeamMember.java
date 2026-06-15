package com.collabpm.backend.project.model;

import com.collabpm.backend.common.model.BaseEntity;
import com.collabpm.backend.organization.model.OrganizationalUnit;
import com.collabpm.backend.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "project_team_members",
    uniqueConstraints = @UniqueConstraint(columnNames = { "project_id", "team_id", "user_id" })
)
public class ProjectTeamMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private OrganizationalUnit team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected ProjectTeamMember() {
    }

    public ProjectTeamMember(Project project, OrganizationalUnit team, User user) {
        this.project = project;
        this.team = team;
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public OrganizationalUnit getTeam() {
        return team;
    }

    public User getUser() {
        return user;
    }
}
