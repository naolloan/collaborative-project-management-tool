package com.collabpm.backend.comment.repository;

import com.collabpm.backend.comment.model.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    @Modifying
    @Query("delete from Comment comment where comment.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);
}
