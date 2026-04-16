package jihen.portfolio.controllers;
import jihen.portfolio.dtos.ProjectImpactDto;
import jihen.portfolio.entities.PortfolioProject;
import jihen.portfolio.services.PortfolioProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/portfolio-projects")
public class PortfolioProjectController {
    @Autowired
    private PortfolioProjectService portfolioProjectService;
    @Autowired
    private PortfolioProjectService projectService;

    @GetMapping("/{id}/impact")
    public ResponseEntity<ProjectImpactDto> getProjectImpact(@PathVariable Long id) {
        ProjectImpactDto impact = portfolioProjectService.calculateImpact(id);
        return ResponseEntity.ok(impact);
    }
    @PostMapping("/portfolio/{portfolioId}")
    public ResponseEntity<PortfolioProject> createProject(@PathVariable Long portfolioId, @RequestBody PortfolioProject project) {
        PortfolioProject created = projectService.createProject(portfolioId, project);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<PortfolioProject>> getProjectsByPortfolioId(@PathVariable Long portfolioId) {
        List<PortfolioProject> projects = projectService.getProjectsByPortfolioId(portfolioId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioProject> getProjectById(@PathVariable Long id) {
        Optional<PortfolioProject> project = projectService.getProjectById(id);
        return project.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioProject> updateProject(@PathVariable Long id, @RequestBody PortfolioProject project) {
        PortfolioProject updated = projectService.updateProject(id, project);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{id}/pin")
    public ResponseEntity<Void> pinProject(@PathVariable Long id) {
        projectService.pinProject(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unpin")
    public ResponseEntity<Void> unpinProject(@PathVariable Long id) {
        projectService.unpinProject(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/portfolio/{portfolioId}/pinned")
    public ResponseEntity<List<PortfolioProject>> getPinnedProjects(@PathVariable Long portfolioId) {
        List<PortfolioProject> projects = projectService.getPinnedProjects(portfolioId);
        return ResponseEntity.ok(projects);
    }

    // ========== STATISTIQUES ==========

    @PostMapping("/{id}/increment-views")
    public ResponseEntity<Void> incrementViews(@PathVariable Long id) {
        projectService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/increment-likes")
    public ResponseEntity<Void> incrementLikes(@PathVariable Long id) {
        projectService.incrementLikes(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/portfolio/{portfolioId}/most-viewed")
    public ResponseEntity<List<PortfolioProject>> getMostViewedProjects(@PathVariable Long portfolioId) {
        List<PortfolioProject> projects = projectService.getMostViewedProjects(portfolioId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/portfolio/{portfolioId}/most-liked")
    public ResponseEntity<List<PortfolioProject>> getMostLikedProjects(@PathVariable Long portfolioId) {
        List<PortfolioProject> projects = projectService.getMostLikedProjects(portfolioId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/portfolio/{portfolioId}/recent")
    public ResponseEntity<List<PortfolioProject>> getRecentProjects(@PathVariable Long portfolioId) {
        List<PortfolioProject> projects = projectService.getRecentProjects(portfolioId);
        return ResponseEntity.ok(projects);
    }
}