package org.example.expert.domain.todo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoRequest {

    @NotBlank(message = "일정 제목은 필수 입력값입니다.")
    private String title;
    @NotBlank(message = "일정 내용은 필수 입력값입니다.")
    private String contents;
}
