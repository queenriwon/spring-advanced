package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoAdminService {

    private final TodoRepository todoRepository;

    public void deleteTodo(long todoId) {
        todoRepository.deleteById(todoId);
    }
}
