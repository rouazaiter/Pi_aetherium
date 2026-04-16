package jihen.portfolio.controllers;

import jihen.portfolio.dto.PortfolioDNADto;
import jihen.portfolio.dtos.*;
import jihen.portfolio.entities.Portfolio;
import jihen.portfolio.entities.PortfolioProject;
import jihen.portfolio.entities.Skill;
import jihen.portfolio.enums.PortfolioVisibility;
import jihen.portfolio.repositories.PortfolioProjectRepository;
import jihen.portfolio.services.PortfolioService;
import jihen.portfolio.services.PortfolioThemeService;
import jihen.portfolio.services.SkillGapService;
import jihen.portfolio.services.VisitorAnalyzerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private VisitorAnalyzerService visitorAnalyzerService;

    @Autowired
    private PortfolioProjectRepository portfolioProjectRepository;

    // ========== DNA ==========
    @Autowired
    private PortfolioThemeService portfolioThemeService;
    @Autowired
    private SkillGapService skillGapService;

    @GetMapping("/{id}/skill-gap")
    public ResponseEntity<SkillGapDto> getSkillGap(@PathVariable Long id) {
        SkillGapDto gap = skillGapService.analyzeGap(id);
        return ResponseEntity.ok(gap);
    }
    @GetMapping("/{id}/themes")
    public ResponseEntity<List<ThemeResponseDto>> getAllThemes(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioThemeService.getAllThemes(id));
    }

    @GetMapping("/{id}/theme/active")
    public ResponseEntity<ThemeResponseDto> getActiveTheme(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioThemeService.getActiveTheme(id));
    }

    @PutMapping("/{id}/theme/active")
    public ResponseEntity<Void> setActiveTheme(@PathVariable Long id, @RequestParam Long themeId) {
        portfolioThemeService.setActiveTheme(id, themeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/themes/custom")
    public ResponseEntity<ThemeResponseDto> createCustomTheme(@PathVariable Long id, @RequestBody ThemeRequestDto request) {
        return ResponseEntity.ok(portfolioThemeService.createCustomTheme(id, request));
    }

    @PutMapping("/{id}/themes/custom/{themeId}")
    public ResponseEntity<ThemeResponseDto> updateCustomTheme(@PathVariable Long id, @PathVariable Long themeId, @RequestBody ThemeRequestDto request) {
        return ResponseEntity.ok(portfolioThemeService.updateCustomTheme(id, themeId, request));
    }

    @DeleteMapping("/{id}/themes/custom/{themeId}")
    public ResponseEntity<Void> deleteCustomTheme(@PathVariable Long id, @PathVariable Long themeId) {
        portfolioThemeService.deleteCustomTheme(id, themeId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}/dna")
    public ResponseEntity<PortfolioDNADto> getPortfolioDNA(@PathVariable Long id) {
        PortfolioDNADto dna = portfolioService.getPortfolioDNA(id);
        return ResponseEntity.ok(dna);
    }

    // ========== CRUD ==========

    @PostMapping("/user/{userId}")
    public ResponseEntity<Portfolio> createPortfolio(@PathVariable Long userId, @RequestBody Portfolio portfolio) {
        Portfolio created = portfolioService.createPortfolio(userId, portfolio);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Portfolio>> getAllPortfolios() {
        List<Portfolio> portfolios = portfolioService.getAllPortfolios();
        return ResponseEntity.ok(portfolios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> getPortfolioById(@PathVariable Long id) {
        Optional<Portfolio> portfolio = portfolioService.getPortfolioById(id);
        return portfolio.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @PutMapping("/{id}")
    public ResponseEntity<Portfolio> updatePortfolio(
            @PathVariable Long id,
            @RequestBody Portfolio portfolio,
            @RequestHeader Long userId) {  // ← ID de l'utilisateur connecté

        Portfolio existing = portfolioService.getPortfolioById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

        // ✅ Vérifier que l'utilisateur est le propriétaire
        if (!existing.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Portfolio updated = portfolioService.updatePortfolio(id, portfolio);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }

    // ========== GESTION DES SKILLS ==========

    @PostMapping("/{portfolioId}/skills/{skillId}")
    public ResponseEntity<Portfolio> addSkillToPortfolio(@PathVariable Long portfolioId, @PathVariable Long skillId) {
        Portfolio portfolio = portfolioService.addSkillToPortfolio(portfolioId, skillId);
        return ResponseEntity.ok(portfolio);
    }

    @DeleteMapping("/{portfolioId}/skills/{skillId}")
    public ResponseEntity<Portfolio> removeSkillFromPortfolio(@PathVariable Long portfolioId, @PathVariable Long skillId) {
        Portfolio portfolio = portfolioService.removeSkillFromPortfolio(portfolioId, skillId);
        return ResponseEntity.ok(portfolio);
    }

    @GetMapping("/{portfolioId}/skills")
    public ResponseEntity<Set<Skill>> getSkillsByPortfolio(@PathVariable Long portfolioId) {
        Set<Skill> skills = portfolioService.getSkillsByPortfolioId(portfolioId);
        return ResponseEntity.ok(skills);
    }

    // ========== GESTION DE LA VISIBILITÉ ==========

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Portfolio> changeVisibility(@PathVariable Long id, @RequestParam PortfolioVisibility visibility) {
        Portfolio portfolio = portfolioService.changeVisibility(id, visibility);
        return ResponseEntity.ok(portfolio);
    }

    @GetMapping("/public/all")
    public ResponseEntity<List<Portfolio>> getPublicPortfolios() {
        List<Portfolio> portfolios = portfolioService.getPublicPortfolios();
        return ResponseEntity.ok(portfolios);
    }

    // ========== STATISTIQUES ==========

    @PostMapping("/{id}/increment-views")
    public ResponseEntity<Void> incrementViews(@PathVariable Long id) {
        portfolioService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/total-views")
    public ResponseEntity<Long> getTotalViews() {
        Long totalViews = portfolioService.getTotalViews();
        return ResponseEntity.ok(totalViews);
    }

    // ========== RECHERCHE ==========

    @GetMapping("/search")
    public ResponseEntity<List<Portfolio>> searchPortfolios(@RequestParam String keyword) {
        List<Portfolio> portfolios = portfolioService.searchPortfolios(keyword);
        return ResponseEntity.ok(portfolios);
    }

    @GetMapping("/search/by-location")
    public ResponseEntity<List<Portfolio>> searchByLocation(@RequestParam String location) {
        List<Portfolio> portfolios = portfolioService.searchByLocation(location);
        return ResponseEntity.ok(portfolios);
    }

    // ========== PORTFOLIO ADAPTATIF (IA-driven) ==========

    @GetMapping("/{id}/adaptive")
    public ResponseEntity<PortfolioAdaptiveDto> getAdaptivePortfolio(
            @PathVariable Long id,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "Referer", required = false) String referer,
            @RequestParam(value = "visitor", required = false) String visitorParam) {

        // 1. Analyser le visiteur
        VisitorProfile visitor = visitorAnalyzerService.analyzeVisitor(userAgent, referer, visitorParam);

        // 2. Récupérer le portfolio
        Portfolio portfolio = portfolioService.getPortfolioById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

        // 3. Convertir Portfolio en PortfolioSimpleDTO (sans projets)
        PortfolioSimpleDto portfolioDTO = convertToSimpleDTO(portfolio);

        // 4. Récupérer les projets
        List<PortfolioProject> projects = portfolioProjectRepository.findByPortfolioId(id);
        if (projects == null) projects = new ArrayList<>();

        // 5. Adapter l'ordre
        List<PortfolioProject> adaptedProjects = adaptProjects(projects, visitor);

        // 6. Convertir en DTO (sans référence circulaire)
        List<PortfolioProjectDto> projectDTOs = adaptedProjects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 7. Construire la réponse
        PortfolioAdaptiveDto response = new PortfolioAdaptiveDto(portfolioDTO, projectDTOs,
                visitor.getType(), visitor.getPriority());

        return ResponseEntity.ok(response);
    }

    private PortfolioSimpleDto convertToSimpleDTO(Portfolio portfolio) {
        return new PortfolioSimpleDto(
                portfolio.getId(),
                portfolio.getUser().getId(),
                portfolio.getTitle(),
                portfolio.getBio(),
                portfolio.getProfilePicture(),
                portfolio.getCoverImage(),
                portfolio.getLocation(),
                portfolio.getVisibility(),
                portfolio.getTotalViews(),
                portfolio.getIsVerified(),
                portfolio.getFollowerCount(),
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }

    // ========== MÉTHODE PRIVÉE D'ADAPTATION ==========
    private PortfolioProjectDto convertToDTO(PortfolioProject project) {
        return new PortfolioProjectDto(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getProjectUrl(),
                project.getPinned(),
                project.getViews(),
                project.getLikes(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
    private List<PortfolioProject> adaptProjects(List<PortfolioProject> projects, VisitorProfile visitor) {
        if (projects == null || projects.isEmpty()) {
            return new ArrayList<>();
        }

        String priority = visitor.getPriority();
        List<PortfolioProject> mutableProjects = new ArrayList<>(projects);

        switch (priority) {
            case "EXPERIENCE":
                // Trier par likes + vues (les plus populaires d'abord)
                mutableProjects.sort((p1, p2) -> {
                    int score1 = (p1.getLikes() != null ? p1.getLikes() : 0) +
                            (p1.getViews() != null ? p1.getViews() : 0);
                    int score2 = (p2.getLikes() != null ? p2.getLikes() : 0) +
                            (p2.getViews() != null ? p2.getViews() : 0);
                    return Integer.compare(score2, score1);
                });
                break;

            case "SKILLS":
                // Trier par projets avec URL (démonstration technique)
                mutableProjects.sort((p1, p2) -> {
                    boolean p1HasUrl = p1.getProjectUrl() != null && !p1.getProjectUrl().isEmpty();
                    boolean p2HasUrl = p2.getProjectUrl() != null && !p2.getProjectUrl().isEmpty();
                    return Boolean.compare(p2HasUrl, p1HasUrl);
                });
                break;

            case "AVAILABILITY":
                // Mettre les projets épinglés en premier
                mutableProjects.sort((p1, p2) -> {
                    boolean p1Pinned = p1.getPinned() != null && p1.getPinned();
                    boolean p2Pinned = p2.getPinned() != null && p2.getPinned();
                    return Boolean.compare(p2Pinned, p1Pinned);
                });
                break;

            default: // PROJECTS
                // Ordre normal par date de création (plus récent d'abord)
                mutableProjects.sort((p1, p2) -> {
                    if (p1.getCreatedAt() == null) return 1;
                    if (p2.getCreatedAt() == null) return -1;
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                });
                break;
        }

        // Limiter à 6 projets maximum
        return mutableProjects.stream().limit(6).collect(Collectors.toList());
    }
    @GetMapping("/simple")
    public ResponseEntity<List<PortfolioSimpleDto>> getAllPortfoliosSimple() {
        List<Portfolio> portfolios = portfolioService.getAllPortfolios();

        List<PortfolioSimpleDto> dtos = portfolios.stream()
                .map(this::convertToSimpleDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
    @GetMapping("/{id}/simple")
    public ResponseEntity<PortfolioSimpleDto> getPortfolioSimple(@PathVariable Long id) {
        Portfolio portfolio = portfolioService.getPortfolioById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

        PortfolioSimpleDto dto = convertToSimpleDto(portfolio);
        return ResponseEntity.ok(dto);
    }

    private PortfolioSimpleDto convertToSimpleDto(Portfolio portfolio) {
        return new PortfolioSimpleDto(
                portfolio.getId(),
                portfolio.getUser().getId(),
                portfolio.getTitle(),
                portfolio.getBio(),
                portfolio.getProfilePicture(),
                portfolio.getCoverImage(),
                portfolio.getLocation(),
                portfolio.getVisibility(),
                portfolio.getTotalViews(),
                portfolio.getIsVerified(),
                portfolio.getFollowerCount(),
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }
    @PostMapping("/{id}/upload-images")
    public ResponseEntity<PortfolioSimpleDto> uploadImages(
            @PathVariable Long id,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {

        Portfolio portfolio = portfolioService.getPortfolioById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String profileUrl = saveImage(profilePicture, "profile");
            portfolio.setProfilePicture(profileUrl);
        }

        if (coverImage != null && !coverImage.isEmpty()) {
            String coverUrl = saveImage(coverImage, "cover");
            portfolio.setCoverImage(coverUrl);
        }

        portfolioService.updatePortfolio(id, portfolio);
        return ResponseEntity.ok(convertToSimpleDto(portfolio));
    }

    private String saveImage(MultipartFile file, String type) {
        try {
            // 1. Vérifier que le fichier n'est pas vide
            if (file == null || file.isEmpty()) {
                return null;
            }

            // 2. Créer le dossier uploads (avec chemin absolu)
            String uploadDir = System.getProperty("user.dir") + "/uploads/" + type + "/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new RuntimeException("Impossible de créer le dossier: " + uploadDir);
                }
            }

            // 3. Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = System.currentTimeMillis() + "_" + type + extension;

            // 4. Sauvegarder le fichier
            File destFile = new File(directory, fileName);
            file.transferTo(destFile);

            // 5. Retourner l'URL relative (pour accès depuis le frontend)
            return "/uploads/" + type + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de l'image: " + e.getMessage(), e);
        }
    }
}