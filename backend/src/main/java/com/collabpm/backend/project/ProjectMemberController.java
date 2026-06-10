package com.collabpm.backend.project;

import com.collabpm.backend.project.dto.AddProjectMemberRequest;
import com.collabpm.backend.project.dto.ProjectMemberResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @GetMapping
    public List<ProjectMemberResponse> listProjectMembers(@PathVariable Long projectId, Authentication authentication) {
        return projectMemberService.listProjectMembers(projectId, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberResponse addProjectMember(
        @PathVariable Long projectId,
        @Valid @RequestBody AddProjectMemberRequest request,
        Authentication authentication
    ) {
        return projectMemberService.addProjectMember(projectId, request, authentication);
    }
}
