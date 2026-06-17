package com.collabpm.backend.project;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.collabpm.backend.activity.ActivityService;
import com.collabpm.backend.project.dto.AddProjectMemberRequest;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.model.ProjectMember;
import com.collabpm.backend.project.model.ProjectRole;
import com.collabpm.backend.project.repository.ProjectMemberRepository;
import com.collabpm.backend.project.repository.ProjectRepository;
import com.collabpm.backend.project.repository.ProjectTeamMemberRepository;
import com.collabpm.backend.task.repository.TaskRepository;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.SystemRole;
import com.collabpm.backend.user.User;
import com.collabpm.backend.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

class ProjectMemberServiceTest {

    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectMemberRepository projectMemberRepository = mock(ProjectMemberRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final ProjectAccessService projectAccessService = mock(ProjectAccessService.class);
    private final ActivityService activityService = mock(ActivityService.class);
    private final ProjectTeamMemberRepository projectTeamMemberRepository = mock(ProjectTeamMemberRepository.class);
    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final ProjectMemberService projectMemberService = new ProjectMemberService(
        projectRepository,
        projectMemberRepository,
        userRepository,
        currentUserService,
        projectAccessService,
        activityService,
        projectTeamMemberRepository,
        taskRepository);

    @Test
    void rejectsProjectMembersWhoTryToManageMembers() {
        Authentication authentication = mock(Authentication.class);
        Project project = mock(Project.class);
        User currentUser = mock(User.class);

        given(projectRepository.findById(1L)).willReturn(Optional.of(project));
        given(currentUserService.getOrCreateCurrentUser(authentication)).willReturn(currentUser);
        given(currentUser.getId()).willReturn(20L);
        given(currentUser.getSystemRole()).willReturn(SystemRole.TEAM_MEMBER);
        given(projectMemberRepository.findByProjectIdAndUserId(1L, 20L))
            .willReturn(Optional.of(new ProjectMember(project, currentUser, ProjectRole.MEMBER, Instant.now())));

        assertThatThrownBy(() -> projectMemberService.addProjectMember(
            1L,
            new AddProjectMemberRequest("member@example.com", ProjectRole.MEMBER),
            authentication))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Only project managers can manage members");
    }

    @Test
    void allowsProjectManagersToManageMembers() {
        Authentication authentication = mock(Authentication.class);
        Project project = mock(Project.class);
        User currentUser = mock(User.class);
        User addedUser = mock(User.class);

        given(projectRepository.findById(1L)).willReturn(Optional.of(project));
        given(currentUserService.getOrCreateCurrentUser(authentication)).willReturn(currentUser);
        given(currentUser.getId()).willReturn(20L);
        given(currentUser.getSystemRole()).willReturn(SystemRole.TEAM_MEMBER);
        given(projectMemberRepository.findByProjectIdAndUserId(1L, 20L))
            .willReturn(Optional.of(new ProjectMember(project, currentUser, ProjectRole.MANAGER, Instant.now())));
        given(userRepository.findByEmailIgnoreCase("member@example.com")).willReturn(Optional.of(addedUser));
        given(addedUser.getId()).willReturn(21L);
        given(addedUser.getFullName()).willReturn("Team Member");
        given(addedUser.getEmail()).willReturn("member@example.com");
        given(projectMemberRepository.findByProjectIdAndUserId(1L, 21L)).willReturn(Optional.empty());
        given(projectMemberRepository.save(org.mockito.ArgumentMatchers.any(ProjectMember.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        projectMemberService.addProjectMember(
            1L,
            new AddProjectMemberRequest("member@example.com", ProjectRole.MEMBER),
            authentication);

        verify(projectMemberRepository).save(org.mockito.ArgumentMatchers.any(ProjectMember.class));
    }

    @Test
    void removesProjectMemberAndTeamAssignmentsWhenManagerDeletesMember() {
        Authentication authentication = mock(Authentication.class);
        Project project = mock(Project.class);
        User currentUser = mock(User.class);
        User removedUser = mock(User.class);
        ProjectMember memberToRemove = new ProjectMember(project, removedUser, ProjectRole.MEMBER, Instant.now());

        given(projectRepository.findById(1L)).willReturn(Optional.of(project));
        given(project.getId()).willReturn(1L);
        given(currentUserService.getOrCreateCurrentUser(authentication)).willReturn(currentUser);
        given(currentUser.getId()).willReturn(20L);
        given(currentUser.getSystemRole()).willReturn(SystemRole.TEAM_MEMBER);
        given(projectMemberRepository.findByProjectIdAndUserId(1L, 20L))
            .willReturn(Optional.of(new ProjectMember(project, currentUser, ProjectRole.MANAGER, Instant.now())));
        given(projectMemberRepository.findById(30L)).willReturn(Optional.of(memberToRemove));
        given(removedUser.getId()).willReturn(21L);
        given(removedUser.getFullName()).willReturn("Team Member");

        projectMemberService.removeProjectMember(1L, 30L, authentication);

        verify(projectTeamMemberRepository).deleteByProjectIdAndUserId(1L, 21L);
        verify(taskRepository).clearAssigneeForProjectMember(1L, 21L);
        verify(activityService).recordProjectMemberRemoved(memberToRemove, currentUser);
        verify(projectMemberRepository).deleteByProjectIdAndMemberId(1L, 30L);
    }
}
