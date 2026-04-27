package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.entities.portfolio.SkillCategory;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public final class SkillFamilyWeightMapper {

    private SkillFamilyWeightMapper() {
    }

    public static Map<DeveloperFamily, Double> mapWeights(SkillCategory category) {
        Map<DeveloperFamily, Double> weights = new EnumMap<>(DeveloperFamily.class);
        if (category == null) {
            weights.put(DeveloperFamily.GENERAL, 1.0);
            return weights;
        }

        switch (category) {
            case BACKEND_DEVELOPMENT -> {
                weights.put(DeveloperFamily.BACKEND, 1.0);
                weights.put(DeveloperFamily.FULL_STACK, 0.3);
            }
            case SQL_DATABASES -> {
                weights.put(DeveloperFamily.BACKEND, 1.0);
                weights.put(DeveloperFamily.DATA_AI, 0.3);
                weights.put(DeveloperFamily.FULL_STACK, 0.2);
            }
            case NOSQL_DATABASES -> {
                weights.put(DeveloperFamily.BACKEND, 0.9);
                weights.put(DeveloperFamily.DATA_AI, 0.4);
                weights.put(DeveloperFamily.FULL_STACK, 0.2);
            }
            case DATABASE_ADMINISTRATION -> {
                weights.put(DeveloperFamily.BACKEND, 0.8);
                weights.put(DeveloperFamily.DATA_AI, 0.3);
                weights.put(DeveloperFamily.DEVOPS_CLOUD, 0.2);
            }
            case FRONTEND_DEVELOPMENT, MOBILE_DEVELOPMENT, DESKTOP_DEVELOPMENT -> {
                weights.put(DeveloperFamily.FRONTEND, 1.0);
                weights.put(DeveloperFamily.FULL_STACK, 0.35);
            }
            case UI_UX_DESIGN -> {
                weights.put(DeveloperFamily.FRONTEND, 0.8);
                weights.put(DeveloperFamily.DESIGN_CREATIVE, 0.7);
            }
            case FULL_STACK_DEVELOPMENT -> {
                weights.put(DeveloperFamily.FULL_STACK, 1.0);
                weights.put(DeveloperFamily.BACKEND, 0.4);
                weights.put(DeveloperFamily.FRONTEND, 0.4);
            }
            case DEVOPS, CLOUD_COMPUTING, CONTAINERIZATION, CI_CD, INFRASTRUCTURE_AS_CODE, MONITORING, EMBEDDED_SYSTEMS, ROBOTICS -> {
                weights.put(DeveloperFamily.DEVOPS_CLOUD, 1.0);
                weights.put(DeveloperFamily.BACKEND, 0.25);
                weights.put(DeveloperFamily.FULL_STACK, 0.15);
            }
            case DEVSECOPS -> {
                weights.put(DeveloperFamily.DEVOPS_CLOUD, 0.8);
                weights.put(DeveloperFamily.SECURITY, 0.8);
                weights.put(DeveloperFamily.BACKEND, 0.2);
            }
            case MACHINE_LEARNING, DEEP_LEARNING, DATA_ANALYSIS, DATA_ENGINEERING,
                 BUSINESS_INTELLIGENCE, NATURAL_LANGUAGE_PROCESSING, COMPUTER_VISION,
                 LLM_ENGINEERING, QUANTUM_COMPUTING -> weights.put(DeveloperFamily.DATA_AI, 1.0);
            case NETWORK_SECURITY, SECURITY_OPERATIONS -> {
                weights.put(DeveloperFamily.SECURITY, 1.0);
                weights.put(DeveloperFamily.DEVOPS_CLOUD, 0.2);
            }
            case APPLICATION_SECURITY, IDENTITY_ACCESS_MANAGEMENT -> {
                weights.put(DeveloperFamily.SECURITY, 1.0);
                weights.put(DeveloperFamily.BACKEND, 0.4);
                weights.put(DeveloperFamily.FULL_STACK, 0.2);
            }
            case GRAPHIC_DESIGN, VIDEO_EDITING, MOTION_GRAPHICS, THREE_D_MODELING,
                 ILLUSTRATION, PHOTOGRAPHY, AUDIO_PRODUCTION, MUSIC_COMPOSITION,
                 VOICE_OVER, PODCAST_PRODUCTION, COPYWRITING, CONTENT_WRITING,
                 TRANSLATION, PROOFREADING, SCRIPT_WRITING -> weights.put(DeveloperFamily.DESIGN_CREATIVE, 1.0);
            case PRODUCT_MANAGEMENT, BUSINESS_ANALYSIS, AGILE_PROJECT_MANAGEMENT, E_COMMERCE, CUSTOMER_SUPPORT, VIRTUAL_ASSISTANCE, TUTORING, CAREER_COACHING, CONSULTING -> {
                weights.put(DeveloperFamily.GENERAL, 0.7);
                weights.put(DeveloperFamily.FRONTEND, 0.1);
                weights.put(DeveloperFamily.FULL_STACK, 0.1);
            }
            case DIGITAL_MARKETING, SOCIAL_MEDIA_MARKETING, EMAIL_MARKETING, CONTENT_STRATEGY, SEO_STRATEGY, AFFILIATE_MARKETING -> {
                weights.put(DeveloperFamily.DESIGN_CREATIVE, 0.6);
                weights.put(DeveloperFamily.GENERAL, 0.4);
            }
            case BLOCKCHAIN, CRYPTOCURRENCY, NFT_DEVELOPMENT -> {
                weights.put(DeveloperFamily.BACKEND, 0.5);
                weights.put(DeveloperFamily.FULL_STACK, 0.4);
                weights.put(DeveloperFamily.SECURITY, 0.2);
            }
            case GAME_DEVELOPMENT, AUGMENTED_REALITY, VIRTUAL_REALITY -> {
                weights.put(DeveloperFamily.FRONTEND, 0.4);
                weights.put(DeveloperFamily.DESIGN_CREATIVE, 0.4);
                weights.put(DeveloperFamily.GENERAL, 0.3);
            }
            default -> weights.put(DeveloperFamily.GENERAL, 0.5);
        }

        return weights;
    }

    public static DeveloperFamily dominantFamily(Collection<SkillCategory> categories) {
        Map<DeveloperFamily, Double> totals = new EnumMap<>(DeveloperFamily.class);
        for (DeveloperFamily family : DeveloperFamily.values()) {
            totals.put(family, 0.0);
        }

        if (categories != null) {
            for (SkillCategory category : categories) {
                for (Map.Entry<DeveloperFamily, Double> entry : mapWeights(category).entrySet()) {
                    totals.merge(entry.getKey(), entry.getValue(), Double::sum);
                }
            }
        }

        return totals.entrySet().stream()
                .filter(entry -> entry.getKey() != DeveloperFamily.GENERAL)
                .max(Map.Entry.comparingByValue())
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse(DeveloperFamily.GENERAL);
    }
}
