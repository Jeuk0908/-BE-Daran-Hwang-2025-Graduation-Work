package org.example.greduatebe.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 리뷰 제출 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSubmitRequest {

    @NotNull(message = "rating은 필수입니다")
    @Min(value = 1, message = "rating은 1 이상이어야 합니다")
    @Max(value = 5, message = "rating은 5 이하여야 합니다")
    private Integer rating;

    @NotBlank(message = "ratingText는 필수입니다")
    @Size(max = 20, message = "ratingText는 20자 이하여야 합니다")
    private String ratingText;

    private String feedback;

    @NotNull(message = "hasFeedback은 필수입니다")
    private Boolean hasFeedback;
}
