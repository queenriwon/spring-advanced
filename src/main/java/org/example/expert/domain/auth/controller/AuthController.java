package org.example.expert.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.auth.dto.request.RefreshTokenRequest;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.TokenResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auths")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public TokenResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
        return authService.signup(signupRequest);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody SigninRequest signinRequest) {
        return authService.login(signinRequest);
    }

    @PostMapping("/logout")
    public void logout(@Auth AuthUser authUser) {
        authService.logout(authUser);
    }

    @PostMapping("/refresh")
    public TokenResponse reissueAccessToken(@RequestBody RefreshTokenRequest request) {
        return authService.reissueAccessToken(request);
    }
}