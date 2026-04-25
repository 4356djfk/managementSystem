ALTER TABLE risk_register
    ADD COLUMN task_id BIGINT NULL AFTER response_strategy,
    ADD COLUMN phase_name VARCHAR(255) NULL AFTER task_id,
    ADD KEY idx_risk_task (task_id),
    ADD CONSTRAINT fk_risk_task FOREIGN KEY (task_id) REFERENCES project_task(id);
