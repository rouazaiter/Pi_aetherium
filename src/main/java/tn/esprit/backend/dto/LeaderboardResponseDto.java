package tn.esprit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardResponseDto {
    private String type;
    private String category;
    private int days;
    private int limit;
    private LocalDateTime generatedAt;
    private List<LeaderboardEntryDto> entries;
}