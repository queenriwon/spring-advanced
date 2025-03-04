package org.example.expert.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.exception.custom.InvalidRequestException;
import org.example.expert.domain.comment.dto.request.CommentRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TodoService todoService;

    @Transactional
    public CommentSaveResponse saveComment(AuthUser authUser, long todoId, CommentRequest commentRequest) {
        User user = User.fromAuthUser(authUser);
        Todo todo = todoService.findTodoByIdOrElseThrow(todoId);

        Comment newComment = new Comment(
                commentRequest.getContents(),
                user,
                todo
        );

        Comment savedComment = commentRepository.save(newComment);

        return new CommentSaveResponse(
                savedComment.getId(),
                savedComment.getContents(),
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(long todoId) {
        List<Comment> commentList = commentRepository.findByTodoIdWithUser(todoId);

        List<CommentResponse> dtoList = new ArrayList<>();
        for (Comment comment : commentList) {
            User user = comment.getUser();
            CommentResponse dto = new CommentResponse(
                    comment.getId(),
                    comment.getContents(),
                    new UserResponse(user.getId(), user.getEmail())
            );
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Transactional
    public CommentResponse updateComment(AuthUser authUser, Long commentId, CommentRequest commentRequest) {
        Comment comment = findCommentByIdOrElseThrow(commentId);

        if (!ObjectUtils.nullSafeEquals(comment.getUser().getId(), authUser.getId())) {
            throw new InvalidRequestException("댓글 작성자가 아닙니다.");
        }
        comment.update(commentRequest.getContents());

        return new CommentResponse(
                comment.getId(),
                comment.getContents(),
                new UserResponse(authUser.getId(), authUser.getEmail())
        );
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = findCommentByIdOrElseThrow(commentId);

        if (!ObjectUtils.nullSafeEquals(comment.getUser().getId(), userId)) {
            throw new InvalidRequestException("댓글 작성자가 아닙니다.");
        }
        commentRepository.delete(comment);
    }

    public Comment findCommentByIdOrElseThrow(Long commentId) {
        return commentRepository.findCommentById(commentId)
                .orElseThrow(() -> new InvalidRequestException("Comment not found"));
    }
}
