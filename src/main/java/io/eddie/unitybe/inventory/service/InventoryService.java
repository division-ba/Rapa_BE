package io.eddie.unitybe.inventory.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.inventory.dto.InventoryItemResponse;
import io.eddie.unitybe.inventory.repository.InventoryItemRepository;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;

    public List<InventoryItemResponse> getInventory(String email) {
        User user = getUser(email);

        return inventoryItemRepository.findAllByUserIdAndDeletedAtIsNull(user.getId()).stream()
                .map(InventoryItemResponse::from)
                .toList();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new DiversionException(ErrorCode.USER_NOT_FOUND_BY_EMAIL));
    }
}
