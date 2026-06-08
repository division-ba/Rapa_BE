package io.eddie.unitybe.npc.repository;

import io.eddie.unitybe.npc.domain.Npc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NpcRepository extends JpaRepository<Npc, Long> {

    // 025: 활성 + 삭제 안 된 NPC 전체.
    List<Npc> findAllByActiveTrueAndDeletedAtIsNull();

    // 026: 삭제 안 된 NPC 단건.
    Optional<Npc> findByIdAndDeletedAtIsNull(Long id);
}
