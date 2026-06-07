package io.eddie.unitybe.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 들어오는 요청 상자 : 무엇을(itemId) 얼마나(quantity) 획득할지만 담는다.
// record 는 "데이터만 담는 클래스"를 짧게 쓰는 문법으로, 게터 등이 자동 생성된다.
// 엔티티를 그대로 받지 않고 필요한 값만 골라 담는 이유 : 보안 + 불필요한 연관관계로 인한 변환 문제 방지.
public record InventoryPickupRequestDto(
        // @NotNull : 값이 비면 컨트롤러의 @Valid 단계에서 막혀 400 으로 응답된다(서비스까지 가지 않음).
        @NotNull(message = "아이템 ID는 필수입니다.")
        Long itemId,

        // @Min(1) : 최소 1개 이상만 허용. 0 이하 요청은 진입 자체를 차단한다.
        @NotNull(message = "아이템 수량은 필수입니다.")
        @Min(value = 1, message = "아이템 수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
