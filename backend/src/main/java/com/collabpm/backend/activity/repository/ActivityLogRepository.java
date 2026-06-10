package com.collabpm.backend.activity.repository;

import com.collabpm.backend.activity.model.ActivityLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    void deleteByTaskId(Long taskId);
}
