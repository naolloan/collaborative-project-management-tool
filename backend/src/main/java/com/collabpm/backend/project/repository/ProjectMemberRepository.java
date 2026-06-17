package com.collabpm.backend.project.repository;

import com.collabpm.backend.project.model.ProjectMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectIdOrderByJoinedAtAsc(Long projectId);
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    @Modifying
    @Query("delete from ProjectMember member where member.project.id = :projectId and member.id = :memberId")
    void deleteByProjectIdAndMemberId(@Param("projectId") Long projectId, @Param("memberId") Long memberId);
}
