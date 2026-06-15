package com.collabpm.backend.project;

import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.model.ProjectStatus;
import com.collabpm.backend.sprint.model.Sprint;
import com.collabpm.backend.sprint.model.SprintStatus;
import com.collabpm.backend.sprint.repository.SprintRepository;
import com.collabpm.backend.task.model.Task;
import com.collabpm.backend.task.model.TaskPriority;
import com.collabpm.backend.task.model.TaskStatus;
import com.collabpm.backend.task.repository.TaskRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProjectHealthService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;

    public ProjectHealthService(TaskRepository taskRepository, SprintRepository sprintRepository) {
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
    }

    public String computeHealth(Project project) {
        if (project == null || project.getId() == null) {
            return "ON_TRACK";
        }

        if (project.getStatus() == ProjectStatus.COMPLETED || project.getStatus() == ProjectStatus.ARCHIVED) {
            return "ON_TRACK";
        }

        LocalDate today = LocalDate.now();
        List<Task> tasks = taskRepository.findByProjectId(project.getId());
        List<Sprint> sprints = sprintRepository.findByProjectIdOrderByStartDateAscCreatedAtAsc(project.getId());

        boolean hasOpenTasks = tasks.stream().anyMatch(this::isOpenTask);
        boolean overdueTaskExists = tasks.stream().anyMatch((task) -> isOpenTask(task) && isOverdue(task.getDueDate(), today));
        boolean overdueSprintExists = sprints.stream().anyMatch((sprint) ->
            sprint.getStatus() != SprintStatus.COMPLETED && isOverdue(sprint.getEndDate(), today));
        boolean overdueProject = isOverdue(project.getDueDate(), today) && hasOpenTasks;

        if (overdueTaskExists || overdueSprintExists || overdueProject) {
            return "OFF_TRACK";
        }

        Optional<Sprint> activeSprint = activeSprint(sprints, today);
        if (activeSprint.isPresent()) {
            Sprint sprint = activeSprint.get();
            List<Task> sprintTasks = tasks.stream()
                .filter((task) -> task.getSprint() != null && sprint.getId().equals(task.getSprint().getId()))
                .toList();

            double sprintCompletion = completionPercentage(sprintTasks);
            long sprintHighPriorityOpen = sprintTasks.stream()
                .filter((task) -> isOpenTask(task) && task.getPriority() == TaskPriority.HIGH)
                .count();
            long daysLeft = daysUntil(sprint.getEndDate(), today);

            if ((daysLeft >= 0 && daysLeft <= 3 && sprintCompletion < 60.0) || sprintHighPriorityOpen >= 2) {
                return "AT_RISK";
            }
        }

        double projectCompletion = completionPercentage(tasks);
        long openHighPriorityTasks = tasks.stream()
            .filter((task) -> isOpenTask(task) && task.getPriority() == TaskPriority.HIGH)
            .count();
        long projectDaysLeft = daysUntil(project.getDueDate(), today);

        if ((projectDaysLeft >= 0 && projectDaysLeft <= 7 && projectCompletion < 70.0) || openHighPriorityTasks >= 3) {
            return "AT_RISK";
        }

        return "ON_TRACK";
    }

    private Optional<Sprint> activeSprint(List<Sprint> sprints, LocalDate today) {
        return sprints.stream()
            .filter((sprint) -> sprint.getStatus() == SprintStatus.ACTIVE)
            .findFirst()
            .or(() -> sprints.stream()
                .filter((sprint) ->
                    sprint.getStatus() != SprintStatus.COMPLETED
                        && sprint.getStartDate() != null
                        && !sprint.getStartDate().isAfter(today)
                        && (sprint.getEndDate() == null || !sprint.getEndDate().isBefore(today)))
                .findFirst());
    }

    private boolean isOpenTask(Task task) {
        return task.getStatus() != TaskStatus.DONE;
    }

    private boolean isOverdue(LocalDate date, LocalDate today) {
        return date != null && date.isBefore(today);
    }

    private long daysUntil(LocalDate date, LocalDate today) {
        if (date == null) {
            return Long.MAX_VALUE;
        }

        return ChronoUnit.DAYS.between(today, date);
    }

    private double completionPercentage(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return 100.0;
        }

        long completedTasks = tasks.stream()
            .filter((task) -> task.getStatus() == TaskStatus.DONE)
            .count();
        return (completedTasks * 100.0) / tasks.size();
    }
}
