package com.collabpm.backend.activity;

import com.collabpm.backend.activity.dto.ActivityResponse;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/projects/{projectId}/activities")
    public List<ActivityResponse> listProjectActivities(@PathVariable Long projectId, Authentication authentication) {
        return activityService.listProjectActivities(projectId, authentication);
    }

    @GetMapping("/tasks/{taskId}/activities")
    public List<ActivityResponse> listTaskActivities(@PathVariable Long taskId, Authentication authentication) {
        return activityService.listTaskActivities(taskId, authentication);
    }
}
