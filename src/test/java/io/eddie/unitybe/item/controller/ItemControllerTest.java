package io.eddie.unitybe.item.controller;

import io.eddie.unitybe.common.config.SecurityConfig;
import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.item.dto.ItemResponse;
import io.eddie.unitybe.item.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(SecurityConfig.class)
@DisplayName("ItemController 클래스의")
class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ItemService itemService;

    private ItemResponse itemResponse() {
        return new ItemResponse(
                1L,
                "sword_001",
                "연습용 검",
                "WEAPON",
                "COMMON",
                "연습용 검이다.",
                100,
                50
        );
    }

    @Nested
    @DisplayName("GET /api/v1/items 엔드포인트는")
    class GetItems {

        @Test
        @DisplayName("200 상태와 아이템 목록을 반환한다")
        void it_returns_item_list() throws Exception {
            given(itemService.getItems()).willReturn(List.of(itemResponse()));

            mockMvc.perform(get("/api/v1/items"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.data[0].itemId").value(1L))
                    .andExpect(jsonPath("$.data[0].rId").value("sword_001"))
                    .andExpect(jsonPath("$.data[0].itemName").value("연습용 검"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/items/{id} 엔드포인트는")
    class GetItem {

        @Test
        @DisplayName("200 상태와 아이템 단건을 반환한다")
        void it_returns_item() throws Exception {
            given(itemService.getItem(1L)).willReturn(itemResponse());

            mockMvc.perform(get("/api/v1/items/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.itemId").value(1L))
                    .andExpect(jsonPath("$.data.rId").value("sword_001"));
        }

        @Test
        @DisplayName("아이템이 없으면 404 상태를 반환한다")
        void it_returns_404_when_item_not_found() throws Exception {
            given(itemService.getItem(999L)).willThrow(new DiversionException(ErrorCode.ITEM_NOT_FOUND));

            mockMvc.perform(get("/api/v1/items/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(ErrorCode.ITEM_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/items 엔드포인트는")
    class InputItems {

        @Test
        @DisplayName("multipart 파일을 받아 201 상태를 반환한다")
        void it_uploads_item_file() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "ItemsData.json",
                    MediaType.APPLICATION_JSON_VALUE,
                    "[]".getBytes(StandardCharsets.UTF_8)
            );
            doNothing().when(itemService).inputItems(file);

            mockMvc.perform(multipart("/api/v1/items").file(file).with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("success"));

            verify(itemService).inputItems(file);
        }
    }
}
