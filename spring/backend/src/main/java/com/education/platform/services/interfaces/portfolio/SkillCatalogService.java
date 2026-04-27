package com.education.platform.services.interfaces.portfolio;

import com.education.platform.dto.portfolio.SkillSummaryDto;
import com.education.platform.dto.portfolio.CreateSkillRequest;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.SkillCategory;

import java.util.List;
import java.util.Set;

public interface SkillCatalogService {

    List<SkillSummaryDto> listAll();

    List<SkillSummaryDto> search(String query);

    List<SkillCategory> listCategories();

    SkillSummaryDto findOrCreate(CreateSkillRequest request);

    Set<Skill> requireSkillsByIds(List<Long> skillIds);
}
