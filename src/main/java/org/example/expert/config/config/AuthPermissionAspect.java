package org.example.expert.config.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.config.exception.custom.InvalidRequestException;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuthPermissionAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(authPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, AuthPermission authPermission) throws Throwable {

        // 요청 로그 작성
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        Long userId = (Long) request.getAttribute("userId");            // 사용자 id
        String traceId = UUID.randomUUID().toString().substring(0, 8);      // 로그 번호
        String url = request.getRequestURI();                               // 사용 url
        LocalDateTime requestTime = LocalDateTime.now();                    // 현재시간
        String method = request.getMethod();                                // 메서드 이름
        String requestBody = objectMapper.writeValueAsString(joinPoint.getArgs());      // 응답바디
        if (!StringUtils.hasText(requestBody)) {
            requestBody = "empty";
        }

        log.info(toRequestLog(traceId, method, userId, url, requestTime, requestBody));


        UserRole role = authPermission.role();
        AuthUser authUser = (AuthUser) joinPoint.getArgs()[0];

        if (!role.equals(authUser.getUserRole())) {
            throw new InvalidRequestException("접근 불가능한 권한 유저");
        }


        Object result = joinPoint.proceed();
        String responseBody = objectMapper.writeValueAsString(result);
        if (!StringUtils.hasText(responseBody)) {
            responseBody = "empty";
        }

        log.info(toResponseLog(traceId, method, url, userId, requestTime, responseBody));

        return result;     // 이후 출력되는 메서드에서 예외를 던질 수 있음(Throwable)
    }


    private String toRequestLog(
            String traceId,
            String method,
            Long userId,
            String requestUrl,
            LocalDateTime requestTime,
            String requestBody
    ) {
        return String.format(
                "%n========== HTTP REQUEST LOG ==========%n" +
                        "Trace ID        : %s%n" +
                        "HTTP Method     : %s%n" +
                        "Request URI     : %s%n" +
                        "Request User ID : %s%n" +
                        "Request Time    : %s%n" +
                        "Request Body    : %s%n" +
                        "======================================",
                traceId, method, requestUrl, userId, requestTime, requestBody
        );
    }

    private String toResponseLog(
            String traceId,
            String method,
            String requestUrl,
            Long userId,
            LocalDateTime requestTime,
            Object result
    ) {
        return String.format(
                "%n========== HTTP RESPONSE LOG ==========%n" +
                        "Trace ID        : %s%n" +
                        "HTTP Method     : %s%n" +
                        "Request URI     : %s%n" +
                        "Request User ID : %s%n" +
                        "Request Time    : %s%n" +
                        "Response Body   : %s%n" +
                        "======================================",
                traceId, method, requestUrl, userId, requestTime, result
        );
    }
}
