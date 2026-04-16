package jihen.portfolio.dtos;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedPortfoliosResponseDto {

    private Long sourcePortfolioId;
    private Integer count;
    private Set<RelatedPortfolioDto> portfolios;

}

