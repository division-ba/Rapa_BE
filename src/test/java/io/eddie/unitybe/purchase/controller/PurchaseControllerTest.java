package io.eddie.unitybe.purchase.controller;

import io.eddie.unitybe.common.config.JwtAuthenticationFilter;
import io.eddie.unitybe.common.config.SecurityConfig;
import io.eddie.unitybe.common.config.entrypoint.JwtAccessDeniedHandler;
import io.eddie.unitybe.common.config.entrypoint.JwtAuthenticationEntryPoint;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.purchase.dto.PurchaseRequestDto;
import io.eddie.unitybe.purchase.dto.PurchaseResponseDto;
import io.eddie.unitybe.purchase.service.PurchaseService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurchaseController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
@DisplayName("PurchaseController 클래스의")
class PurchaseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PurchaseService purchaseService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    TokenProvider tokenProvider;

    private static final String ACCESS_TOKEN = "access-token";
    private final AuthUser userDetails = new AuthUser(1L, "gamer@test.com", "password", "USER");

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

    private PurchaseResponseDto purchaseResponse() {
        return new PurchaseResponseDto(
                new PurchaseResponseDto.WalletDto(700L, 0L),
                new InventoryItemResponseDto(11L, 2L, "sword_001", "연습용 검", "WEAPON", "COMMON",
                        "연습용 검", 100, 50, 3, false, LocalDateTime.of(2026, 6, 8, 10, 0))
        );
    }

    @Nested
    @DisplayName("POST /api/v1/users/me/npcs/{npcId}/items/{npcItemId}/purchase 엔드포인트는")
    class Purchase {

        @Test
        @DisplayName("200 상태와 지갑/획득 아이템을 반환한다")
        void it_purchases_item() throws Exception {
            given(purchaseService.purchase(eq(1L), eq(1L), eq(5L), any(PurchaseRequestDto.class)))
                    .willReturn(purchaseResponse());

            mockMvc.perform(post("/api/v1/users/me/npcs/1/items/5/purchase")
                            .with(user())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"quantity\":3}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("구매가 완료되었습니다."))
                    .andExpect(jsonPath("$.data.wallet.gold").value(700))
                    .andExpect(jsonPath("$.data.wallet.gem").value(0))
                    .andExpect(jsonPath("$.data.acquiredItem.userItemId").value(11))
                    .andExpect(jsonPath("$.data.acquiredItem.quantity").value(3));
        }

        @Test
        @DisplayName("수량이 1보다 작으면 400 상태를 반환한다")
        void it_returns_400_when_quantity_is_less_than_one() throws Exception {
            mockMvc.perform(post("/api/v1/users/me/npcs/1/items/5/purchase")
                            .with(user())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"quantity\":0}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
