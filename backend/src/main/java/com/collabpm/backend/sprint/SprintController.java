package com.collabpm.backend.sprint;

import com.collabpm.backend.sprint.dto.CreateSprintRequest;
import com.collabpm.backend.sprint.dto.SprintResponse;
import com.collabpm.backend.sprint.dto.UpdateSprintRequest;
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
@RequestMapping("/api")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping("/projects/{projectId}/sprints")
    public List<SprintResponse> listProjectSprints(@PathVariable Long projectId, Authentication authentication) {
        return sprintService.listProjectSprints(projectId, authentication);
    }

    @PostMapping("/projects/{projectId}/sprints")
    @ResponseStatus(HttpStatus.CREATED)
    public SprintResponse createSprint(
        @PathVariable Long projectId,
        @Valid @RequestBody CreateSprintRequest request,
        Authentication authentication
    ) {
        return sprintService.createSprint(projectId, request, authentication);
    }

    @PatchMapping("/sprints/{sprintId}")
    public SprintResponse updateSprint(
        @PathVariable Long sprintId,
        @Valid @RequestBody UpdateSprintRequest request,
        Authentication authentication
    ) {
        return sprintService.updateSprint(sprintId, request, authentication);
    }
}
