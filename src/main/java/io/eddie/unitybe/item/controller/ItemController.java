package io.eddie.unitybe.item.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.item.dto.ItemResponse;
import io.eddie.unitybe.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
// 이 컨트롤러의 모든 URL 앞에 공통으로 붙는 경로. 아이템(게임 내 마스터 데이터) 관련 API 묶음.
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // POST /api/v1/items : 아이템 마스터 데이터를 "파일 업로드"로 일괄 등록한다.
    // @RequestPart : multipart/form-data 요청에서 "file" 파트(업로드된 파일)를 꺼내 받는다.
    // MultipartFile : 업로드된 파일을 메모리/임시저장으로 들고 있는 객체. (여기선 JSON 파일을 받음)
    // 성공 시 201 Created 로 응답 (새 리소스가 생성되었다는 의미의 상태 코드).
    // throws IOException : 파일을 읽다가 입출력 오류가 나면 호출한 쪽으로 예외를 던진다(전역 핸들러가 처리).
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> inputItems(@RequestPart(name = "file") MultipartFile file) throws IOException {
        itemService.inputItems(file);
        return new ResponseEntity<>(ApiResponse.success("success"), HttpStatus.CREATED);
    }

    // GET /api/v1/items : 삭제되지 않은 아이템 전체 목록 조회.
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItems() {
        return ResponseEntity.ok(ApiResponse.success(itemService.getItems()));
    }

    // GET /api/v1/items/{id} : 아이템 단건 조회.
    // @PathVariable : URL 경로의 {id} 자리 값을 메서드 파라미터로 받는다. (예: /items/5 → id=5)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(itemService.getItem(id)));
    }
}
