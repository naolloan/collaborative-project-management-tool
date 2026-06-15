package com.collabpm.backend.activity.repository;

import com.collabpm.backend.activity.model.ActivityLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    @Modifying
    @Query("update ActivityLog activity set activity.task = null where activity.task.id = :taskId")
    void clearTaskReference(@Param("taskId") Long taskId);
}
