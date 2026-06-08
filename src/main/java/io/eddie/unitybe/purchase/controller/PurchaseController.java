package io.eddie.unitybe.purchase.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.purchase.dto.PurchaseRequestDto;
import io.eddie.unitybe.purchase.dto.PurchaseResponseDto;
import io.eddie.unitybe.purchase.service.PurchaseService;
import io.eddie.unitybe.user.dto.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/npcs")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    // 027: NPC 상점 아이템 구매 (인증 필요)
    @PostMapping("/{npcId}/items/{npcItemId}/purchase")
    public ResponseEntity<ApiResponse<PurchaseResponseDto>> purchase(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long npcId,
            @PathVariable Long npcItemId,
            @Valid @RequestBody PurchaseRequestDto request
    ) {
        PurchaseResponseDto response = purchaseService.purchase(authUser.getId(), npcId, npcItemId, request);
        return ResponseEntity.ok(ApiResponse.success("구매가 완료되었습니다.", response));
    }
}
