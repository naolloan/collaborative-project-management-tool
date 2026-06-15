package com.collabpm.backend.sprint.repository;

import com.collabpm.backend.sprint.model.Sprint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectIdOrderByStartDateAscCreatedAtAsc(Long projectId);
}
