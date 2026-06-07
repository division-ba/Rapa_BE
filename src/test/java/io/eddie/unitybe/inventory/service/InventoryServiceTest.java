package io.eddie.unitybe.inventory.service;

import io.eddie.unitybe.inventory.dto.InventoryItemResponse;
import io.eddie.unitybe.inventory.entity.InventoryItem;
import io.eddie.unitybe.inventory.repository.InventoryItemRepository;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.item.entity.ItemGrade;
import io.eddie.unitybe.item.entity.ItemType;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService 클래스의")
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private UserRepository userRepository;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(inventoryItemRepository, userRepository);
    }

    private User user() {
        return new User(1L, "gamer@test.com", "encoded-password", "게이머");
    }

    private Item item() {
        return Item.builder()
                .id(2L)
                .rid("potion_hp_001")
                .name("HP 포션")
                .description("HP를 회복합니다.")
                .price(30)
                .sellPrice(10)
                .type(ItemType.CONSUMABLE)
                .grade(ItemGrade.COMMON)
                .build();
    }

    private InventoryItem inventoryItem(int quantity) {
        return InventoryItem.builder()
                .id(10L)
                .user(user())
                .item(item())
                .quantity(quantity)
                .equipped(false)
                .acquiredAt(LocalDateTime.of(2026, 6, 4, 10, 0))
                .build();
    }

    @Nested
    @DisplayName("getInventory 메서드는")
    class GetInventory {

        @Test
        @DisplayName("삭제되지 않은 인벤토리 아이템 목록을 반환한다")
        void it_returns_not_deleted_inventory_items() {
            given(userRepository.findByEmail("gamer@test.com")).willReturn(Optional.of(user()));
            given(inventoryItemRepository.findAllByUserIdAndDeletedAtIsNull(1L))
                    .willReturn(List.of(inventoryItem(3)));

            List<InventoryItemResponse> responses = inventoryService.getInventory("gamer@test.com");

            assertThat(responses.size()).isEqualTo(1);
            assertThat(responses.getFirst().userItemId()).isEqualTo(10L);
            assertThat(responses.getFirst().itemId()).isEqualTo(2L);
            assertThat(responses.getFirst().rId()).isEqualTo("potion_hp_001");
            assertThat(responses.getFirst().quantity()).isEqualTo(3);
        }
    }
}
