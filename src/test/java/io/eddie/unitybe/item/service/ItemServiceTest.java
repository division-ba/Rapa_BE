package io.eddie.unitybe.item.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.item.dto.ItemResponse;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.item.entity.ItemGrade;
import io.eddie.unitybe.item.entity.ItemType;
import io.eddie.unitybe.item.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemService 클래스의")
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private MultipartFile multipartFile;

    private final ObjectMapper om = new ObjectMapper();

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(itemRepository, om);
    }

    private Item item(Long id, String rid, String name) {
        return Item.builder()
                .id(id)
                .rid(rid)
                .name(name)
                .description("테스트 아이템")
                .price(100)
                .sellPrice(50)
                .type(ItemType.WEAPON)
                .grade(ItemGrade.COMMON)
                .build();
    }

    @Nested
    @DisplayName("getItems 메서드는")
    class GetItems {

        @Test
        @DisplayName("삭제되지 않은 아이템 목록을 응답 DTO로 반환한다")
        void it_returns_not_deleted_items() {
            given(itemRepository.findAllByDeletedAtIsNull())
                    .willReturn(List.of(item(1L, "sword_001", "연습용 검")));

            List<ItemResponse> responses = itemService.getItems();

            assertThat(responses.size()).isEqualTo(1);
            assertThat(responses.getFirst().itemId()).isEqualTo(1L);
            assertThat(responses.getFirst().rId()).isEqualTo("sword_001");
            assertThat(responses.getFirst().itemName()).isEqualTo("연습용 검");
            verify(itemRepository).findAllByDeletedAtIsNull();
        }
    }

    @Nested
    @DisplayName("getItem 메서드는")
    class GetItem {

        @Test
        @DisplayName("삭제되지 않은 아이템 단건을 응답 DTO로 반환한다")
        void it_returns_not_deleted_item() {
            given(itemRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(item(1L, "sword_001", "연습용 검")));

            ItemResponse response = itemService.getItem(1L);

            assertThat(response.itemId()).isEqualTo(1L);
            assertThat(response.rId()).isEqualTo("sword_001");
            verify(itemRepository).findByIdAndDeletedAtIsNull(1L);
        }

        @Test
        @DisplayName("아이템이 없으면 ITEM_NOT_FOUND 예외를 던진다")
        void it_throws_item_not_found() {
            given(itemRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

            DiversionException exception = assertThrows(
                    DiversionException.class,
                    () -> itemService.getItem(999L)
            );
            assertThat(exception.getMessage()).isEqualTo(ErrorCode.ITEM_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("inputItems 메서드는")
    class InputItems {

        @Test
        @DisplayName("업로드 JSON 파일을 Item 엔티티 목록으로 저장한다")
        @SuppressWarnings("unchecked")
        void it_saves_items_from_uploaded_json_file() throws Exception {
            String json = """
                    [
                      {
                        "id": 1,
                        "name": "연습용 검",
                        "rId": "sword_001",
                        "description": "연습용 검이다.",
                        "price": 100,
                        "sellPrice": 50,
                        "type": "WEAPON",
                        "grade": "COMMON"
                      }
                    ]
                    """;
            given(multipartFile.getBytes()).willReturn(json.getBytes(StandardCharsets.UTF_8));

            itemService.inputItems(multipartFile);

            ArgumentCaptor<List<Item>> captor = ArgumentCaptor.forClass(List.class);
            verify(itemRepository).saveAll(captor.capture());

            List<Item> savedItems = captor.getValue();
            assertThat(savedItems.size()).isEqualTo(1);
            assertThat(savedItems.getFirst().getId()).isNull();
            assertThat(savedItems.getFirst().getRid()).isEqualTo("sword_001");
            assertThat(savedItems.getFirst().getName()).isEqualTo("연습용 검");
            assertThat(savedItems.getFirst().getType()).isEqualTo(ItemType.WEAPON);
            assertThat(savedItems.getFirst().getGrade()).isEqualTo(ItemGrade.COMMON);
        }
    }
}
