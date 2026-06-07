package io.eddie.unitybe.inventory.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.inventory.dto.InventoryItemResponse;
import io.eddie.unitybe.inventory.service.InventoryService;
import io.eddie.unitybe.user.dto.AuthUser;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getInventory(
            @AuthenticationPrincipal AuthUser userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventory(getEmail(userDetails))));
    }

    private String getEmail(AuthUser userDetails) {
        if (userDetails == null) {
            throw new DiversionException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        return userDetails.getUsername();
    }
}
