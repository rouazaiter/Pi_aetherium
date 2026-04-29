package com.education.platform.dto.portfolio.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextBestMoveItemDto {
    private String label;
    private String type;
    private Integer priority;
    private String category;
    private String reason;
}
