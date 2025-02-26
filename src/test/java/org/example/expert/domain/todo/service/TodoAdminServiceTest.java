package org.example.expert.domain.todo.service;

import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
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
public class TodoAdminServiceTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private TodoService todoService;
    @InjectMocks
    private TodoAdminService todoAdminService;

    @Test
    void deleteTodo에서_일정을_정상적으로_삭제할_수_있는가() {
        // given
        long todoId = 1L;
        long userId = 100L;
        Todo todo = new Todo("title", "contents", "weather", new User(userId));
        given(todoService.findTodoByIdOrElseThrow(anyLong())).willReturn(todo);

        // when
        todoAdminService.deleteTodo(todoId);

        // then
        verify(todoRepository, times(1)).deleteById(todoId);
    }
}
