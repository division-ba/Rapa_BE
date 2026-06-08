package io.eddie.unitybe.dashboard.dto;

// 일자별 가입자 수 (그래프 B용). date = "MM-dd" 라벨.
public record DailySignupDto(
        String date,
        Long count
) {
}
