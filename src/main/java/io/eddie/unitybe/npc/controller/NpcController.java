package io.eddie.unitybe.npc.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.npc.dto.NpcResponseDto;
import io.eddie.unitybe.npc.service.NpcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/npcs")
@RequiredArgsConstructor
public class NpcController {

    private final NpcService npcService;

    // 025: NPC 목록 조회 (인증 불필요)
    @GetMapping
    public ResponseEntity<ApiResponse<List<NpcResponseDto>>> getNpcs() {
        return ResponseEntity.ok(ApiResponse.success("NPC 목록을 조회했습니다.", npcService.getNpcs()));
    }

    // 026: NPC 단건 조회 (인증 불필요)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NpcResponseDto>> getNpc(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("NPC를 조회했습니다.", npcService.getNpc(id)));
    }
}
