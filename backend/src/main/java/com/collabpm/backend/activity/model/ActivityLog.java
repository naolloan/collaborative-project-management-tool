package com.collabpm.backend.activity.model;

import com.collabpm.backend.common.model.BaseEntity;
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
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType actionType;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    protected ActivityLog() {
    }

    public ActivityLog(Task task, User actor, ActivityType actionType, String oldValue, String newValue) {
        this.task = task;
        this.actor = actor;
        this.actionType = actionType;
        this.oldValue = oldValue;
        this.newValue = newValue;
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

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}
