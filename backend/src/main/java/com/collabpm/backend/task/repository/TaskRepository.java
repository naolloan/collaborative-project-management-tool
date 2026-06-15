package com.collabpm.backend.task.repository;

import com.collabpm.backend.task.model.Task;
import com.collabpm.backend.task.model.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<Task> findByAssigneeId(Long assigneeId);
    long countBySprintId(Long sprintId);
    long countBySprintIdAndStatus(Long sprintId, TaskStatus status);
}
