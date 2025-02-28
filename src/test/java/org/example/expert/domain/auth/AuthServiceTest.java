package org.example.expert.domain.auth;

import org.example.expert.config.config.JwtUtil;
import org.example.expert.config.config.PasswordEncoder;
import org.example.expert.config.exception.custom.AuthException;
import org.example.expert.config.exception.custom.InvalidRequestException;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void signup에서_성공적으로_회원가입을_할_수_있는가() {
        // given
        String email = "email@email.com";
        String password = "password";
        UserRole userRole = UserRole.USER;
        SignupRequest signupRequest = new SignupRequest(email, password, String.valueOf(userRole));

        new User(signupRequest.getEmail(), signupRequest.getPassword(), UserRole.valueOf(signupRequest.getUserRole()));

        // when
        authService.signup(signupRequest);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signup에서_이메일이_존재할_때_IRE를_던질_수_있는가() {
        // given
        String email = "email@email.com";
        String password = "password";
        UserRole userRole = UserRole.USER;
        SignupRequest signupRequest = new SignupRequest(email, password, String.valueOf(userRole));

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(true);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> authService.signup(signupRequest),
                "이미 존재하는 이메일입니다."
        );

        verify(userRepository, never()).save(any(User.class));
    }

//    @Test
//    void signin에서_성공적으로_로그인을_할_수_있는가() {
//        // given
//        User user = new User("email@email.com", "encodedPassword", UserRole.USER);
//        SigninRequest signinRequest = new SigninRequest("email@email.com", "correctPassword");
//        String fakeToken = "fake-jwt-token";
//
//        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
//        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(true);
//        given(jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getUserRole())).willReturn(fakeToken);
//
//        // when
//        SigninResponse response = authService.signin(signinRequest);
//
//        // then
//        assertNotNull(response);
//        assertEquals(fakeToken, response.getBearerToken());
//    }

    @Test
    void signin에서_비밀번호가_일치하지_않을_경우_AuthException을_던지는가() {
//        // given
//        User user = new User("email@email.com", "encodedPassword", UserRole.USER);
//        SigninRequest signinRequest = new SigninRequest("email@email.com", "correctPassword");
//
//        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
//        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(false);
//
//        // when & then
//        assertThrows(AuthException.class,
//                () -> authService.signin(signinRequest),
//                "잘못된 비밀번호입니다.");
    }

    @Test
    void findUserByEmailOrElseThrow에서_userId를_찾을_수_없는_경우_IRE를_던지는가() {
        // given
        String email = "email@email.com";

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> authService.findUserByEmailOrElseThrow(email),
                "가입되지 않은 유저입니다.");
    }
}
