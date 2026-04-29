package com.education.platform.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public ChatMessageSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!tableExists("chat_messages")) {
                return;
            }

            boolean hasRoomId = columnExists("chat_messages", "room_id");
            boolean hasSessionId = columnExists("chat_messages", "session_id");

            if (!hasRoomId) {
                jdbcTemplate.execute("ALTER TABLE `chat_messages` ADD COLUMN `room_id` BIGINT NULL");
            }
            if (!hasSessionId) {
                jdbcTemplate.execute("ALTER TABLE `chat_messages` ADD COLUMN `session_id` BIGINT NULL");
            }

            jdbcTemplate.execute("""
                    UPDATE `chat_messages`
                    SET `session_id` = `room_id`
                    WHERE `session_id` IS NULL AND `room_id` IS NOT NULL
                    """);
            jdbcTemplate.execute("""
                    UPDATE `chat_messages`
                    SET `room_id` = `session_id`
                    WHERE `room_id` IS NULL AND `session_id` IS NOT NULL
                    """);
        } catch (Exception ignored) {
            // Best-effort chat migration only.
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """,
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }
}
