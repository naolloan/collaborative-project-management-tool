package com.collabpm.backend.project.repository;

import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.model.ProjectStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByStatusOrderByCreatedAtDesc(ProjectStatus status);
    List<Project> findDistinctByMembersUserIdAndStatusOrderByCreatedAtDesc(Long userId, ProjectStatus status);
}
