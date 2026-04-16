package jihen.portfolio.services;

import jihen.portfolio.dtos.ThemeRequestDto;
import jihen.portfolio.dtos.ThemeResponseDto;
import jihen.portfolio.entities.PortfolioTheme;
import jihen.portfolio.repositories.PortfolioThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioThemeService {

    private final PortfolioThemeRepository themeRepository;

    // Thèmes prédéfinis
    public List<ThemeResponseDto> getPredefinedThemes() {
        List<ThemeResponseDto> themes = new ArrayList<>();

        themes.add(ThemeResponseDto.builder()
                .id(100L).name("Terminal Overdrive").type("PREDEFINED")
                .primaryColor("#0a0a0a").secondaryColor("#00ffcc").accentColor("#1a2a4f")
                .fontFamily("Fira Code").backgroundStyle("dark").isActive(false).build());

        themes.add(ThemeResponseDto.builder()
                .id(101L).name("Canvas Cloud").type("PREDEFINED")
                .primaryColor("#ffffff").secondaryColor("#ff3366").accentColor("#6c63ff")
                .fontFamily("Poppins").backgroundStyle("light").isActive(false).build());

        themes.add(ThemeResponseDto.builder()
                .id(102L).name("Neural Prism").type("PREDEFINED")
                .primaryColor("#4f46e5").secondaryColor("#ffffff").accentColor("#00ffcc")
                .fontFamily("Inter").backgroundStyle("gradient").isActive(false).build());

        themes.add(ThemeResponseDto.builder()
                .id(103L).name("Impact Spark").type("PREDEFINED")
                .primaryColor("#0A0A0A").secondaryColor("#FF5C00").accentColor("#ffffff")
                .fontFamily("Space Grotesk").backgroundStyle("brutalist").isActive(false).build());

        themes.add(ThemeResponseDto.builder()
                .id(104L).name("Iron Infrastructure").type("PREDEFINED")
                .primaryColor("#E2E8F0").secondaryColor("#059669").accentColor("#0f172a")
                .fontFamily("Inter").backgroundStyle("industrial").isActive(false).build());

        themes.add(ThemeResponseDto.builder()
                .id(105L).name("Executive Slate").type("PREDEFINED")
                .primaryColor("#121826").secondaryColor("#ffffff").accentColor("#6c63ff")
                .fontFamily("Manrope").backgroundStyle("luxury").isActive(false).build());

        return themes;
    }

    // Récupérer tous les thèmes (prédéfinis + personnalisés)
    public List<ThemeResponseDto> getAllThemes(Long portfolioId) {
        List<ThemeResponseDto> allThemes = getPredefinedThemes();

        List<ThemeResponseDto> customThemes = themeRepository.findByPortfolioId(portfolioId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        allThemes.addAll(customThemes);
        return allThemes;
    }

    // Récupérer le thème actif
    public ThemeResponseDto getActiveTheme(Long portfolioId) {
        return themeRepository.findByPortfolioIdAndIsActiveTrue(portfolioId)
                .map(this::toDto)
                .orElse(getPredefinedThemes().get(0));
    }

    // Changer le thème actif (sans @Query)
    @Transactional
    public void setActiveTheme(Long portfolioId, Long themeId) {
        // Désactiver tous les thèmes du portfolio
        List<PortfolioTheme> allThemes = themeRepository.findByPortfolioId(portfolioId);
        for (PortfolioTheme theme : allThemes) {
            theme.setIsActive(false);
        }
        themeRepository.saveAll(allThemes);

        // Activer le thème choisi
        if (themeId != null && themeId > 0) {
            // Vérifier d'abord si c'est un thème prédéfini ou personnalisé
            // Pour les thèmes prédéfinis, on ne les stocke pas en base, donc on ne fait rien
            // Pour les thèmes personnalisés, on active
            themeRepository.findById(themeId).ifPresent(theme -> {
                theme.setIsActive(true);
                themeRepository.save(theme);
            });
        }
    }

    // Créer un thème personnalisé
    @Transactional
    public ThemeResponseDto createCustomTheme(Long portfolioId, ThemeRequestDto request) {
        PortfolioTheme theme = PortfolioTheme.builder()
                .portfolioId(portfolioId)
                .name(request.getName())
                .type("CUSTOM")
                .primaryColor(request.getPrimaryColor())
                .secondaryColor(request.getSecondaryColor())
                .accentColor(request.getAccentColor())
                .fontFamily(request.getFontFamily())
                .backgroundStyle(request.getBackgroundStyle())
                .backgroundImage(request.getBackgroundImage())
                .isActive(false)
                .build();

        return toDto(themeRepository.save(theme));
    }

    // Modifier un thème personnalisé
    @Transactional
    public ThemeResponseDto updateCustomTheme(Long portfolioId, Long themeId, ThemeRequestDto request) {
        PortfolioTheme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme non trouvé"));

        if (!theme.getPortfolioId().equals(portfolioId)) {
            throw new RuntimeException("Theme non autorisé");
        }

        theme.setName(request.getName());
        theme.setPrimaryColor(request.getPrimaryColor());
        theme.setSecondaryColor(request.getSecondaryColor());
        theme.setAccentColor(request.getAccentColor());
        theme.setFontFamily(request.getFontFamily());
        theme.setBackgroundStyle(request.getBackgroundStyle());
        theme.setBackgroundImage(request.getBackgroundImage());

        return toDto(themeRepository.save(theme));
    }

    // Supprimer un thème personnalisé
    @Transactional
    public void deleteCustomTheme(Long portfolioId, Long themeId) {
        PortfolioTheme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme non trouvé"));

        if (!theme.getPortfolioId().equals(portfolioId)) {
            throw new RuntimeException("Theme non autorisé");
        }

        if (theme.getIsActive()) {
            throw new RuntimeException("Impossible de supprimer le thème actif");
        }

        themeRepository.delete(theme);
    }

    // Convertir entité en DTO
    private ThemeResponseDto toDto(PortfolioTheme theme) {
        return ThemeResponseDto.builder()
                .id(theme.getId())
                .name(theme.getName())
                .type(theme.getType())
                .primaryColor(theme.getPrimaryColor())
                .secondaryColor(theme.getSecondaryColor())
                .accentColor(theme.getAccentColor())
                .fontFamily(theme.getFontFamily())
                .backgroundStyle(theme.getBackgroundStyle())
                .backgroundImage(theme.getBackgroundImage())
                .isActive(theme.getIsActive())
                .build();
    }
}