package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CommentAdminServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentService commentService;
    @InjectMocks
    private CommentAdminService commentAdminService;

    @Test
    void deleteComment에서_정상적으로_댓글을_삭제할_수_있는가() {
        // given
        long commentId = 1L;
        Comment comment = new Comment(commentId);
        given(commentService.findCommentByIdOrElseThrow(anyLong())).willReturn(comment);

        // when
        commentAdminService.deleteComment(commentId);

        // then
        verify(commentRepository, times(1)).deleteById(commentId);
    }
    
}
