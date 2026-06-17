package com.collabpm.backend.task.repository;

import com.collabpm.backend.task.model.Task;
import com.collabpm.backend.task.model.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<Task> findByAssigneeId(Long assigneeId);
    long countBySprintId(Long sprintId);
    long countBySprintIdAndStatus(Long sprintId, TaskStatus status);

    @Modifying
    @Query("update Task task set task.assignee = null where task.project.id = :projectId and task.assignee.id = :userId")
    void clearAssigneeForProjectMember(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
