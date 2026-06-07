package io.eddie.unitybe.inventory.controller;

import io.eddie.unitybe.common.config.JwtAuthenticationFilter;
import io.eddie.unitybe.common.config.SecurityConfig;
import io.eddie.unitybe.common.config.entrypoint.JwtAccessDeniedHandler;
import io.eddie.unitybe.common.config.entrypoint.JwtAuthenticationEntryPoint;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.inventory.service.InventoryService;
import io.eddie.unitybe.user.domain.Role;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.dto.TokenBody;
import io.eddie.unitybe.user.service.TokenProvider;
import io.eddie.unitybe.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
@DisplayName("InventoryController 클래스의")
class InventoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InventoryService inventoryService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    TokenProvider tokenProvider;

    private static final String ACCESS_TOKEN = "access-token";
    private final AuthUser userDetails = new AuthUser(1L, "gamer@test.com", "password", "USER");

    private InventoryItemResponseDto inventoryItemResponse() {
        return new InventoryItemResponseDto(
                10L,
                2L,
                "potion_hp_001",
                "HP 포션",
                "CONSUMABLE",
                "COMMON",
                "HP를 회복합니다.",
                30,
                10,
                5,
                false,
                LocalDateTime.of(2026, 6, 4, 12, 0)
        );
    }

    private RequestPostProcessor user() {
        given(tokenProvider.validate(ACCESS_TOKEN)).willReturn(true);
        given(tokenProvider.parseJwt(ACCESS_TOKEN))
                .willReturn(new TokenBody(1L, "gamer@test.com", Role.USER));
        given(userService.loadUserByUsername("gamer@test.com")).willReturn(userDetails);

        return request -> {
            request.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
            return request;
        };
    }

    @Nested
    @DisplayName("GET /api/v1/users/me/inventory 엔드포인트는")
    class GetInventory {

        @Test
        @DisplayName("200 상태와 인벤토리 목록을 반환한다")
        void it_returns_inventory_items() throws Exception {
            given(inventoryService.getInventory(1L))
                    .willReturn(List.of(inventoryItemResponse()));

            mockMvc.perform(get("/api/v1/users/me/inventory").with(user()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].userItemId").value(10L))
                    .andExpect(jsonPath("$.data[0].itemId").value(2L))
                    .andExpect(jsonPath("$.data[0].rId").value("potion_hp_001"))
                    .andExpect(jsonPath("$.data[0].quantity").value(5));
        }

    }
}
