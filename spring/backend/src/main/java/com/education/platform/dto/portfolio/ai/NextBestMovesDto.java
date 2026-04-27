package com.education.platform.dto.portfolio.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextBestMovesDto {
    private DeveloperFamily dominantFamily;
    private String summary;
    private List<NextBestMoveItemDto> coreMoves;
    private List<NextBestMoveItemDto> adjacentMoves;
    private List<NextBestMoveItemDto> expansionMoves;
}
