package io.eddie.unitybe.dashboard.service;

import io.eddie.unitybe.dashboard.dto.ActivityCountDto;
import io.eddie.unitybe.dashboard.dto.DailySignupDto;
import io.eddie.unitybe.dashboard.dto.DashboardSummaryDto;
import io.eddie.unitybe.dashboard.dto.PopularItemDto;
import io.eddie.unitybe.dashboard.dto.RecentActivityDto;
import io.eddie.unitybe.inventory.domain.InventoryActivity;
import io.eddie.unitybe.inventory.repository.InventoryItemHistoryRepository;
import io.eddie.unitybe.purchase.repository.PurchaseHistoryRepository;
import io.eddie.unitybe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final InventoryItemHistoryRepository inventoryItemHistoryRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    public DashboardSummaryDto getSummary() {
        // 활동별 건수는 한 번만 조회해두고, 카드 숫자는 여기서 골라 쓴다.
        List<ActivityCountDto> activityCounts = inventoryItemHistoryRepository.countByActivity();

        List<PopularItemDto> popularItems = inventoryItemHistoryRepository.findPopularItems(
                List.of(InventoryActivity.ACQUIRED, InventoryActivity.PURCHASE),
                PageRequest.of(0, 10));

        List<RecentActivityDto> recentActivities = inventoryItemHistoryRepository
                .findRecentWithItem(PageRequest.of(0, 20)).stream()
                .map(RecentActivityDto::from)
                .toList();

        return new DashboardSummaryDto(
                userRepository.count(),
                countOf(activityCounts, InventoryActivity.ACQUIRED),
                countOf(activityCounts, InventoryActivity.PURCHASE),
                countOf(activityCounts, InventoryActivity.SALE),
                purchaseHistoryRepository.totalGoldSpent(),
                activityCounts,
                popularItems,
                recentActivities,
                recentSignups()
        );
    }

    // 최근 7일 일자별 가입자 수. 가입 시각을 날짜별로 묶고, 빈 날은 0으로 채운다(선생님 그래프처럼 연속 7일).
    private List<DailySignupDto> recentSignups() {
        Map<LocalDate, Long> byDay = userRepository.findAllCreatedAt().stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));

        DateTimeFormatter label = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        List<DailySignupDto> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            result.add(new DailySignupDto(day.format(label), byDay.getOrDefault(day, 0L)));
        }
        return result;
    }

    // 활동별 건수 목록에서 특정 활동의 건수만 뽑기 (없으면 0).
    private long countOf(List<ActivityCountDto> counts, InventoryActivity activity) {
        return counts.stream()
                .filter(c -> c.activity() == activity)
                .mapToLong(ActivityCountDto::count)
                .findFirst()
                .orElse(0L);
    }
}
