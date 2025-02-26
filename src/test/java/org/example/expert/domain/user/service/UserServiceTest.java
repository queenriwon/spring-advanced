package org.example.expert.domain.user.service;

import org.example.expert.config.config.PasswordEncoder;
import org.example.expert.config.exception.custom.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.request.UserDeleteRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void findUserByIdOrElseThrow에서_userId값으로_정상적으로_유저를_조회하는가() {
        // given
        long userId = 1L;
        User user = new User(userId);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User findUser = userService.findUserByIdOrElseThrow(userId);

        // then
        assertThat(findUser).isNotNull();
        assertThat(findUser.getId()).isEqualTo(userId);
    }

    @Test
    void findUserByIdOrElseThrow에서_userId값이_없을_때_InvalidRequestException을_던지는가() {
        // given
        long userId = 1L;

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> userService.findUserByIdOrElseThrow(userId),
                "User not found");
    }

    @Test
    void getUser에서_정상적으로_userId값으로_dto를_조회하는가() {
        // given
        long userId = 1L;
        String email = "email@email";
        UserRole userRole = UserRole.USER;
        User user = new User(email, "password", userRole);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponse userResponseDto = userService.getUser(userId);

        // then
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getId()).isEqualTo(userId);
        assertThat(userResponseDto.getEmail()).isEqualTo(email);
    }

    @Test
    void changePassword에서_새비밀번호와_기존비밀번호가_같을_경우_IRE예외를_던지는가() {
        // given
        long userId = 1L;
        String oldPassword = "oldPassword";
        User user = new User(userId, "email@email.com", oldPassword, UserRole.USER);
        UserChangePasswordRequest dto = new UserChangePasswordRequest(oldPassword, oldPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(oldPassword, user.getPassword())).willReturn(true);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, dto),
                "새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
    }

    @Test
    void changePassword에서_입력한_비밀번호와_기존비밀번호가_다를_경우_IRE예외를_던지는가() {
        // given
        long userId = 1L;
        User user = new User(userId, "email@email.com", "oldPassword", UserRole.USER);
        String incorrectOldPassword = "incorrectOldPassword";
        UserChangePasswordRequest dto = new UserChangePasswordRequest(incorrectOldPassword, "newPassword");


        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getOldPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(dto.getNewPassword(), user.getPassword())).willReturn(false);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, dto),
                "잘못된 비밀번호입니다.");
    }

    @Test
    void changePassword에서_정상적으로_password수정을_할_수_있는가() {
        // given
        long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        User user = new User(userId, "email@email.com", oldPassword, UserRole.USER);
        UserChangePasswordRequest dto = new UserChangePasswordRequest(oldPassword, newPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(dto.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(newPassword)).willReturn("encodedNewPassword");

        // when
        userService.changePassword(userId, dto);

        // then
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");}

    @Test
    void deleteUser에서_입력한_비밀번호와_기존비밀번호가_다를_경우_IRE예외를_던지는가() {
        // given
        long userId = 1L;
        String password = "password";
        User user = new User(userId, "email@email.com", password, UserRole.USER);
        UserDeleteRequest dto = new UserDeleteRequest(password);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getPassword(), user.getPassword())).willReturn(false);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> userService.deleteUser(userId, dto),
                "잘못된 비밀번호입니다.");
    }

    @Test
    void deleteUser에서_정상적으로_유저를_삭제할_수_있는가() {
        // given
        long userId = 1L;
        String password = "password";
        User user = new User(userId, "email@email.com", password, UserRole.USER);
        UserDeleteRequest dto = new UserDeleteRequest(password);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getPassword(), user.getPassword())).willReturn(true);

        // when
        userService.deleteUser(userId, dto);

        // then
        verify(userRepository).delete(user);
    }
}
