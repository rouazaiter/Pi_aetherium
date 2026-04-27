package com.education.platform.controllers.portfolio;

import com.education.platform.dto.portfolio.CreateSkillRequest;
import com.education.platform.dto.portfolio.SkillSummaryDto;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.services.interfaces.portfolio.SkillCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio/skills")
public class SkillController {

    private final SkillCatalogService skillCatalogService;

    public SkillController(SkillCatalogService skillCatalogService) {
        this.skillCatalogService = skillCatalogService;
    }

    @GetMapping
    public List<SkillSummaryDto> listSkills(@RequestParam(name = "q", required = false) String query) {
        return query == null || query.trim().isEmpty()
                ? skillCatalogService.listAll()
                : skillCatalogService.search(query);
    }

    @GetMapping("/categories")
    public List<SkillCategory> listCategories() {
        return skillCatalogService.listCategories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public SkillSummaryDto createSkill(@Valid @RequestBody CreateSkillRequest request) {
        return skillCatalogService.findOrCreate(request);
    }
}
