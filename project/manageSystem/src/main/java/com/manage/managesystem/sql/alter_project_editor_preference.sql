CREATE TABLE IF NOT EXISTS project_editor_preference (
    id bigint NOT NULL PRIMARY KEY,
    project_id bigint NOT NULL,
    gantt_appearance_json longtext NULL,
    wbs_config_json longtext NULL,
    created_by bigint NULL,
    created_at datetime NOT NULL,
    updated_by bigint NULL,
    updated_at datetime NOT NULL,
    is_deleted tinyint(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_project_editor_preference_project (project_id),
    KEY idx_project_editor_preference_updated (updated_at),
    CONSTRAINT fk_project_editor_preference_project FOREIGN KEY (project_id) REFERENCES project_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
