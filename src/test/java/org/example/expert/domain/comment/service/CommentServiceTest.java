package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.config.exception.custom.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoService todoService;
    @InjectMocks
    private CommentService commentService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentRequest request = new CommentRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoService.findTodoByIdOrElseThrow(anyLong())).willThrow(new InvalidRequestException("Todo not found"));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentRequest request = new CommentRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoService.findTodoByIdOrElseThrow(anyLong())).willReturn(todo);
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    void getComments에서_정상적으로_리스트가_출력될_수_있는가() {
        // given
        long todoId = 2L;
        User user = new User(1L, "email@email.com", UserRole.USER);
        Todo todo = new Todo(todoId);
        List<Comment> commentList = List.of(
                new Comment("content 1", user, todo),
                new Comment("content 2", user, todo)
        );

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(commentList);

        // when
        List<CommentResponse> commentResponseList = commentService.getComments(todoId);

        // then
        assertThat(commentResponseList.get(0).getContents()).isEqualTo("content 1");
        assertThat(commentResponseList.get(1).getContents()).isEqualTo("content 2");

        verify(commentRepository, times(1)).findByTodoIdWithUser(todoId);
    }

    @Test
    void updateComment에서_정상적으로_댓글_내용을_수정할_수_있는가() {
        // given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "email@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        long commentId = 2L;
        Comment comment = new Comment(commentId);
        ReflectionTestUtils.setField(comment, "user", user);
        CommentRequest commentRequest = new CommentRequest("contents");

        given(commentRepository.findCommentById(commentId)).willReturn(Optional.of(comment));

        // when
        CommentResponse commentResponse = commentService.updateComment(authUser, commentId, commentRequest);

        // then
        assertThat(commentResponse.getContents()).isEqualTo(commentRequest.getContents());

        verify(commentRepository, times(1)).findCommentById(commentId);
    }

    @Test
    void updateComment에서_유저와_댓글을_작성한_유저가_동일하지_않을_때_IRE를_던지는가() {
        // given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "email@email.com", UserRole.USER);

        long commentId = 2L;
        Comment comment = new Comment(commentId);
        ReflectionTestUtils.setField(comment, "user", new User(2L));
        CommentRequest commentRequest = new CommentRequest("contents");

        given(commentRepository.findCommentById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> commentService.updateComment(authUser, commentId, commentRequest),
                "댓글 작성자가 아닙니다.");

    }

    @Test
    void deleteComment에서_정상적으로_댓글을_삭제할_수_있는가() {
        // given
        long userId = 100L;
        long commentId = 1L;
        Comment comment = new Comment(commentId);
        ReflectionTestUtils.setField(comment, "user", new User(userId));

        given(commentRepository.findCommentById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(userId, commentId);

        // then
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void deleteComment에서_정유저와_댓글을_작성한_유저가_동일하지_않을_때_IRE를_던지는가() {
        // given
        long userId = 100L;
        long commentId = 1L;
        Comment comment = new Comment(commentId);
        ReflectionTestUtils.setField(comment, "user", new User(2L));

        given(commentRepository.findCommentById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> commentService.deleteComment(userId, commentId),
                "댓글 작성자가 아닙니다.");
    }

}
