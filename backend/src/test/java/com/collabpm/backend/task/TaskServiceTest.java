package com.collabpm.backend.task;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.collabpm.backend.activity.ActivityService;
import com.collabpm.backend.activity.repository.ActivityLogRepository;
import com.collabpm.backend.comment.repository.CommentRepository;
import com.collabpm.backend.project.ProjectAccessService;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.repository.ProjectMemberRepository;
import com.collabpm.backend.project.repository.ProjectRepository;
import com.collabpm.backend.sprint.repository.SprintRepository;
import com.collabpm.backend.task.model.Task;
import com.collabpm.backend.task.repository.TaskRepository;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.User;
import com.collabpm.backend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class TaskServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectMemberRepository projectMemberRepository = mock(ProjectMemberRepository.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ActivityService activityService = mock(ActivityService.class);
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final ActivityLogRepository activityLogRepository = mock(ActivityLogRepository.class);
    private final ProjectAccessService projectAccessService = mock(ProjectAccessService.class);
    private final SprintRepository sprintRepository = mock(SprintRepository.class);
    private final TaskService taskService = new TaskService(
        taskRepository,
        projectRepository,
        projectMemberRepository,
        currentUserService,
        userRepository,
        activityService,
        commentRepository,
        activityLogRepository,
        projectAccessService,
        sprintRepository
    );

    @Test
    void deletesTaskAfterClearingCommentsAndActivityReferences() {
        Authentication authentication = mock(Authentication.class);
        Project project = mock(Project.class);
        Task task = mock(Task.class);
        User actor = mock(User.class);

        given(taskRepository.findById(11L)).willReturn(Optional.of(task));
        given(task.getProject()).willReturn(project);
        given(project.getId()).willReturn(1L);
        given(projectAccessService.ensureCanManageProject(1L, authentication)).willReturn(actor);

        taskService.deleteTask(11L, authentication);

        verify(activityService).recordTaskDeleted(task, actor);
        verify(commentRepository).deleteByTaskId(11L);
        verify(activityLogRepository).clearTaskReference(11L);
        verify(taskRepository).delete(task);
    }
}
