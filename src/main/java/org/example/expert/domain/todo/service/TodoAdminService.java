package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoAdminService {

    private final TodoRepository todoRepository;
    private final TodoService todoService;

    public void deleteTodo(long todoId) {
        todoService.findTodoByIdOrElseThrow(todoId);
        todoRepository.deleteById(todoId);
    }
}
