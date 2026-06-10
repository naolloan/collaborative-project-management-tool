package com.collabpm.backend.project;

import com.collabpm.backend.project.dto.AddProjectMemberRequest;
import com.collabpm.backend.project.dto.ProjectMemberResponse;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.model.ProjectMember;
import com.collabpm.backend.project.model.ProjectRole;
import com.collabpm.backend.project.repository.ProjectMemberRepository;
import com.collabpm.backend.project.repository.ProjectRepository;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.SystemRole;
import com.collabpm.backend.user.User;
import com.collabpm.backend.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ProjectAccessService projectAccessService;

    public ProjectMemberService(
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        UserRepository userRepository,
        CurrentUserService currentUserService,
        ProjectAccessService projectAccessService
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.projectAccessService = projectAccessService;
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listProjectMembers(Long projectId, Authentication authentication) {
        ensureProjectExists(projectId);
        projectAccessService.ensureCanViewProject(projectId, authentication);
        return projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ProjectMemberResponse addProjectMember(
        Long projectId,
        AddProjectMemberRequest request,
        Authentication authentication
    ) {
        Project project = ensureProjectExists(projectId);
        User currentUser = currentUserService.getOrCreateCurrentUser(authentication);
        ensureCanManageProjectMembers(projectId, currentUser);

        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found. Ask the user to log in once before adding them to a project."));

        ProjectMember existingMember = projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
            .orElse(null);
        if (existingMember != null) {
            return toResponse(existingMember);
        }

        ProjectRole role = request.projectRole() == null ? ProjectRole.MEMBER : request.projectRole();
        ProjectMember member = new ProjectMember(project, user, role, Instant.now());
        return toResponse(projectMemberRepository.save(member));
    }

    private void ensureCanManageProjectMembers(Long projectId, User currentUser) {
        if (currentUser.getSystemRole() == SystemRole.ADMINISTRATOR) {
            return;
        }

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project managers can manage members"));

        if (member.getProjectRole() != ProjectRole.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project managers can manage members");
        }
    }

    private Project ensureProjectExists(Long projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private ProjectMemberResponse toResponse(ProjectMember member) {
        User user = member.getUser();
        return new ProjectMemberResponse(
            member.getId(),
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            member.getProjectRole().name(),
            member.getJoinedAt());
    }
}
