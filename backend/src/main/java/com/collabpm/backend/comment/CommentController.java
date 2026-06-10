package com.collabpm.backend.comment;

import com.collabpm.backend.comment.dto.CommentResponse;
import com.collabpm.backend.comment.dto.CreateCommentRequest;
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
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentResponse> listTaskComments(@PathVariable Long taskId, Authentication authentication) {
        return commentService.listTaskComments(taskId, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
        @PathVariable Long taskId,
        @Valid @RequestBody CreateCommentRequest request,
        Authentication authentication
    ) {
        return commentService.createComment(taskId, request, authentication);
    }
}
