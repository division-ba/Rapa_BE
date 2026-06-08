package io.eddie.unitybe.inventory.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.inventory.domain.InventoryItem;
import io.eddie.unitybe.inventory.domain.InventoryItemHistory;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.inventory.dto.InventoryPickupRequestDto;
import io.eddie.unitybe.inventory.dto.SellRequestDto;
import io.eddie.unitybe.inventory.repository.InventoryItemHistoryRepository;
import io.eddie.unitybe.inventory.repository.InventoryItemRepository;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.item.entity.ItemGrade;
import io.eddie.unitybe.item.entity.ItemType;
import io.eddie.unitybe.item.repository.ItemRepository;
import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.player.repository.PlayerRepository;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService 클래스의")
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private InventoryItemHistoryRepository inventoryItemHistoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerRepository playerRepository;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(
                inventoryItemRepository,
                inventoryItemHistoryRepository,
                itemRepository,
                userRepository,
                playerRepository
        );
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
            given(inventoryItemRepository.findAllByUserIdAndDeletedAtIsNull(1L))
                    .willReturn(List.of(inventoryItem(3)));

            List<InventoryItemResponseDto> responses = inventoryService.getInventory(1L);

            assertThat(responses.size()).isEqualTo(1);
            assertThat(responses.getFirst().userItemId()).isEqualTo(10L);
            assertThat(responses.getFirst().itemId()).isEqualTo(2L);
            assertThat(responses.getFirst().rId()).isEqualTo("potion_hp_001");
            assertThat(responses.getFirst().quantity()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("pickup 메서드는")
    class Pickup {

        private final InventoryPickupRequestDto request = new InventoryPickupRequestDto(2L, 5);

        @Test
        @DisplayName("처음 획득하는 아이템이면 새 인벤토리 아이템을 생성하고 획득 이력을 저장한다")
        void it_creates_new_inventory_item() {
            given(itemRepository.findByIdAndDeletedAtIsNull(2L)).willReturn(Optional.of(item()));
            given(inventoryItemRepository.findByUserIdAndItemId(1L, 2L)).willReturn(Optional.empty());
            given(userRepository.getReferenceById(1L)).willReturn(user());
            given(inventoryItemRepository.save(any(InventoryItem.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            InventoryItemResponseDto response = inventoryService.pickup(1L, request);

            assertThat(response.itemId()).isEqualTo(2L);
            assertThat(response.quantity()).isEqualTo(5);
            verify(inventoryItemRepository, times(1)).save(any(InventoryItem.class));
            verify(inventoryItemHistoryRepository, times(1)).save(any(InventoryItemHistory.class));
        }

        @Test
        @DisplayName("이미 보유한 아이템이면 수량을 누적하고 획득 이력을 저장한다")
        void it_accumulates_quantity_for_existing_item() {
            given(itemRepository.findByIdAndDeletedAtIsNull(2L)).willReturn(Optional.of(item()));
            given(inventoryItemRepository.findByUserIdAndItemId(1L, 2L))
                    .willReturn(Optional.of(inventoryItem(3)));

            InventoryItemResponseDto response = inventoryService.pickup(1L, request);

            assertThat(response.quantity()).isEqualTo(8);
            verify(inventoryItemRepository, never()).save(any(InventoryItem.class));
            verify(inventoryItemHistoryRepository, times(1)).save(any(InventoryItemHistory.class));
        }

        @Test
        @DisplayName("soft delete 된 아이템을 재획득하면 복구 후 수량을 재설정한다")
        void it_restores_soft_deleted_item() {
            InventoryItem deleted = inventoryItem(3);
            deleted.setDeletedAt(LocalDateTime.of(2026, 6, 5, 10, 0));
            given(itemRepository.findByIdAndDeletedAtIsNull(2L)).willReturn(Optional.of(item()));
            given(inventoryItemRepository.findByUserIdAndItemId(1L, 2L)).willReturn(Optional.of(deleted));

            InventoryItemResponseDto response = inventoryService.pickup(1L, request);

            assertThat(response.quantity()).isEqualTo(5);
            assertThat(deleted.getDeletedAt()).isNull();
            verify(inventoryItemHistoryRepository, times(1)).save(any(InventoryItemHistory.class));
        }

        @Test
        @DisplayName("수량이 1보다 작으면 INVALID_ITEM_QUANTITY 예외를 던진다")
        void it_throws_when_quantity_is_less_than_one() {
            InventoryPickupRequestDto invalid = new InventoryPickupRequestDto(2L, 0);

            assertThatThrownBy(() -> inventoryService.pickup(1L, invalid))
                    .isInstanceOf(DiversionException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ITEM_QUANTITY);
        }

        @Test
        @DisplayName("존재하지 않는 아이템이면 ITEM_NOT_FOUND 예외를 던진다")
        void it_throws_when_item_not_found() {
            given(itemRepository.findByIdAndDeletedAtIsNull(2L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.pickup(1L, request))
                    .isInstanceOf(DiversionException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("discard 메서드는")
    class Discard {

        @Test
        @DisplayName("수량을 차감하고 버리기 이력을 저장한다")
        void it_decreases_quantity_and_logs() {
            InventoryItem owned = inventoryItem(5);
            given(inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                    .willReturn(Optional.of(owned));

            inventoryService.discard(1L, 10L, 2);

            assertThat(owned.getQuantity()).isEqualTo(3);
            assertThat(owned.getDeletedAt()).isNull();
            verify(inventoryItemHistoryRepository, times(1)).save(any(InventoryItemHistory.class));
        }

        @Test
        @DisplayName("전부 버려서 수량이 0이 되면 soft delete 한다")
        void it_soft_deletes_when_quantity_reaches_zero() {
            InventoryItem owned = inventoryItem(2);
            given(inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                    .willReturn(Optional.of(owned));

            inventoryService.discard(1L, 10L, 2);

            assertThat(owned.getQuantity()).isEqualTo(0);
            assertThat(owned.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("보유 수량보다 많이 버리면 INSUFFICIENT_ITEM_QUANTITY 예외를 던진다")
        void it_throws_when_quantity_exceeds_owned() {
            given(inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                    .willReturn(Optional.of(inventoryItem(1)));

            assertThatThrownBy(() -> inventoryService.discard(1L, 10L, 2))
                    .isInstanceOf(DiversionException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_ITEM_QUANTITY);
        }

        @Test
        @DisplayName("보유하지 않은 항목이면 INVENTORY_ITEM_NOT_FOUND 예외를 던진다")
        void it_throws_when_not_owned() {
            given(inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.discard(1L, 10L, 1))
                    .isInstanceOf(DiversionException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("수량이 1보다 작으면 INVALID_ITEM_QUANTITY 예외를 던진다")
        void it_throws_when_quantity_is_less_than_one() {
            assertThatThrownBy(() -> inventoryService.discard(1L, 10L, 0))
                    .isInstanceOf(DiversionException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ITEM_QUANTITY);
        }
    }

    private Player player(long gold) {
        Player p = new Player(user());
        p.setGold(gold);
        return p;
    }

    @Nested
    @DisplayName("sell 메서드는")
    class Sell {

        // item()의 sellPrice = 10
        private final SellRequestDto request = new SellRequestDto(2);

        @Test
        @DisplayName("수량을 차감하고 sellPrice×수량 만큼 골드를 증가시키며 판매 이력을 저장한다")
        void it_sells_and_increases_gold() {
            InventoryItem owned = inventoryItem(5);
            Player player = player(1000L);
            given(inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                    .willReturn(Optional.of(owned));
            given(playerRepository.findById(1L)).willReturn(Optional.of(player));

            inventoryService.sell(1L, 10L, request);

            assertThat(owned.getQuantity()).isEqualTo(3);
            assertThat(player.getGold()).isEqualTo(1000L + 10L * 2);  // sellPrice 10 × 2
            assertThat(owned.getDeletedAt()).isNull();
            verify(inventoryItemHistoryRepository, times(1)).save(any(InventoryItemHistory.class));
        }

        @Test
        @DisplayName("전부 팔아서 수량이 0이 되면 soft delete 한다")
        void it_soft_deletes_when_quantity_reaches_zero() {
            InventoryItem owned = inventoryItem(2);
            given(inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                    .willReturn(Optional.of(owned));
            given(playerRepository.findById(1L)).willReturn(Optional.of(player(0L)));

            inventoryService.sell(1L, 10L, request);

            assertThat(owned.getQuantity()).isEqualTo(0);
            assertThat(owned.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("보유 수량보다 많이 팔면 INSUFFICIENT_ITEM_QUANTITY 예외를 던진다")
        void it_throws_when_quantity_exceeds_owned() {
            given(inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                    .willReturn(Optional.of(inventoryItem(1)));

            assertThatThrownBy(() -> inventoryService.sell(1L, 10L, request))
                    .isInstanceOf(DiversionException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_ITEM_QUANTITY);
        }

        @Test
        @DisplayName("보유하지 않은 항목이면 INVENTORY_ITEM_NOT_FOUND 예외를 던진다")
        void it_throws_when_not_owned() {
            given(inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.sell(1L, 10L, request))
                    .isInstanceOf(DiversionException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("수량이 1보다 작으면 INVALID_ITEM_QUANTITY 예외를 던진다")
        void it_throws_when_quantity_is_less_than_one() {
            assertThatThrownBy(() -> inventoryService.sell(1L, 10L, new SellRequestDto(0)))
                    .isInstanceOf(DiversionException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ITEM_QUANTITY);
        }
    }
}
