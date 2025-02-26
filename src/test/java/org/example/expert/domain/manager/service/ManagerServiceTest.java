package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.config.exception.custom.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
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
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserService userService;
    @Mock
    private TodoService todoService;
    @InjectMocks
    private ManagerService managerService;

    @Test
    void manager_목록_조회_시_Todo가_없다면_InvalidRequestException_에러를_던진다() {
        // given
        long todoId = 1L;
//        given(todoRepository.findById(todoId)).willReturn(Optional.empty());
        given(todoService.findTodoByIdOrElseThrow(todoId)).willThrow(new InvalidRequestException("Todo not found"));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoService.findTodoByIdOrElseThrow(todoId)).willReturn(todo);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    void saveManager에서_유저와_일정을_작성한_유저가_같지_않을_때_IRE을_던지는가() {
        // given
        long userId = 1L;
        long todoId = 100L;

        AuthUser authUser = new AuthUser(userId, "email@email.com", UserRole.USER);
        Todo todo = new Todo("title", "contents", "weather", new User(2L));
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest();

        given(todoService.findTodoByIdOrElseThrow(anyLong())).willReturn(todo);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, managerSaveRequest),
                "담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.");
    }

    @Test
    void saveManager에서_유저와_매니저로_지정한_유저가_같을_때_IRE을_던지는가() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 1L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoService.findTodoByIdOrElseThrow(todoId)).willReturn(todo);
        given(userService.findUserByIdOrElseThrow(managerUserId)).willReturn(managerUser);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, managerSaveRequest),
                "일정 작성자는 본인을 담당자로 등록할 수 없습니다.");
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

//        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(todoService.findTodoByIdOrElseThrow(todoId)).willReturn(todo);
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoService.findTodoByIdOrElseThrow(todoId)).willReturn(todo);
        given(userService.findUserByIdOrElseThrow(managerUserId)).willReturn(managerUser);

        // when
        managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        Manager newManager = new Manager(managerUser, todo);
        ManagerSaveResponse managerSaveResponse = new ManagerSaveResponse(
                newManager.getId(),
                new UserResponse(managerUser.getId(), managerUser.getEmail()));

        assertThat(managerSaveResponse).isNotNull();
        assertThat(managerSaveResponse.getUser().getId()).isEqualTo(managerUserId);
        assertThat(managerSaveResponse.getUser().getEmail()).isEqualTo(managerUser.getEmail());

        verify(managerRepository).save(any(Manager.class));
    }

    @Test
    void findManagerByIdOrElseThrow에서_managerId를_이용해_성공적으로_조회할_수_있는가() {
        // given
        long managerId = 1L;
        Manager manager = new Manager(managerId);

        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // when
        Manager findManager = managerService.findManagerByIdOrElseThrow(managerId);

        // then
        assertThat(findManager).isNotNull();
        assertThat(findManager.getId()).isEqualTo(managerId);
    }

    @Test
    void deleteManager에서_manager를_성공적으로_삭제할_수_있는가() {
        // given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 100L;
        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        // Mocking
        given(todoService.findTodoByIdOrElseThrow(todoId)).willReturn(todo);
        given(userService.findUserByIdOrElseThrow(userId)).willReturn(user);
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));  // findById mock

        // when
        managerService.deleteManager(userId, todoId, managerId);

        // then
        verify(managerRepository).delete(manager);
    }

    @Test
    void deleteManager에서_유저와_일정을_작성한_유저가_같지_않을_때_IRE을_던지는가() {
        // given
        long managerId = 1L;
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", new User(2L));

        given(todoService.findTodoByIdOrElseThrow(anyLong())).willReturn(todo);
        given(userService.findUserByIdOrElseThrow(userId)).willReturn(user);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(userId, todoId, managerId),
                "해당 일정을 만든 유저가 유효하지 않습니다.");
    }

    @Test
    void deleteManager에서_todo의_user가_null인_경우_예외가_발생한다() {
        // given
        long managerId = 1L;
        long userId = 1L;
        User user = new User(userId);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", new User(2L));
        ReflectionTestUtils.setField(todo, "user", null);

        given(todoService.findTodoByIdOrElseThrow(anyLong())).willReturn(todo);
        given(userService.findUserByIdOrElseThrow(userId)).willReturn(user);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(userId, todoId, managerId),
                "해당 일정을 만든 유저가 유효하지 않습니다.");
    }

    @Test
    void deleteManager에서_매니저_엔티티의_할일과_매니저로_작성한_할일이_다를_때_IRE을_던지는가() {
        // given
        long userId = 1L;
        User user = new User(userId);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerId = 100L;
        Todo differentTodo = new Todo(2L);
        Manager manager = new Manager(user, differentTodo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(todoService.findTodoByIdOrElseThrow(anyLong())).willReturn(todo);
        given(userService.findUserByIdOrElseThrow(userId)).willReturn(user);
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));
        
        // when & then
        assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(userId, todoId, managerId),
                "해당 일정에 등록된 담당자가 아닙니다.");
    }
}
