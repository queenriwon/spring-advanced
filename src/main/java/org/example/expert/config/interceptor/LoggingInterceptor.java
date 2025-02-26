package org.example.expert.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Long userId = (Long) request.getAttribute("userId");

        LocalDateTime now = LocalDateTime.now();
        String formatDatetime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("[관리자 접근] id = {}, 요청시각 = {}, URI = {}", userId, formatDatetime, request.getRequestURI());
        return true;
    }
}
