package com.education.platform.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class StudyGroupSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public StudyGroupSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE `study_group` MODIFY COLUMN `image_url` LONGTEXT NULL");
        } catch (Exception ignored) {
            // Best-effort migration: ignore when table/column is not ready yet.
        }
    }
}
