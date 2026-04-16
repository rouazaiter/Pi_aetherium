package jihen.portfolio.controllers;


import jihen.portfolio.dtos.RelatedPortfoliosResponseDto;
import jihen.portfolio.services.RelatedPortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/api/portfolios")
    @RequiredArgsConstructor
    public class RelatedPortfolioController {

        private final RelatedPortfolioService relatedPortfolioService;

        @GetMapping("/{id}/related")
        public ResponseEntity<RelatedPortfoliosResponseDto> getRelatedPortfolios(
                @PathVariable Long id,
                @RequestParam(defaultValue = "4") Integer limit,
                @RequestParam(required = false) String excludeIds
        ) {
            return ResponseEntity.ok(
                    relatedPortfolioService.getRelatedPortfolios(id, limit, excludeIds)
            );
        }
    }

