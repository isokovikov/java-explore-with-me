package ru.practicum.main_service.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.main_service.comment.model.Comment;

import java.util.List;
import java.util.Map;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAuthorId(Long userId);

    List<Comment> findAllByAuthorIdAndEventId(Long userId, Long eventId);

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);

    @Query("SELECT com.event.id, count(com) " +
            "FROM Comment com " +
            "WHERE com.event.id = ?1 " +
            "GROUP BY com.event.id")
    Map<Long, Integer> findAllCommentsByEventId(List<Long> eventsId);
}
