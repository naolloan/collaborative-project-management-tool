package com.collabpm.backend.comment.repository;

import com.collabpm.backend.comment.model.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
    void deleteByTaskId(Long taskId);
}
