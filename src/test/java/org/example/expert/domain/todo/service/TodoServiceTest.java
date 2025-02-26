package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.config.exception.custom.InvalidRequestException;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.request.UserDeleteRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    @Test
    void findTodoByIdOrElseThrow에서_todoId값으로_정상적으로_할일을_조회하는가() {
        // given
        long todoId = 1L;
        Todo todo = new Todo(todoId);

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        // when
        Todo findTodo = todoService.findTodoByIdOrElseThrow(todoId);

        // then
        assertThat(findTodo).isNotNull();
        assertThat(findTodo.getId()).isEqualTo(todoId);
    }

    @Test
    void findTodoByIdOrElseThrow에서_todoId값이_없을_때_InvalidRequestException을_던지는가() {
        // given
        long userId = 1L;

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> todoService.findTodoByIdOrElseThrow(userId),
                "Todo not found");
    }

    /* User.fromAuthUser(authUser) 테스트로 옮기기 */
    @Test
    void saveTodo에서_fromAuthUser를_한_것과_유저정보가_같은가() {
        // given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "email@email.com", UserRole.USER);

        // when
        User user = User.fromAuthUser(authUser);

        // then
        assertThat(user.getId()).isEqualTo(authUser.getId());
        assertThat(user.getEmail()).isEqualTo(authUser.getEmail());
        assertThat(user.getUserRole()).isEqualTo(authUser.getUserRole());
    }

    @Test
    void saveTodo에서_유저를_저장할_수_있는가() {
        // given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "email@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        String weather = "weather";
        String title = "title";
        String contents = "contents";

        TodoRequest todoRequest = new TodoRequest(title, contents);
        Todo todo = new Todo(todoRequest.getTitle(), todoRequest.getContents(), weather, user);

        // when
        todoService.saveTodo(authUser, todoRequest);

        //then
        TodoSaveResponse todoSaveResponse = new TodoSaveResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail()));

        assertThat(todoSaveResponse).isNotNull();
        assertThat(todoSaveResponse.getId()).isEqualTo(todo.getId());
        assertThat(todoSaveResponse.getTitle()).isEqualTo(todo.getTitle());
        assertThat(todoSaveResponse.getContents()).isEqualTo(todo.getContents());
        assertThat(todoSaveResponse.getWeather()).isEqualTo(todo.getWeather());
    }

    @Test
    void getTodos에서_할일_리스트를_정상적으로_가져올_수_있는가() {
        // given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size);

        User user = new User(1L, "email@email.com", UserRole.USER);
        List<Todo> todoList = List.of(
                new Todo("title 1", "content 1", "weather1", user),
                new Todo("title 2", "content 2", "weather2", user)
        );

        Page<Todo> mockPage = new PageImpl<>(todoList, pageable, todoList.size());
        given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(mockPage);

        // when
        Page<TodoResponse> todosPage = todoService.getTodos(page, size);

        // then
        assertThat(todosPage.getContent()).hasSize(2);
        assertThat(todosPage.getTotalElements()).isEqualTo(2);
        assertThat(todosPage.getContent().get(0).getTitle()).isEqualTo("title 1");
        assertThat(todosPage.getContent().get(1).getTitle()).isEqualTo("title 2");

        verify(todoRepository, times(1)).findAllByOrderByModifiedAtDesc(pageable);

    }

    @Test
    void getTodo에서_todoId값으로_정상적으로_할일을_조회하는가() {
        // given
        long todoId = 1L;
        String title = "title";
        String contents = "contents";
        String weather = "weather";

        User user = new User(1L, "email@email.com", UserRole.USER);
        Todo todo = new Todo(title, contents, weather, user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when
        TodoResponse todoResponse = todoService.getTodo(todoId);

        // then
        assertThat(todoResponse).isNotNull();
        assertThat(todoResponse.getId()).isEqualTo(todoId);
        assertThat(todoResponse.getTitle()).isEqualTo(title);
        assertThat(todoResponse.getContents()).isEqualTo(contents);
        assertThat(todoResponse.getWeather()).isEqualTo(weather);
    }

    @Test
    void updateTodo에서_할일_제목과_할일_내용을_정상적으로_수정할_수_있는가() {
        // given
        long todoId = 1L;
        long userId = 1L;
        String weather = "weather";

        AuthUser authUser = new AuthUser(userId, "email@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "contents", weather, user);

        String newTitle = "New Title";
        String newContents = "New Contents";
        TodoRequest todoRequest = new TodoRequest(newTitle, newContents);

        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when
        TodoResponse updatedTodo = todoService.updateTodo(authUser, todoId, todoRequest);

        // then
        assertThat(updatedTodo.getTitle()).isEqualTo(newTitle);
        assertThat(updatedTodo.getContents()).isEqualTo(newContents);
        assertThat(updatedTodo.getWeather()).isEqualTo(weather);
        assertThat(updatedTodo.getUser().getId()).isEqualTo(userId);

        verify(todoRepository, times(1)).findByIdWithUser(todoId);
    }

    @Test
    void updateTodo에서_할일_제목만_정상적으로_수정할_수_있는가() {
        // given
        long todoId = 1L;
        long userId = 1L;
        String weather = "weather";

        AuthUser authUser = new AuthUser(userId, "email@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "contents", weather, user);

        String newTitle = "New Title";
        TodoRequest todoRequest = new TodoRequest(newTitle, null);

        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when
        TodoResponse updatedTodo = todoService.updateTodo(authUser, todoId, todoRequest);

        // then
        assertThat(updatedTodo.getTitle()).isEqualTo(newTitle);

        verify(todoRepository, times(1)).findByIdWithUser(todoId);
    }

    @Test
    void updateTodo에서_할일_내용만_정상적으로_수정할_수_있는가() {
        // given
        long todoId = 1L;
        long userId = 1L;
        String weather = "weather";

        AuthUser authUser = new AuthUser(userId, "email@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "contents", weather, user);

        String newContents = "New Contents";
        TodoRequest todoRequest = new TodoRequest(null, newContents);

        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when
        TodoResponse updatedTodo = todoService.updateTodo(authUser, todoId, todoRequest);

        // then
        assertThat(updatedTodo.getContents()).isEqualTo(newContents);

        verify(todoRepository, times(1)).findByIdWithUser(todoId);
    }

    @Test
    void updateTodo에서_할일_작성자가_아닐_때_IRE를_던질_수_있는가() {
        // given
        long userId = 1L;
        long todoId = 100L;

        AuthUser authUser = new AuthUser(userId, "email@email.com", UserRole.USER);
        User user = new User(2L);
        Todo todo = new Todo("title", "contents", "weather", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        TodoRequest todoRequest = new TodoRequest();

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> todoService.updateTodo(authUser, todoId, todoRequest),
                "일정 작성자가 아닙니다.");
    }

    @Test
    void deleteTodo에서_할일_내용만_정상적으로_수정할_수_있는가() {
        // given
        long todoId = 1L;
        long userId = 100L;
        Todo todo = new Todo("title", "contents", "weather", new User(userId));
        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when
        todoService.deleteTodo(userId, todoId);

        // then
        verify(todoRepository, times(1)).delete(todo);
    }

    @Test
    void deleteTodo에서_할일_작성자가_아닐_때_IRE를_던질_수_있는가() {
        // given
        long userId = 1L;
        long todoId = 100L;
        Todo todo = new Todo("title", "contents", "weather", new User(2L));
        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> todoService.deleteTodo(userId, todoId),
                "일정 작성자가 아닙니다.");
    }
}
