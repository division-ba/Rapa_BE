package io.eddie.unitybe.inventory.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.inventory.service.InventoryService;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItemResponseDto>>> getInventory(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventory(authUser.getId())));
    }
}
