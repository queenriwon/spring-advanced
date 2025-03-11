package org.example.expert.domain.todo.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.config.AuthPermission;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.service.TodoAdminService;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TodoAdminController {

    private final TodoAdminService todoAdminService;

    @AuthPermission(role = UserRole.ADMIN)
    @DeleteMapping("/admin/todos/{todoId}")
    public void deleteTodo(
            @Auth AuthUser authUser,
            @PathVariable long todoId) {
        todoAdminService.deleteTodo(todoId);
    }
}
