package io.eddie.unitybe.inventory.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.inventory.dto.InventoryPickupRequestDto;
import io.eddie.unitybe.inventory.service.InventoryService;
import io.eddie.unitybe.user.dto.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
// 이 컨트롤러의 모든 URL 앞에 공통으로 붙는 경로. "내(me) 인벤토리" 라는 의미.
@RequestMapping("/api/v1/users/me/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // GET /api/v1/users/me/inventory : 내 인벤토리 목록 조회.
    // @AuthenticationPrincipal AuthUser : JWT 인증을 통과하면 시큐리티가 넣어주는 "현재 로그인한 사용자" 객체.
    // 로그인이 되었다는 것 자체가 user 가 존재한다는 뜻이라, 여기서 user 존재 여부를 또 확인하지 않는다.
    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItemResponseDto>>> getInventory(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventory(authUser.getId())));
    }

    // POST /api/v1/users/me/inventory/pickup : 아이템 획득.
    // @Valid : 요청 본문(DTO)에 붙은 검증 규칙(@NotNull, @Min)을 서비스 진입 전에 자동으로 검사한다.
    //          위반 시 MethodArgumentNotValidException 이 발생하고 전역 핸들러가 400 응답으로 변환한다.
    // @RequestBody : 클라이언트가 보낸 JSON 본문을 DTO 객체로 변환해 받는다.
    @PostMapping("/pickup")
    public ResponseEntity<ApiResponse<InventoryItemResponseDto>> pickup(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody InventoryPickupRequestDto request
    ) {
        InventoryItemResponseDto response = inventoryService.pickup(authUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("아이템을 획득했습니다.", response));
    }
}
