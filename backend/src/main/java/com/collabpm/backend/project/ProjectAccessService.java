package com.collabpm.backend.project;

import com.collabpm.backend.project.model.ProjectMember;
import com.collabpm.backend.project.model.ProjectRole;
import com.collabpm.backend.project.repository.ProjectMemberRepository;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.SystemRole;
import com.collabpm.backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectAccessService {

    private final CurrentUserService currentUserService;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectAccessService(CurrentUserService currentUserService, ProjectMemberRepository projectMemberRepository) {
        this.currentUserService = currentUserService;
        this.projectMemberRepository = projectMemberRepository;
    }

    public User currentUser(Authentication authentication) {
        return currentUserService.getOrCreateCurrentUser(authentication);
    }

    public User ensureCanViewProject(Long projectId, Authentication authentication) {
        User user = currentUser(authentication);
        if (user.getSystemRole() == SystemRole.ADMINISTRATOR) {
            return user;
        }

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project");
        }

        return user;
    }

    public User ensureCanManageProject(Long projectId, Authentication authentication) {
        User user = currentUser(authentication);
        if (user.getSystemRole() == SystemRole.ADMINISTRATOR) {
            return user;
        }

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project managers can manage this project"));

        if (member.getProjectRole() != ProjectRole.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project managers can manage this project");
        }

        return user;
    }
}
