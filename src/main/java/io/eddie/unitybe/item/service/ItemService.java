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
// 클래스 기본은 조회 전용(readOnly). 데이터를 바꾸는 inputItems 에만 따로 @Transactional 을 붙여 쓰기 허용.
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    // ObjectMapper : JSON 문자열 <-> 자바 객체 변환기. 스프링이 빈으로 등록해둔 것을 주입받아 재사용한다.
    private final ObjectMapper om;


    // 아이템 전체 조회 : 삭제 안 된 것만 가져와 응답 DTO 목록으로 변환.
    public List<ItemResponse> getItems() {
        return itemRepository.findAllByDeletedAtIsNull().stream()
                .map(ItemResponse::from)
                .toList();
    }

    // 아이템 단건 조회 : 없으면 ITEM_NOT_FOUND 예외(전역 핸들러가 404 로 변환).
    public ItemResponse getItem(Long id) {
        return itemRepository.findByIdAndDeletedAtIsNull(id)
                .map(ItemResponse::from)
                .orElseThrow(() -> new DiversionException(ErrorCode.ITEM_NOT_FOUND));
    }

    // 아이템 일괄 등록 : 업로드된 JSON 파일을 읽어 여러 아이템을 한 번에 저장한다.
    @Transactional
    public void inputItems(MultipartFile file) throws IOException {
        // 1) 업로드된 파일의 바이트를 UTF-8 문자열(JSON)로 변환.
        String json = new String(file.getBytes(), StandardCharsets.UTF_8);

        // 2) JSON 배열을 ItemInputRequest 목록으로 역직렬화.
        //    new TypeReference<List<...>>(){} : 제네릭 타입(List 안의 타입)까지 알려주기 위한 Jackson 관용구.
        //    (제네릭은 런타임에 타입이 지워지므로, 이렇게 익명 클래스로 타입 정보를 박아 전달한다)
        List<Item> requests = om.readValue(
                json,
                new TypeReference<List<ItemInputRequest>>() {}
        ).stream()
                // 3) 요청 DTO 를 실제 저장할 Item 엔티티로 변환. 문자열 type/grade 는 enum 으로 매핑.
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

        // 4) 변환된 엔티티들을 한 번에 저장.
        itemRepository.saveAll(requests);
    }
}
