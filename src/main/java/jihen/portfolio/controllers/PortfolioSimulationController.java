package jihen.portfolio.controllers;


import jihen.portfolio.dtos.PortfolioScenarioDto;
import jihen.portfolio.dtos.PortfolioSimulationResponseDto;
import jihen.portfolio.services.PortfolioSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

    @RestController
    @RequestMapping("/api/portfolios")
    @RequiredArgsConstructor
    public class PortfolioSimulationController {

        private final PortfolioSimulationService portfolioSimulationService;

        @GetMapping("/scenarios")
        public ResponseEntity<Set<PortfolioScenarioDto>> getActiveScenarios() {
            return ResponseEntity.ok(portfolioSimulationService.getActiveScenarios());
        }

        @GetMapping("/{id}/simulate")
        public ResponseEntity<PortfolioSimulationResponseDto> simulatePortfolio(
                @PathVariable Long id,
                @RequestParam String scenarioKey
        ) {
            return ResponseEntity.ok(portfolioSimulationService.simulate(id, scenarioKey));
        }
    }

