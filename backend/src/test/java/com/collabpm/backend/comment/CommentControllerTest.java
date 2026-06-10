package com.collabpm.backend.comment;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabpm.backend.comment.dto.CommentResponse;
import com.collabpm.backend.comment.dto.CreateCommentRequest;
import com.collabpm.backend.config.SecurityConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    void listsTaskCommentsForAuthenticatedUsers() throws Exception {
        given(commentService.listTaskComments(eq(10L), any(Authentication.class))).willReturn(List.of(new CommentResponse(
            100L,
            10L,
            "Admin Admin",
            "This task needs a first draft.",
            Instant.parse("2026-06-09T14:00:00Z"))));

        mockMvc.perform(get("/api/tasks/10/comments").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].authorName").value("Admin Admin"))
            .andExpect(jsonPath("$[0].content").value("This task needs a first draft."));
    }

    @Test
    void createsTaskCommentsForAuthenticatedUsers() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("I started working on this.");
        given(commentService.createComment(eq(10L), eq(request), any(Authentication.class))).willReturn(new CommentResponse(
            101L,
            10L,
            "Admin Admin",
            request.content(),
            Instant.parse("2026-06-09T14:10:00Z")));

        mockMvc.perform(post("/api/tasks/10/comments")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "content": "I started working on this."
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(101))
            .andExpect(jsonPath("$.taskId").value(10))
            .andExpect(jsonPath("$.content").value("I started working on this."));
    }

    @Test
    void rejectsAnonymousCommentRequests() throws Exception {
        mockMvc.perform(get("/api/tasks/10/comments"))
            .andExpect(status().isUnauthorized());
    }
}
