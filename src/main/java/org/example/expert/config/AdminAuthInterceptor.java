package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserRole userRole = UserRole.of((String) request.getAttribute("userRole"));

        log.info(String.valueOf(userRole));

        if(UserRole.ADMIN != userRole) {
            log.warn("관리자가 아닌 사용자 접근");
            throw new AuthException("관리자가 아닌 사용자 접근");
        }
        return true;
    }

}
