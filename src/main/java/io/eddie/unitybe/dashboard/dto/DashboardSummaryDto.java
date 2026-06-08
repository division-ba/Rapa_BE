package io.eddie.unitybe.dashboard.dto;

import java.util.List;

// 대시보드 한 화면에 필요한 모든 집계를 묶은 그릇. 컨트롤러가 이걸 Thymeleaf에 넘긴다.
public record DashboardSummaryDto(
        // 상단 카드
        long totalUsers,
        long acquiredCount,
        long purchaseCount,
        long saleCount,
        long totalGoldSpent,
        // 표
        List<ActivityCountDto> activityCounts,
        List<PopularItemDto> popularItems,
        List<RecentActivityDto> recentActivities,
        // 그래프 B: 일자별 가입자 (최근 7일)
        List<DailySignupDto> dailySignups
) {
}
