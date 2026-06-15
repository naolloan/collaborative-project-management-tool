package com.collabpm.backend.activity.model;

import com.collabpm.backend.common.model.BaseEntity;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.sprint.model.Sprint;
import com.collabpm.backend.task.model.Task;
import com.collabpm.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "activity_logs")
public class ActivityLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivitySubjectType subjectType;

    @Column(nullable = false)
    private String subjectName;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    protected ActivityLog() {
    }

    public ActivityLog(
        Project project,
        Sprint sprint,
        Task task,
        User actor,
        ActivityType actionType,
        ActivitySubjectType subjectType,
        String subjectName,
        String oldValue,
        String newValue
    ) {
        this.project = project;
        this.sprint = sprint;
        this.task = task;
        this.actor = actor;
        this.actionType = actionType;
        this.subjectType = subjectType;
        this.subjectName = subjectName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Project getProject() {
        return project;
    }

    public Sprint getSprint() {
        return sprint;
    }

    public Task getTask() {
        return task;
    }

    public User getActor() {
        return actor;
    }

    public ActivityType getActionType() {
        return actionType;
    }

    public ActivitySubjectType getSubjectType() {
        return subjectType;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void clearTask() {
        this.task = null;
    }
}
