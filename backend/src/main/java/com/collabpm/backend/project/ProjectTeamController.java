package com.collabpm.backend.project;

import com.collabpm.backend.project.dto.CreateProjectTeamRequest;
import com.collabpm.backend.project.dto.ProjectTeamResponse;
import com.collabpm.backend.project.dto.UpdateProjectTeamRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/teams")
public class ProjectTeamController {

    private final ProjectTeamService projectTeamService;

    public ProjectTeamController(ProjectTeamService projectTeamService) {
        this.projectTeamService = projectTeamService;
    }

    @GetMapping
    public List<ProjectTeamResponse> listProjectTeams(@PathVariable Long projectId, Authentication authentication) {
        return projectTeamService.listProjectTeams(projectId, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectTeamResponse createProjectTeam(
        @PathVariable Long projectId,
        @Valid @RequestBody CreateProjectTeamRequest request,
        Authentication authentication
    ) {
        return projectTeamService.createProjectTeam(projectId, request, authentication);
    }

    @PatchMapping("/{teamId}")
    public ProjectTeamResponse updateProjectTeam(
        @PathVariable Long projectId,
        @PathVariable Long teamId,
        @Valid @RequestBody UpdateProjectTeamRequest request,
        Authentication authentication
    ) {
        return projectTeamService.updateProjectTeam(projectId, teamId, request, authentication);
    }
}
