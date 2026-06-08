package io.eddie.unitybe.purchase.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.inventory.domain.InventoryItem;
import io.eddie.unitybe.inventory.service.InventoryService;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.item.entity.ItemGrade;
import io.eddie.unitybe.item.entity.ItemType;
import io.eddie.unitybe.npc.domain.Npc;
import io.eddie.unitybe.npc.repository.NpcRepository;
import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.player.repository.PlayerRepository;
import io.eddie.unitybe.purchase.domain.PurchaseHistory;
import io.eddie.unitybe.purchase.dto.PurchaseRequestDto;
import io.eddie.unitybe.purchase.dto.PurchaseResponseDto;
import io.eddie.unitybe.purchase.repository.PurchaseHistoryRepository;
import io.eddie.unitybe.shop.domain.ShopItem;
import io.eddie.unitybe.shop.repository.ShopItemRepository;
import io.eddie.unitybe.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseService 클래스의")
class PurchaseServiceTest {

    @Mock
    private NpcRepository npcRepository;
    @Mock
    private ShopItemRepository shopItemRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PurchaseHistoryRepository purchaseHistoryRepository;
    @Mock
    private InventoryService inventoryService;

    private PurchaseService purchaseService;

    @BeforeEach
    void setUp() {
        purchaseService = new PurchaseService(
                npcRepository, shopItemRepository, playerRepository, purchaseHistoryRepository, inventoryService);
    }

    private final Long userId = 16L;
    private final Long npcId = 1L;
    private final Long npcItemId = 5L;
    private final PurchaseRequestDto request = new PurchaseRequestDto(3);  // 3개 구매

    private Item item(int price) {
        return Item.builder()
                .id(2L).rid("sword_001").name("연습용 검").description("연습용 검")
                .price(price).sellPrice(50)
                .type(ItemType.WEAPON).grade(ItemGrade.COMMON)
                .build();
    }

    private ShopItem shopItem(int stock, int price) {
        return ShopItem.builder()
                .id(npcItemId).item(item(price)).quantity(stock).sortOrder(0)
                .build();
    }

    private Player player(long gold) {
        Player player = new Player(new User(userId, "a@a.com", "pw", "유저a"));
        player.setGold(gold);
        return player;
    }

    private InventoryItem acquiredItem() {
        return InventoryItem.builder()
                .id(11L).user(new User(userId, "a@a.com", "pw", "유저a")).item(item(100))
                .quantity(3).equipped(false).acquiredAt(LocalDateTime.of(2026, 6, 8, 10, 0))
                .build();
    }

    @Test
    @DisplayName("정상 구매 시 골드와 재고를 차감하고 인벤토리에 추가하며 구매 이력을 저장한다")
    void it_purchases_successfully() {
        ShopItem shopItem = shopItem(10, 100);
        Player player = player(1000L);
        given(npcRepository.findByIdAndDeletedAtIsNull(npcId)).willReturn(Optional.of(mock(Npc.class)));
        given(shopItemRepository.findByIdAndNpcIdAndDeletedAtIsNull(npcItemId, npcId)).willReturn(Optional.of(shopItem));
        given(playerRepository.findById(userId)).willReturn(Optional.of(player));
        given(inventoryService.addPurchasedItem(eq(userId), any(Item.class), eq(3))).willReturn(acquiredItem());

        PurchaseResponseDto response = purchaseService.purchase(userId, npcId, npcItemId, request);

        assertThat(player.getGold()).isEqualTo(700L);       // 1000 - 100*3
        assertThat(shopItem.getQuantity()).isEqualTo(7);    // 10 - 3
        assertThat(response.wallet().gold()).isEqualTo(700L);
        assertThat(response.acquiredItem().quantity()).isEqualTo(3);
        verify(inventoryService).addPurchasedItem(eq(userId), any(Item.class), eq(3));
        verify(purchaseHistoryRepository).save(any(PurchaseHistory.class));
    }

    @Test
    @DisplayName("NPC가 없으면 NPC_NOT_FOUND 예외를 던진다")
    void it_throws_when_npc_not_found() {
        given(npcRepository.findByIdAndDeletedAtIsNull(npcId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.purchase(userId, npcId, npcItemId, request))
                .isInstanceOf(DiversionException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NPC_NOT_FOUND);
    }

    @Test
    @DisplayName("상점 아이템이 없으면 SHOP_ITEM_NOT_FOUND 예외를 던진다")
    void it_throws_when_shop_item_not_found() {
        given(npcRepository.findByIdAndDeletedAtIsNull(npcId)).willReturn(Optional.of(mock(Npc.class)));
        given(shopItemRepository.findByIdAndNpcIdAndDeletedAtIsNull(npcItemId, npcId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.purchase(userId, npcId, npcItemId, request))
                .isInstanceOf(DiversionException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("재고가 부족하면 SHOP_ITEM_OUT_OF_STOCK 예외를 던진다")
    void it_throws_when_out_of_stock() {
        given(npcRepository.findByIdAndDeletedAtIsNull(npcId)).willReturn(Optional.of(mock(Npc.class)));
        given(shopItemRepository.findByIdAndNpcIdAndDeletedAtIsNull(npcItemId, npcId)).willReturn(Optional.of(shopItem(2, 100)));

        assertThatThrownBy(() -> purchaseService.purchase(userId, npcId, npcItemId, request))
                .isInstanceOf(DiversionException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_ITEM_OUT_OF_STOCK);
    }

    @Test
    @DisplayName("골드가 부족하면 INSUFFICIENT_GOLD 예외를 던진다")
    void it_throws_when_insufficient_gold() {
        given(npcRepository.findByIdAndDeletedAtIsNull(npcId)).willReturn(Optional.of(mock(Npc.class)));
        given(shopItemRepository.findByIdAndNpcIdAndDeletedAtIsNull(npcItemId, npcId)).willReturn(Optional.of(shopItem(10, 100)));
        given(playerRepository.findById(userId)).willReturn(Optional.of(player(100L)));  // 100 < 300

        assertThatThrownBy(() -> purchaseService.purchase(userId, npcId, npcItemId, request))
                .isInstanceOf(DiversionException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_GOLD);
    }
}
