package org.example.expert.domain.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.dto.request.CommentRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/todos/{todoId}/comments")
    public ResponseEntity<CommentSaveResponse> saveComment(
            @Auth AuthUser authUser,
            @PathVariable long todoId,
            @Valid @RequestBody CommentRequest commentRequest
    ) {
        return ResponseEntity.ok(commentService.saveComment(authUser, todoId, commentRequest));
    }

    @GetMapping("/todos/{todoId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable long todoId) {
        return ResponseEntity.ok(commentService.getComments(todoId));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @Auth AuthUser authUser,
            @PathVariable Long commentId,
            @RequestBody CommentRequest commentRequest
    ) {
        return ResponseEntity.ok(commentService.updateComment(authUser, commentId, commentRequest));
    }

    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(
            @Auth AuthUser authUser,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(authUser.getId(), commentId);
    }
}
