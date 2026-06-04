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
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> inputItems(@RequestPart(name = "file") MultipartFile file) throws IOException {
        itemService.inputItems(file);
        return new ResponseEntity<>(ApiResponse.success("success"), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItems() {
        return ResponseEntity.ok(ApiResponse.success(itemService.getItems()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(itemService.getItem(id)));
    }
}
