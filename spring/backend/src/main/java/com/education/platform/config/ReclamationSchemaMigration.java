package com.education.platform.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReclamationSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public ReclamationSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // MariaDB-safe creation to avoid Hibernate DDL incompatibilities on LONGTEXT/enum.
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `reclamation` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `admin_response` LONGTEXT NULL,
                    `created_at` DATETIME(6) NOT NULL,
                    `description` LONGTEXT NOT NULL,
                    `reviewed_at` DATETIME(6) NULL,
                    `status` VARCHAR(32) NOT NULL,
                    `subject` VARCHAR(255) NOT NULL,
                    `updated_at` DATETIME(6) NOT NULL,
                    `reviewed_by_id` BIGINT NULL,
                    `user_id` BIGINT NOT NULL,
                    PRIMARY KEY (`id`),
                    INDEX `idx_reclamation_user` (`user_id`),
                    INDEX `idx_reclamation_reviewed_by` (`reviewed_by_id`)
                ) ENGINE=InnoDB
                """);

        // Add FKs best-effort; ignore if already present/not possible yet.
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE `reclamation`
                    ADD CONSTRAINT `fk_reclamation_user`
                    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
                    """);
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE `reclamation`
                    ADD CONSTRAINT `fk_reclamation_reviewed_by`
                    FOREIGN KEY (`reviewed_by_id`) REFERENCES `user`(`id`)
                    """);
        } catch (Exception ignored) {
        }
    }
}
