package com.education.platform.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CVSectionSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public CVSectionSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE `cv_section` MODIFY COLUMN `content_json` LONGTEXT NOT NULL");
        } catch (Exception ignored) {
            // Best-effort migration: ignore when table/column is not ready yet.
        }
    }
}
