package io.eddie.unitybe.inventory.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.inventory.dto.InventoryPickupRequestDto;
import io.eddie.unitybe.inventory.dto.SellRequestDto;
import io.eddie.unitybe.inventory.service.InventoryService;
import io.eddie.unitybe.user.dto.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItemResponseDto>>> getInventory(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventory(authUser.getId())));
    }

    @PostMapping("/pickup")
    public ResponseEntity<ApiResponse<InventoryItemResponseDto>> pickup(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody InventoryPickupRequestDto request
    ) {
        InventoryItemResponseDto response = inventoryService.pickup(authUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("아이템을 획득했습니다.", response));
    }

    // 022: 버리기 — userItemId(Path) + quantity(Query). 응답 body 없음.
    @DeleteMapping("/{userItemId}/discard")
    public ResponseEntity<ApiResponse<Void>> discard(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long userItemId,
            @RequestParam int quantity
    ) {
        inventoryService.discard(authUser.getId(), userItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("아이템을 버렸습니다."));
    }

    // 028: 판매 — userItemId(Path) + quantity(Body). 응답 body 없음.
    @PostMapping("/{userItemId}/sell")
    public ResponseEntity<ApiResponse<Void>> sell(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long userItemId,
            @Valid @RequestBody SellRequestDto request
    ) {
        inventoryService.sell(authUser.getId(), userItemId, request);
        return ResponseEntity.ok(ApiResponse.success("아이템을 판매했습니다."));
    }
}
