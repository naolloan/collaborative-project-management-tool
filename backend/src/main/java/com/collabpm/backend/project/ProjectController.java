package com.collabpm.backend.project;

import com.collabpm.backend.project.dto.CreateProjectRequest;
import com.collabpm.backend.project.dto.ProjectResponse;
import com.collabpm.backend.project.dto.UpdateProjectRequest;
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
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> listProjects(Authentication authentication) {
        return projectService.listProjects(authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody CreateProjectRequest request, Authentication authentication) {
        return projectService.createProject(request, authentication);
    }

    @PatchMapping("/{projectId}")
    public ProjectResponse updateProject(
        @PathVariable Long projectId,
        @Valid @RequestBody UpdateProjectRequest request,
        Authentication authentication
    ) {
        return projectService.updateProject(projectId, request, authentication);
    }

    @PatchMapping("/{projectId}/archive")
    public ProjectResponse archiveProject(@PathVariable Long projectId, Authentication authentication) {
        return projectService.archiveProject(projectId, authentication);
    }
}
