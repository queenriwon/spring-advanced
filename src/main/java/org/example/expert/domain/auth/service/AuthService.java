package org.example.expert.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.RefreshTokenRequest;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.config.exception.custom.AuthException;
import org.example.expert.config.exception.custom.InvalidRequestException;
import org.example.expert.domain.auth.dto.response.TokenResponse;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public TokenResponse signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new InvalidRequestException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        User user = new User(
                signupRequest.getEmail(),
                encodedPassword,
                userRole
        );
        userRepository.save(user);

        // 토큰발행
        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(SigninRequest signinRequest) {
        User user = findUserByEmailOrElseThrow(signinRequest.getEmail());

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }

        // 토큰 발생 및 저장
        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        return new TokenResponse(accessToken, refreshToken);
    }

    //로그아웃 추가 구현
    @Transactional
    public void logout(AuthUser authUser) {
        tokenService.revokeRefreshToken(authUser.getId());
    }

    @Transactional
    public TokenResponse reissueAccessToken(@RequestBody RefreshTokenRequest request) {
        User user = tokenService.reissueToken(request);

        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        return new TokenResponse(accessToken,refreshToken);
    }

    public User findUserByEmailOrElseThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new InvalidRequestException("가입되지 않은 유저입니다."));
    }
}