package io.eddie.unitybe.item.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.item.dto.ItemInputRequest;
import io.eddie.unitybe.item.dto.ItemResponse;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.item.entity.ItemGrade;
import io.eddie.unitybe.item.entity.ItemType;
import io.eddie.unitybe.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final ObjectMapper om;


    public List<ItemResponse> getItems() {
        return itemRepository.findAllByDeletedAtIsNull().stream()
                .map(ItemResponse::from)
                .toList();
    }

    public ItemResponse getItem(Long id) {
        return itemRepository.findByIdAndDeletedAtIsNull(id)
                .map(ItemResponse::from)
                .orElseThrow(() -> new DiversionException(ErrorCode.ITEM_NOT_FOUND));
    }

    @Transactional
    public void inputItems(MultipartFile file) throws IOException {
        String json = new String(file.getBytes(), StandardCharsets.UTF_8);

        List<Item> requests = om.readValue(
                json,
                new TypeReference<List<ItemInputRequest>>() {}
        ).stream()
                .map(item -> new Item(
                        item.name(),
                        item.rId(),
                        item.description(),
                        item.price(),
                        item.sellPrice(),
                        ItemType.fromString(item.type()),
                        ItemGrade.fromString(item.grade())
                ))
                .toList();

        itemRepository.saveAll(requests);
    }
}
