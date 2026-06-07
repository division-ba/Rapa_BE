package io.eddie.unitybe.npc.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.npc.domain.Npc;
import io.eddie.unitybe.npc.dto.NpcResponseDto;
import io.eddie.unitybe.npc.repository.NpcRepository;
import io.eddie.unitybe.shop.repository.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NpcService {

    private final NpcRepository npcRepository;
    private final ShopItemRepository shopItemRepository;

    // 025: 활성 NPC 전체 + 각 NPC의 상점 아이템.
    public List<NpcResponseDto> getNpcs() {
        return npcRepository.findAllByActiveTrueAndDeletedAtIsNull().stream()
                .map(npc -> NpcResponseDto.from(npc,
                        shopItemRepository.findAllByNpcIdAndDeletedAtIsNullOrderBySortOrderAsc(npc.getId())))
                .toList();
    }

    // 026: NPC 단건 + 상점 아이템.
    public NpcResponseDto getNpc(Long id) {
        Npc npc = npcRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DiversionException(ErrorCode.NPC_NOT_FOUND));
        return NpcResponseDto.from(npc,
                shopItemRepository.findAllByNpcIdAndDeletedAtIsNullOrderBySortOrderAsc(npc.getId()));
    }
}
