package org.example.expert.domain.todo.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.example.expert.domain.todo.service.TodoAdminService;
import org.example.expert.domain.todo.service.TodoService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TodoAdminController {

    private final TodoAdminService todoAdminService;

    @DeleteMapping("/admin/todos/{todoId}")
    public void deleteTodo(@PathVariable long todoId) {
        todoAdminService.deleteTodo(todoId);
    }
}
