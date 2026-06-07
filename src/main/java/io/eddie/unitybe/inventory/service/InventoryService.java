package io.eddie.unitybe.inventory.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.inventory.domain.InventoryItem;
import io.eddie.unitybe.inventory.domain.InventoryItemHistory;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.inventory.dto.InventoryPickupRequestDto;
import io.eddie.unitybe.inventory.repository.InventoryItemHistoryRepository;
import io.eddie.unitybe.inventory.repository.InventoryItemRepository;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.item.repository.ItemRepository;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
// @Transactional 은 "한 작업을 전부 성공 / 전부 실패(롤백)" 으로 묶는 장치다.
// 예) pickup 에서 수량은 늘렸는데 이력 저장 중 에러가 나면, 수량 늘린 것도 자동으로 취소된다. (데이터 꼬임 방지)
// 클래스 레벨에 readOnly=true 를 걸어두면 "조회 전용"이 기본값이 되어 빠르고 안전하다.
// 데이터를 바꾸는 메서드(pickup 등)에만 메서드 위에 @Transactional 을 따로 붙여 쓰기를 허용한다.
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemHistoryRepository inventoryItemHistoryRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    // 인벤토리 조회 : 내 것 중 안 버린 것(deletedAt == null) 전부를 응답 DTO 목록으로 변환해 반환한다.
    public List<InventoryItemResponseDto> getInventory(Long userId) {
        return inventoryItemRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .map(InventoryItemResponseDto::from)
                .toList();
    }

    // 아이템 획득(pickup) :
    // 클래스가 readOnly 라서 데이터를 바꾸는 이 메서드에만 @Transactional 을 따로 붙여 쓰기를 허용한다.
    @Transactional
    public InventoryItemResponseDto pickup(Long userId, InventoryPickupRequestDto request) {
        // 1) 수량 검증 : DTO 의 @Min(1) 로도 막지만, 서비스 단에서도 한 번 더 막아 단위 테스트로 검증 가능하게 한다.
        validateQuantity(request.quantity());

        // 2) 획득하려는 아이템이 실제로 존재하는지 확인. 없으면 ITEM_NOT_FOUND (전역 핸들러가 404 응답으로 변환).
        Item item = itemRepository.findByIdAndDeletedAtIsNull(request.itemId())
                .orElseThrow(() -> new DiversionException(ErrorCode.ITEM_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();

        // 3) (유저, 아이템) 조합으로 기존 보유분을 찾는다. (inventory_item 테이블은 이 조합에 유니크 제약)
        //    .map + .orElseGet 은 Optional 문법으로 풀어쓰면 => if (이미 있으면) 누적, else 새로 생성.
        InventoryItem inventoryItem = inventoryItemRepository.findByUserIdAndItemId(userId, item.getId())
                .map(existing -> accumulate(existing, request.quantity(), now))   // 보유 중이면 수량 누적
                .orElseGet(() -> create(userId, item, request.quantity(), now));  // 처음이면 새 row 생성

        // 4) 엔티티를 응답 DTO 로 변환해 반환.
        return InventoryItemResponseDto.from(inventoryItem);
    }

    // 이미 보유 중인 경우 : (필요하면 복구한 뒤) 수량을 누적하고 획득 이력을 남긴다.
    private InventoryItem accumulate(InventoryItem inventoryItem, int quantity, LocalDateTime now) {
        // 예전에 버려서 soft delete(deletedAt 기록) 된 행이면 먼저 되살린다. 이때 수량은 0으로 재설정된다.
        if (inventoryItem.getDeletedAt() != null) {
            inventoryItem.restore(now);
        }
        int before = inventoryItem.getQuantity();              // 누적 전 수량 (이력용)
        int after = inventoryItem.increaseQuantity(quantity);  // 누적 후 수량
        // 변경 자체는 더티 체킹으로 자동 반영되므로 inventoryItem 은 save 하지 않는다. 이력만 저장한다.
        inventoryItemHistoryRepository.save(InventoryItemHistory.acquired(inventoryItem, quantity, before, after));
        return inventoryItem;
    }

    // 처음 획득하는 경우 : 새 인벤토리 row 를 만들고 획득 이력을 남긴다.
    private InventoryItem create(Long userId, Item item, int quantity, LocalDateTime now) {
        // 로그인된 요청이라 user 존재는 이미 보장된다. getReferenceById 는 DB 조회 없이 프록시(껍데기)만 만들어
        // FK(user_id) 세팅에만 쓰므로, 불필요한 SELECT 한 번을 아낄 수 있다.
        User user = userRepository.getReferenceById(userId);
        InventoryItem inventoryItem = inventoryItemRepository.save(InventoryItem.create(user, item, quantity, now));
        // 새로 생긴 것이므로 before 는 0, after 는 획득 수량.
        inventoryItemHistoryRepository.save(InventoryItemHistory.acquired(inventoryItem, quantity, 0, quantity));
        return inventoryItem;
    }

    // 수량 검증 : null 이거나 1 미만이면 예외. (Integer 라서 null 도 함께 막는다)
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new DiversionException(ErrorCode.INVALID_ITEM_QUANTITY);
        }
    }
}
