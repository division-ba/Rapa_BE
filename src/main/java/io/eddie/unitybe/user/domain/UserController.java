package io.eddie.unitybe.user.domain;

import io.eddie.unitybe.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getI() {
        return new ResponseEntity<>(ApiResponse.success(
            Map.of(
                    "username","username", "password", "password"
            )
        ), HttpStatus.OK);
    }


}
