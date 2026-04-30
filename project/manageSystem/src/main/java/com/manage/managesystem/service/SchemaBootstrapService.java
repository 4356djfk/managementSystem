package com.manage.managesystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaBootstrapService implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SchemaBootstrapService.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaBootstrapService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureTaskSchema();
        ensureProjectEditorPreferenceSchema();
    }

    private void ensureTaskSchema() {
        if (!tableExists("project_task")) {
            log.warn("Skip schema bootstrap because table project_task does not exist");
        } else {
            ensureColumn(
                    "project_task",
                    "deadline_date",
                    "alter table project_task add column deadline_date datetime null after planned_end_date"
            );
            ensureColumn(
                    "project_task",
                    "constraint_type",
                    "alter table project_task add column constraint_type varchar(16) null after deadline_date"
            );
            ensureColumn(
                    "project_task",
                    "constraint_date",
                    "alter table project_task add column constraint_date datetime null after constraint_type"
            );
        }

        if (!tableExists("task_dependency")) {
            log.warn("Skip schema bootstrap because table task_dependency does not exist");
        } else {
            ensureColumn(
                    "task_dependency",
                    "lag_days",
                    "alter table task_dependency add column lag_days int not null default 0 after dependency_type"
            );
        }
    }

    private void ensureProjectEditorPreferenceSchema() {
        if (!tableExists("project_editor_preference")) {
            log.warn("Skip schema bootstrap because table project_editor_preference does not exist");
            return;
        }

        ensureColumn(
                "project_editor_preference",
                "schedule_options_json",
                "alter table project_editor_preference add column schedule_options_json longtext null after wbs_config_json"
        );
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(1)
                from information_schema.tables
                where table_schema = database()
                  and table_name = ?
                """,
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private void ensureColumn(String tableName, String columnName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(1)
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                """,
                Integer.class,
                tableName,
                columnName
        );

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.execute(alterSql);
        log.info("Added missing column {}.{} during schema bootstrap", tableName, columnName);
    }
}
