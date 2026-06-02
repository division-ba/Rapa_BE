package io.eddie.unitybe.item.repository;

import io.eddie.unitybe.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByDeletedAtIsNull();
    Optional<Item> findByIdAndDeletedAtIsNull(Long id);
}
