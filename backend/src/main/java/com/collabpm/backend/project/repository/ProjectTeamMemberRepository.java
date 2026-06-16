package com.collabpm.backend.project.repository;

import com.collabpm.backend.project.model.ProjectTeamMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTeamMemberRepository extends JpaRepository<ProjectTeamMember, Long> {
    List<ProjectTeamMember> findByProjectIdOrderByTeamNameAscUserFullNameAsc(Long projectId);
    List<ProjectTeamMember> findByProjectIdAndTeamIdOrderByUserFullNameAsc(Long projectId, Long teamId);
    void deleteByProjectIdAndUserId(Long projectId, Long userId);
}
