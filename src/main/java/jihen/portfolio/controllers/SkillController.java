package jihen.portfolio.controllers;

import jihen.portfolio.entities.Portfolio;
import jihen.portfolio.entities.Skill;
import jihen.portfolio.enums.SkillCategory;
import jihen.portfolio.services.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @PostMapping
    public ResponseEntity<Skill> createSkill(@RequestBody Skill skill) {
        Skill created = skillService.createSkill(skill);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        List<Skill> skills = skillService.getAllSkills();
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Skill> getSkillById(@PathVariable Long id) {
        Optional<Skill> skill = skillService.getSkillById(id);
        return skill.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Skill> getSkillByName(@PathVariable String name) {
        Optional<Skill> skill = skillService.getSkillByName(name);
        return skill.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        Skill updated = skillService.updateSkill(id, skill);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/category/{category}")
    public ResponseEntity<List<Skill>> getSkillsByCategory(@PathVariable SkillCategory category) {
        List<Skill> skills = skillService.getSkillsByCategory(category);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/trendy")
    public ResponseEntity<List<Skill>> getTrendySkills() {
        List<Skill> skills = skillService.getTrendySkills();
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/most-searched")
    public ResponseEntity<List<Skill>> getMostSearchedSkills() {
        List<Skill> skills = skillService.getMostSearchedSkills();
        return ResponseEntity.ok(skills);
    }


    @PostMapping("/{id}/increment-search")
    public ResponseEntity<Void> incrementSearchCount(@PathVariable Long id) {
        skillService.incrementSearchCount(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/mark-trendy")
    public ResponseEntity<Void> markAsTrendy(@PathVariable Long id) {
        skillService.markAsTrendy(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-trendy")
    public ResponseEntity<Void> resetAllTrendySkills() {
        skillService.resetAllTrendySkills();
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{skillId}/portfolios")
    public ResponseEntity<List<Portfolio>> getPortfoliosBySkill(@PathVariable Long skillId) {
        List<Portfolio> portfolios = skillService.getPortfoliosBySkillId(skillId);
        return ResponseEntity.ok(portfolios);
    }

}

