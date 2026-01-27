package com.javarush.taskmanager.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@Slf4j
public class DatabaseCleaner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void cleanTables() {
        try {
            boolean usersExists = tableExists("users");
            boolean tasksExists = tableExists("tasks");
            boolean refreshTokensExists = tableExists("refresh_tokens");

            if (tasksExists) {
                jdbcTemplate.execute("DELETE FROM tasks");
            }

            if (refreshTokensExists) {
                jdbcTemplate.execute("DELETE FROM refresh_tokens");
            }

            if (usersExists) {
                jdbcTemplate.execute("DELETE FROM users");
            }

            resetSequences();
            log.info("Database cleaned successfully");

        } catch (Exception e) {
            System.err.println("Error cleaning database: " + e.getMessage());
        }
    }

    private boolean tableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                            "WHERE table_schema = 'public' AND table_name = ?",
                    Integer.class, tableName.toLowerCase());
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void resetSequences() {
        try {
            resetSequenceIfExists("users_id_seq");
            resetSequenceIfExists("tasks_id_seq");
            resetSequenceIfExists("refresh_tokens_id_seq");
        } catch (Exception e) {
        }
    }

    private void resetSequenceIfExists(String sequenceName) {
        try {
            Integer exists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM pg_sequences WHERE schemaname = 'public' AND sequencename = ?",
                    Integer.class, sequenceName);

            if (exists != null && exists > 0) {
                jdbcTemplate.execute("ALTER SEQUENCE " + sequenceName + " RESTART WITH 1");
            }
        } catch (Exception e) {
        }
    }
}
