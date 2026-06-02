package io.eddie.unitybe.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
        @NotNull
        @Email(message = "이메일 형식으로 입력해주세요.")
        String email,
        @NotNull
        @Size(min=6, max = 64, message = "비밀번호는 8~64자여야 합니다.")
        String password,
        @NotNull
        @Size(min=2, max = 20, message = "닉네임은 2~20자여야 합니다.")
        String nickname
) {
}
