package io.eddie.unitybe.item.dto;

// 업로드된 JSON 파일의 각 원소를 받는 입력 상자.
// type/grade 가 enum 이 아니라 String 인 이유 : 파일의 원본 문자열을 일단 그대로 받고,
// 서비스에서 ItemType.fromString/ItemGrade.fromString 으로 enum 에 매핑하기 위함.
public record ItemInputRequest(
        Long id,
        String name,
        String rId,
        String description,
        Integer price,
        Integer sellPrice,
        String type,
        String grade
) {
}
