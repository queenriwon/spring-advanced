package org.example.expert.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentAdminService {

    private final CommentService commentService;
    private final CommentRepository commentRepository;

    @Transactional
    public void deleteComment(long commentId) {
        commentService.findCommentByIdOrElseThrow(commentId);
        commentRepository.deleteById(commentId);
    }
}
