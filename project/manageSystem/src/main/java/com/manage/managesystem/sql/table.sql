/* 软件项目管理工具数据库初始化脚本 */
/* 1. 基础配置 */
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0; -- 临时禁用外键检查，避免建表顺序问题
DROP DATABASE IF EXISTS project_management;
CREATE DATABASE IF NOT EXISTS project_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE project_management;

/* 2. 系统基础表（无外键依赖） */
-- 2.1 角色表 sys_role
CREATE TABLE sys_role (
                          id bigint NOT NULL PRIMARY KEY,
                          role_code varchar(64) NOT NULL,
                          role_name varchar(64) NOT NULL,
                          description varchar(255) NULL,
                          created_at datetime NOT NULL,
                          updated_at datetime NOT NULL,
                          UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.2 用户表 sys_user
CREATE TABLE sys_user (
                          id bigint NOT NULL PRIMARY KEY,
                          username varchar(64) NOT NULL,
                          password_hash varchar(255) NOT NULL,
                          real_name varchar(64) NOT NULL,
                          email varchar(128) NULL,
                          phone varchar(32) NULL,
                          avatar_url varchar(255) NULL,
                          status varchar(32) NOT NULL,
                          last_login_at datetime NULL,
                          created_by bigint NULL,
                          created_at datetime NOT NULL,
                          updated_by bigint NULL,
                          updated_at datetime NOT NULL,
                          is_deleted tinyint(1) NOT NULL DEFAULT 0,
                          UNIQUE KEY uk_sys_user_username (username),
                          KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.3 用户角色关联表 sys_user_role
CREATE TABLE sys_user_role (
                               id bigint NOT NULL PRIMARY KEY,
                               user_id bigint NOT NULL,
                               role_id bigint NOT NULL,
                               created_at datetime NOT NULL,
                               UNIQUE KEY uk_user_role (user_id, role_id),
                               KEY idx_sys_user_role_user (user_id),
                               KEY idx_sys_user_role_role (role_id),
                               CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
                               CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 3. 项目核心基础表 */
-- 3.1 项目模板表 project_template（被project_info依赖）
CREATE TABLE project_template (
                                  id bigint NOT NULL PRIMARY KEY,
                                  name varchar(128) NOT NULL,
                                  type varchar(32) NOT NULL,
                                  description varchar(255) NULL,
                                  is_system tinyint(1) DEFAULT 0,
                                  status varchar(32) NOT NULL,
                                  created_by bigint NULL,
                                  created_at datetime NOT NULL,
                                  updated_by bigint NULL,
                                  updated_at datetime NOT NULL,
                                  KEY idx_project_template_status (status),
                                  KEY idx_project_template_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2 项目主表 project_info
CREATE TABLE project_info (
                              id bigint NOT NULL PRIMARY KEY,
                              project_code varchar(64) NULL,
                              name varchar(128) NOT NULL,
                              description text NULL,
                              life_cycle_model varchar(32) NOT NULL,
                              status varchar(32) NOT NULL,
                              start_date date NULL,
                              end_date date NULL,
                              actual_start_date date NULL,
                              actual_end_date date NULL,
                              owner_id bigint NULL,
                              template_id bigint NULL,
                              progress_rate decimal(5,2) DEFAULT 0,
                              planned_budget decimal(14,2) DEFAULT 0,
                              actual_cost decimal(14,2) DEFAULT 0,
                              is_demo tinyint(1) DEFAULT 0,
                              created_by bigint NULL,
                              created_at datetime NOT NULL,
                              updated_by bigint NULL,
                              updated_at datetime NOT NULL,
                              is_deleted tinyint(1) NOT NULL DEFAULT 0,
                              UNIQUE KEY uk_project_info_code (project_code),
                              KEY idx_project_info_owner (owner_id),
                              KEY idx_project_info_status (status),
                              KEY idx_project_info_template (template_id),
                              CONSTRAINT fk_project_info_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id),
                              CONSTRAINT fk_project_info_template FOREIGN KEY (template_id) REFERENCES project_template(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.3 项目章程表 project_charter
CREATE TABLE project_charter (
                                 id bigint NOT NULL PRIMARY KEY,
                                 project_id bigint NOT NULL,
                                 objective text NOT NULL,
                                 scope_summary text NULL,
                                 sponsor varchar(128) NULL,
                                 approver varchar(128) NULL,
                                 stakeholders text NULL,
                                 success_criteria text NULL,
                                 created_by bigint NULL,
                                 created_at datetime NOT NULL,
                                 updated_by bigint NULL,
                                 updated_at datetime NOT NULL,
                                 UNIQUE KEY uk_project_charter_project (project_id),
                                 KEY idx_project_charter_project (project_id),
                                 CONSTRAINT fk_project_charter_project FOREIGN KEY (project_id) REFERENCES project_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.4 项目成员表 project_member
CREATE TABLE project_member (
                                id bigint NOT NULL PRIMARY KEY,
                                project_id bigint NOT NULL,
                                user_id bigint NOT NULL,
                                project_role varchar(32) NOT NULL,
                                member_status varchar(32) NOT NULL,
                                joined_at datetime NULL,
                                left_at datetime NULL,
                                created_by bigint NULL,
                                created_at datetime NOT NULL,
                                updated_by bigint NULL,
                                updated_at datetime NOT NULL,
                                UNIQUE KEY uk_project_member (project_id, user_id),
                                KEY idx_project_member_user (user_id),
                                KEY idx_project_member_role (project_role),
                                KEY idx_project_member_status (member_status),
                                CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.5 模板任务表 project_template_task
CREATE TABLE project_template_task (
                                       id bigint NOT NULL PRIMARY KEY,
                                       template_id bigint NOT NULL,
                                       parent_id bigint NULL,
                                       name varchar(128) NOT NULL,
                                       task_type varchar(32) NOT NULL,
                                       default_days int NULL,
                                       sort_order int DEFAULT 0,
                                       created_at datetime NOT NULL,
                                       KEY idx_template_task_template (template_id),
                                       KEY idx_template_task_parent (parent_id),
                                       CONSTRAINT fk_template_task_template FOREIGN KEY (template_id) REFERENCES project_template(id),
                                       CONSTRAINT fk_template_task_parent FOREIGN KEY (parent_id) REFERENCES project_template_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 4. 范围与任务管理表 */
-- 4.1 WBS表 wbs_node
CREATE TABLE wbs_node (
                          id bigint NOT NULL PRIMARY KEY,
                          project_id bigint NOT NULL,
                          parent_id bigint NULL,
                          node_code varchar(64) NOT NULL,
                          node_name varchar(128) NOT NULL,
                          description varchar(500) NULL,
                          owner_id bigint NULL,
                          sort_order int DEFAULT 0,
                          level_no int DEFAULT 1,
                          created_by bigint NULL,
                          created_at datetime NOT NULL,
                          updated_by bigint NULL,
                          updated_at datetime NOT NULL,
                          is_deleted tinyint(1) NOT NULL DEFAULT 0,
                          UNIQUE KEY uk_wbs_code (project_id, node_code),
                          KEY idx_wbs_parent (parent_id),
                          KEY idx_wbs_owner (owner_id),
                          CONSTRAINT fk_wbs_node_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                          CONSTRAINT fk_wbs_node_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id),
                          CONSTRAINT fk_wbs_node_parent FOREIGN KEY (parent_id) REFERENCES wbs_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4.2 需求表 requirement_info
CREATE TABLE requirement_info (
                                  id bigint NOT NULL PRIMARY KEY,
                                  project_id bigint NOT NULL,
                                  wbs_id bigint NULL,
                                  requirement_code varchar(64) NOT NULL,
                                  title varchar(255) NOT NULL,
                                  requirement_type varchar(32) NOT NULL,
                                  priority varchar(32) NOT NULL,
                                  status varchar(32) NOT NULL,
                                  description text NULL,
                                  proposer_id bigint NULL,
                                  created_by bigint NULL,
                                  created_at datetime NOT NULL,
                                  updated_by bigint NULL,
                                  updated_at datetime NOT NULL,
                                  is_deleted tinyint(1) NOT NULL DEFAULT 0,
                                  KEY idx_requirement_project (project_id),
                                  KEY idx_requirement_wbs (wbs_id),
                                  KEY idx_requirement_status (status),
                                  KEY idx_requirement_proposer (proposer_id),
                                  CONSTRAINT fk_requirement_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                  CONSTRAINT fk_requirement_wbs FOREIGN KEY (wbs_id) REFERENCES wbs_node(id),
                                  CONSTRAINT fk_requirement_proposer FOREIGN KEY (proposer_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4.3 范围基准表 scope_baseline
CREATE TABLE scope_baseline (
                                id bigint NOT NULL PRIMARY KEY,
                                project_id bigint NOT NULL,
                                version_no varchar(32) NOT NULL,
                                baseline_name varchar(128) NOT NULL,
                                description varchar(500) NULL,
                                snapshot_json longtext NOT NULL,
                                status varchar(32) NOT NULL,
                                published_by bigint NULL,
                                published_at datetime NULL,
                                created_at datetime NOT NULL,
                                UNIQUE KEY uk_scope_baseline_version (project_id, version_no),
                                KEY idx_scope_baseline_status (status),
                                KEY idx_scope_baseline_publisher (published_by),
                                CONSTRAINT fk_scope_baseline_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                CONSTRAINT fk_scope_baseline_publisher FOREIGN KEY (published_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4.4 里程碑表 milestone
CREATE TABLE milestone (
                           id bigint NOT NULL PRIMARY KEY,
                           project_id bigint NOT NULL,
                           name varchar(128) NOT NULL,
                           description varchar(500) NULL,
                           planned_date datetime NULL,
                           actual_date datetime NULL,
                           status varchar(32) NOT NULL,
                           owner_id bigint NULL,
                           created_by bigint NULL,
                           created_at datetime NOT NULL,
                           updated_by bigint NULL,
                           updated_at datetime NOT NULL,
                           is_deleted tinyint(1) NOT NULL DEFAULT 0,
                           KEY idx_milestone_project (project_id),
                           KEY idx_milestone_owner (owner_id),
                           KEY idx_milestone_status (status),
                           CONSTRAINT fk_milestone_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                           CONSTRAINT fk_milestone_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4.5 任务表 project_task
CREATE TABLE project_task (
                              id bigint NOT NULL PRIMARY KEY,
                              project_id bigint NOT NULL,
                              parent_task_id bigint NULL,
                              wbs_id bigint NULL,
                              milestone_id bigint NULL,
                              task_code varchar(64) NULL,
                              name varchar(128) NOT NULL,
                              description text NULL,
                              task_type varchar(32) NOT NULL,
                              priority varchar(32) NOT NULL,
                              status varchar(32) NOT NULL,
                              progress decimal(5,2) DEFAULT 0,
                              assignee_id bigint NULL,
                              planned_start_date datetime NULL,
                              planned_end_date datetime NULL,
                              actual_start_date datetime NULL,
                              actual_end_date datetime NULL,
                              planned_hours decimal(8,2) DEFAULT 0,
                              actual_hours decimal(8,2) DEFAULT 0,
                              planned_cost decimal(14,2) DEFAULT 0,
                              actual_cost decimal(14,2) DEFAULT 0,
                              sort_order int DEFAULT 0,
                              remark varchar(500) NULL,
                              created_by bigint NULL,
                              created_at datetime NOT NULL,
                              updated_by bigint NULL,
                              updated_at datetime NOT NULL,
                              is_deleted tinyint(1) NOT NULL DEFAULT 0,
                              KEY idx_task_project (project_id),
                              KEY idx_task_assignee (assignee_id),
                              KEY idx_task_status (status),
                              KEY idx_task_parent (parent_task_id),
                              KEY idx_task_plan_time (planned_start_date, planned_end_date),
                              KEY idx_task_milestone (milestone_id),
                              KEY idx_task_wbs (wbs_id),
                              CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                              CONSTRAINT fk_task_parent FOREIGN KEY (parent_task_id) REFERENCES project_task(id),
                              CONSTRAINT fk_task_wbs FOREIGN KEY (wbs_id) REFERENCES wbs_node(id),
                              CONSTRAINT fk_task_milestone FOREIGN KEY (milestone_id) REFERENCES milestone(id),
                              CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4.6 任务依赖表 task_dependency
CREATE TABLE task_dependency (
                                 id bigint NOT NULL PRIMARY KEY,
                                 project_id bigint NOT NULL,
                                 predecessor_task_id bigint NOT NULL,
                                 successor_task_id bigint NOT NULL,
                                 dependency_type varchar(8) NOT NULL,
                                 created_at datetime NOT NULL,
                                 UNIQUE KEY uk_task_dependency (project_id, predecessor_task_id, successor_task_id, dependency_type),
                                 KEY idx_task_dependency_predecessor (predecessor_task_id),
                                 KEY idx_task_dependency_successor (successor_task_id),
                                 CONSTRAINT fk_task_dependency_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                 CONSTRAINT fk_task_dependency_predecessor FOREIGN KEY (predecessor_task_id) REFERENCES project_task(id),
                                 CONSTRAINT fk_task_dependency_successor FOREIGN KEY (successor_task_id) REFERENCES project_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 5. 风险与变更管理表 */
-- 5.1 风险登记表 risk_register
CREATE TABLE risk_register (
                               id bigint NOT NULL PRIMARY KEY,
                               project_id bigint NOT NULL,
                               risk_code varchar(64) NULL,
                               name varchar(255) NOT NULL,
                               description text NULL,
                               probability int NOT NULL,
                               impact int NOT NULL,
                               level varchar(32) NOT NULL,
                               status varchar(32) NOT NULL,
                               response_strategy varchar(500) NULL,
                               task_id bigint NULL,
                               phase_name varchar(255) NULL,
                               owner_id bigint NULL,
                               identified_at datetime NULL,
                               closed_at datetime NULL,
                               created_by bigint NULL,
                               created_at datetime NOT NULL,
                               updated_by bigint NULL,
                               updated_at datetime NOT NULL,
                               is_deleted tinyint(1) NOT NULL DEFAULT 0,
                               KEY idx_risk_project (project_id),
                               KEY idx_risk_status (status),
                               KEY idx_risk_level (level),
                               KEY idx_risk_task (task_id),
                               KEY idx_risk_owner (owner_id),
                               CONSTRAINT fk_risk_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                               CONSTRAINT fk_risk_task FOREIGN KEY (task_id) REFERENCES project_task(id),
                               CONSTRAINT fk_risk_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5.2 变更请求表 change_request
CREATE TABLE change_request (
                                id bigint NOT NULL PRIMARY KEY,
                                project_id bigint NOT NULL,
                                change_code varchar(64) NULL,
                                title varchar(255) NOT NULL,
                                change_type varchar(32) NOT NULL,
                                priority varchar(32) NOT NULL,
                                status varchar(32) NOT NULL,
                                proposer_id bigint NULL,
                                approver_id bigint NULL,
                                reason text NULL,
                                impact_analysis text NULL,
                                decision_comment varchar(500) NULL,
                                submitted_at datetime NULL,
                                decided_at datetime NULL,
                                implemented_at datetime NULL,
                                created_by bigint NULL,
                                created_at datetime NOT NULL,
                                updated_by bigint NULL,
                                updated_at datetime NOT NULL,
                                is_deleted tinyint(1) NOT NULL DEFAULT 0,
                                KEY idx_change_project (project_id),
                                KEY idx_change_status (status),
                                KEY idx_change_proposer (proposer_id),
                                KEY idx_change_approver (approver_id),
                                CONSTRAINT fk_change_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                CONSTRAINT fk_change_proposer FOREIGN KEY (proposer_id) REFERENCES sys_user(id),
                                CONSTRAINT fk_change_approver FOREIGN KEY (approver_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5.3 变更日志表 change_request_log
CREATE TABLE change_request_log (
                                    id bigint NOT NULL PRIMARY KEY,
                                    change_request_id bigint NOT NULL,
                                    action varchar(64) NOT NULL,
                                    from_status varchar(32) NULL,
                                    to_status varchar(32) NULL,
                                    operator_id bigint NULL,
                                    comment varchar(500) NULL,
                                    created_at datetime NOT NULL,
                                    KEY idx_change_log_request (change_request_id),
                                    KEY idx_change_log_operator (operator_id),
                                    CONSTRAINT fk_change_log_request FOREIGN KEY (change_request_id) REFERENCES change_request(id),
                                    CONSTRAINT fk_change_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 6. 配置与基线管理表 */
-- 6.1 配置项表 config_item
CREATE TABLE config_item (
                             id bigint NOT NULL PRIMARY KEY,
                             project_id bigint NOT NULL,
                             item_name varchar(128) NOT NULL,
                             item_type varchar(32) NOT NULL,
                             version_no varchar(64) NULL,
                             status varchar(32) NOT NULL,
                             repository_url varchar(255) NULL,
                             remark varchar(500) NULL,
                             created_by bigint NULL,
                             created_at datetime NOT NULL,
                             updated_by bigint NULL,
                             updated_at datetime NOT NULL,
                             is_deleted tinyint(1) NOT NULL DEFAULT 0,
                             KEY idx_config_project (project_id),
                             KEY idx_config_type (item_type),
                             KEY idx_config_status (status),
                             CONSTRAINT fk_config_project FOREIGN KEY (project_id) REFERENCES project_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6.2 基线记录表 baseline_record
CREATE TABLE baseline_record (
                                 id bigint NOT NULL PRIMARY KEY,
                                 project_id bigint NOT NULL,
                                 baseline_type varchar(32) NOT NULL,
                                 version_no varchar(32) NOT NULL,
                                 baseline_name varchar(128) NOT NULL,
                                 snapshot_json longtext NOT NULL,
                                 status varchar(32) NOT NULL,
                                 published_by bigint NULL,
                                 published_at datetime NULL,
                                 created_at datetime NOT NULL,
                                 KEY idx_baseline_project (project_id),
                                 KEY idx_baseline_type (baseline_type),
                                 KEY idx_baseline_status (status),
                                 KEY idx_baseline_publisher (published_by),
                                 CONSTRAINT fk_baseline_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                 CONSTRAINT fk_baseline_publisher FOREIGN KEY (published_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 7. 成本与工时管理表 */
-- 7.1 成本计划表 cost_plan
CREATE TABLE cost_plan (
                           id bigint NOT NULL PRIMARY KEY,
                           project_id bigint NOT NULL,
                           task_id bigint NULL,
                           cost_type varchar(32) NOT NULL,
                           phase_name varchar(128) NULL,
                           item_name varchar(128) NOT NULL,
                           planned_amount decimal(14,2) NOT NULL,
                           currency varchar(16) NOT NULL DEFAULT 'CNY',
                           baseline_version varchar(32) NULL,
                           remark varchar(500) NULL,
                           created_by bigint NULL,
                           created_at datetime NOT NULL,
                           updated_by bigint NULL,
                           updated_at datetime NOT NULL,
                           is_deleted tinyint(1) NOT NULL DEFAULT 0,
                           KEY idx_cost_plan_project (project_id),
                           KEY idx_cost_plan_task (task_id),
                           KEY idx_cost_plan_type (cost_type),
                           CONSTRAINT fk_cost_plan_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                           CONSTRAINT fk_cost_plan_task FOREIGN KEY (task_id) REFERENCES project_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7.2 实际成本表 cost_actual
CREATE TABLE cost_actual (
                             id bigint NOT NULL PRIMARY KEY,
                             project_id bigint NOT NULL,
                             task_id bigint NULL,
                             cost_plan_id bigint NULL,
                             source_type varchar(32) NOT NULL,
                             actual_amount decimal(14,2) NOT NULL,
                             occurred_date date NOT NULL,
                             description varchar(500) NULL,
                             recorder_id bigint NULL,
                             created_at datetime NOT NULL,
                             updated_at datetime NOT NULL,
                             is_deleted tinyint(1) NOT NULL DEFAULT 0,
                             KEY idx_cost_actual_project (project_id),
                             KEY idx_cost_actual_task (task_id),
                             KEY idx_cost_actual_plan (cost_plan_id),
                             KEY idx_cost_actual_date (occurred_date),
                             CONSTRAINT fk_cost_actual_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                             CONSTRAINT fk_cost_actual_task FOREIGN KEY (task_id) REFERENCES project_task(id),
                             CONSTRAINT fk_cost_actual_plan FOREIGN KEY (cost_plan_id) REFERENCES cost_plan(id),
                             CONSTRAINT fk_cost_actual_recorder FOREIGN KEY (recorder_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7.3 工时表 timesheet
CREATE TABLE timesheet (
                           id bigint NOT NULL PRIMARY KEY,
                           project_id bigint NOT NULL,
                           task_id bigint NOT NULL,
                           user_id bigint NOT NULL,
                           work_date date NOT NULL,
                           hours decimal(6,2) NOT NULL,
                           cost_rate decimal(10,2) NULL,
                           labor_cost decimal(14,2) NULL,
                           description varchar(500) NULL,
                           status varchar(32) NOT NULL DEFAULT 'SUBMITTED',
                           created_at datetime NOT NULL,
                           updated_at datetime NOT NULL,
                           is_deleted tinyint(1) NOT NULL DEFAULT 0,
                           KEY idx_timesheet_project (project_id),
                           KEY idx_timesheet_user (user_id),
                           KEY idx_timesheet_task (task_id),
                           KEY idx_timesheet_work_date (user_id, work_date),
                           CONSTRAINT fk_timesheet_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                           CONSTRAINT fk_timesheet_task FOREIGN KEY (task_id) REFERENCES project_task(id),
                           CONSTRAINT fk_timesheet_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 8. 项目收尾表 */
-- 8.1 验收项表 acceptance_item
CREATE TABLE acceptance_item (
                                 id bigint NOT NULL PRIMARY KEY,
                                 project_id bigint NOT NULL,
                                 item_name varchar(255) NOT NULL,
                                 description varchar(500) NULL,
                                 status varchar(32) NOT NULL,
                                 checker_id bigint NULL,
                                 checked_at datetime NULL,
                                 remark varchar(500) NULL,
                                 created_at datetime NOT NULL,
                                 updated_at datetime NOT NULL,
                                 is_deleted tinyint(1) NOT NULL DEFAULT 0,
                                 KEY idx_acceptance_project (project_id),
                                 KEY idx_acceptance_status (status),
                                 KEY idx_acceptance_checker (checker_id),
                                 CONSTRAINT fk_acceptance_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                 CONSTRAINT fk_acceptance_checker FOREIGN KEY (checker_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8.2 归档表 archive_item
CREATE TABLE archive_item (
                              id bigint NOT NULL PRIMARY KEY,
                              project_id bigint NOT NULL,
                              archive_type varchar(32) NOT NULL,
                              item_name varchar(255) NOT NULL,
                              attachment_id bigint NULL,
                              repository_url varchar(255) NULL,
                              status varchar(32) NOT NULL,
                              archived_by bigint NULL,
                              archived_at datetime NULL,
                              created_at datetime NOT NULL,
                              updated_at datetime NOT NULL,
                              is_deleted tinyint(1) NOT NULL DEFAULT 0,
                              KEY idx_archive_project (project_id),
                              KEY idx_archive_type (archive_type),
                              KEY idx_archive_status (status),
                              CONSTRAINT fk_archive_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                              CONSTRAINT fk_archive_archived_by FOREIGN KEY (archived_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8.3 经验教训表 lesson_learned
CREATE TABLE lesson_learned (
                                id bigint NOT NULL PRIMARY KEY,
                                project_id bigint NOT NULL,
                                category varchar(64) NOT NULL,
                                title varchar(255) NOT NULL,
                                content text NOT NULL,
                                author_id bigint NULL,
                                created_at datetime NOT NULL,
                                updated_at datetime NOT NULL,
                                is_deleted tinyint(1) NOT NULL DEFAULT 0,
                                KEY idx_lesson_project (project_id),
                                KEY idx_lesson_category (category),
                                KEY idx_lesson_author (author_id),
                                CONSTRAINT fk_lesson_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                CONSTRAINT fk_lesson_author FOREIGN KEY (author_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8.4 报表记录表 report_record
CREATE TABLE report_record (
                               id bigint NOT NULL PRIMARY KEY,
                               project_id bigint NOT NULL,
                               report_type varchar(32) NOT NULL,
                               title varchar(255) NOT NULL,
                               start_date date NULL,
                               end_date date NULL,
                               content_json longtext NULL,
                               attachment_id bigint NULL,
                               generated_by bigint NULL,
                               generated_at datetime NOT NULL,
                               status varchar(32) NOT NULL,
                               is_deleted tinyint(1) NOT NULL DEFAULT 0,
                               KEY idx_report_project (project_id),
                               KEY idx_report_type (report_type),
                               KEY idx_report_status (status),
                               KEY idx_report_generator (generated_by),
                               CONSTRAINT fk_report_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                               CONSTRAINT fk_report_generator FOREIGN KEY (generated_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 9. 平台通用表 */
-- 9.1 附件表 attachment_info
CREATE TABLE attachment_info (
                                 id bigint NOT NULL PRIMARY KEY,
                                 file_name varchar(255) NOT NULL,
                                 file_url varchar(500) NOT NULL,
                                 file_size bigint NOT NULL,
                                 file_type varchar(64) NULL,
                                 biz_type varchar(32) NULL,
                                 biz_id bigint NULL,
                                 uploaded_by bigint NULL,
                                 uploaded_at datetime NOT NULL,
                                 is_deleted tinyint(1) NOT NULL DEFAULT 0,
                                 KEY idx_attachment_biz (biz_type, biz_id),
                                 KEY idx_attachment_uploader (uploaded_by),
                                 CONSTRAINT fk_attachment_uploader FOREIGN KEY (uploaded_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9.2 通知表 notification
CREATE TABLE notification (
                              id bigint NOT NULL PRIMARY KEY,
                              receiver_id bigint NOT NULL,
                              project_id bigint NULL,
                              biz_type varchar(32) NOT NULL,
                              biz_id bigint NULL,
                              title varchar(255) NOT NULL,
                              content varchar(500) NOT NULL,
                              read_status varchar(32) NOT NULL DEFAULT 'UNREAD',
                              read_at datetime NULL,
                              created_at datetime NOT NULL,
                              KEY idx_notification_receiver (receiver_id, read_status, created_at),
                              KEY idx_notification_project (project_id),
                              KEY idx_notification_biz (biz_type, biz_id),
                              CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES sys_user(id),
                              CONSTRAINT fk_notification_project FOREIGN KEY (project_id) REFERENCES project_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9.3 操作日志表 operation_log
CREATE TABLE operation_log (
                               id bigint NOT NULL PRIMARY KEY,
                               project_id bigint NULL,
                               module_name varchar(64) NOT NULL,
                               biz_id bigint NULL,
                               action varchar(64) NOT NULL,
                               operator_id bigint NULL,
                               operator_name varchar(64) NULL,
                               request_method varchar(16) NULL,
                               request_path varchar(255) NULL,
                               request_body longtext NULL,
                               result_code int NULL,
                               ip_address varchar(64) NULL,
                               created_at datetime NOT NULL,
                               KEY idx_operation_log_project (project_id),
                               KEY idx_operation_log_operator (operator_id),
                               KEY idx_operation_log_module (module_name),
                               KEY idx_operation_log_created (created_at),
                               CONSTRAINT fk_operation_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9.4 导出任务表 export_task
CREATE TABLE export_task (
                             id bigint NOT NULL PRIMARY KEY,
                             project_id bigint NULL,
                             module_name varchar(32) NOT NULL,
                             export_format varchar(16) NOT NULL,
                             filter_json longtext NULL,
                             status varchar(32) NOT NULL,
                             file_attachment_id bigint NULL,
                             requested_by bigint NULL,
                             requested_at datetime NOT NULL,
                             finished_at datetime NULL,
                             KEY idx_export_task_project (project_id),
                             KEY idx_export_task_status (status),
                             KEY idx_export_task_requester (requested_by),
                             CONSTRAINT fk_export_task_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                             CONSTRAINT fk_export_task_attachment FOREIGN KEY (file_attachment_id) REFERENCES attachment_info(id),
                             CONSTRAINT fk_export_task_requester FOREIGN KEY (requested_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 10. V1.1 扩展表 */
-- 10.1 质量计划表 quality_plan
CREATE TABLE quality_plan (
                              id bigint NOT NULL PRIMARY KEY,
                              project_id bigint NOT NULL,
                              title varchar(255) NOT NULL,
                              quality_standard text NULL,
                              acceptance_rule text NULL,
                              owner_id bigint NULL,
                              status varchar(32) NOT NULL,
                              created_at datetime NOT NULL,
                              updated_at datetime NOT NULL,
                              is_deleted tinyint(1) NOT NULL DEFAULT 0,
                              KEY idx_quality_plan_project (project_id),
                              KEY idx_quality_plan_owner (owner_id),
                              KEY idx_quality_plan_status (status),
                              CONSTRAINT fk_quality_plan_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                              CONSTRAINT fk_quality_plan_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.2 质量活动表 quality_activity
CREATE TABLE quality_activity (
                                  id bigint NOT NULL PRIMARY KEY,
                                  project_id bigint NOT NULL,
                                  quality_plan_id bigint NULL,
                                  activity_name varchar(255) NOT NULL,
                                  activity_type varchar(32) NOT NULL,
                                  planned_date datetime NULL,
                                  actual_date datetime NULL,
                                  result varchar(255) NULL,
                                  owner_id bigint NULL,
                                  created_at datetime NOT NULL,
                                  updated_at datetime NOT NULL,
                                  is_deleted tinyint(1) NOT NULL DEFAULT 0,
                                  KEY idx_quality_activity_project (project_id),
                                  KEY idx_quality_activity_plan (quality_plan_id),
                                  KEY idx_quality_activity_owner (owner_id),
                                  CONSTRAINT fk_quality_activity_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                  CONSTRAINT fk_quality_activity_plan FOREIGN KEY (quality_plan_id) REFERENCES quality_plan(id),
                                  CONSTRAINT fk_quality_activity_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.3 质量指标表 quality_metric
CREATE TABLE quality_metric (
                                id bigint NOT NULL PRIMARY KEY,
                                project_id bigint NOT NULL,
                                metric_name varchar(128) NOT NULL,
                                metric_value decimal(14,4) NULL,
                                metric_unit varchar(32) NULL,
                                statistic_date date NULL,
                                created_at datetime NOT NULL,
                                KEY idx_quality_metric_project (project_id),
                                KEY idx_quality_metric_date (statistic_date),
                                CONSTRAINT fk_quality_metric_project FOREIGN KEY (project_id) REFERENCES project_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.4 测试计划表 test_plan
CREATE TABLE test_plan (
                           id bigint NOT NULL PRIMARY KEY,
                           project_id bigint NOT NULL,
                           title varchar(255) NOT NULL,
                           version_no varchar(32) NULL,
                           scope_desc text NULL,
                           owner_id bigint NULL,
                           status varchar(32) NOT NULL,
                           created_at datetime NOT NULL,
                           updated_at datetime NOT NULL,
                           is_deleted tinyint(1) NOT NULL DEFAULT 0,
                           KEY idx_test_plan_project (project_id),
                           KEY idx_test_plan_owner (owner_id),
                           KEY idx_test_plan_status (status),
                           CONSTRAINT fk_test_plan_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                           CONSTRAINT fk_test_plan_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.5 测试用例表 test_case
CREATE TABLE test_case (
                           id bigint NOT NULL PRIMARY KEY,
                           project_id bigint NOT NULL,
                           test_plan_id bigint NULL,
                           requirement_id bigint NULL,
                           task_id bigint NULL,
                           case_code varchar(64) NOT NULL,
                           title varchar(255) NOT NULL,
                           precondition text NULL,
                           steps longtext NULL,
                           expected_result longtext NULL,
                           actual_result longtext NULL,
                           execution_status varchar(32) NOT NULL,
                           tester_id bigint NULL,
                           executed_at datetime NULL,
                           created_at datetime NOT NULL,
                           updated_at datetime NOT NULL,
                           is_deleted tinyint(1) NOT NULL DEFAULT 0,
                           UNIQUE KEY uk_test_case_code (project_id, case_code),
                           KEY idx_test_case_plan (test_plan_id),
                           KEY idx_test_case_requirement (requirement_id),
                           KEY idx_test_case_task (task_id),
                           KEY idx_test_case_tester (tester_id),
                           KEY idx_test_case_status (execution_status),
                           CONSTRAINT fk_test_case_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                           CONSTRAINT fk_test_case_plan FOREIGN KEY (test_plan_id) REFERENCES test_plan(id),
                           CONSTRAINT fk_test_case_requirement FOREIGN KEY (requirement_id) REFERENCES requirement_info(id),
                           CONSTRAINT fk_test_case_task FOREIGN KEY (task_id) REFERENCES project_task(id),
                           CONSTRAINT fk_test_case_tester FOREIGN KEY (tester_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.6 缺陷表 defect_record
CREATE TABLE defect_record (
                               id bigint NOT NULL PRIMARY KEY,
                               project_id bigint NOT NULL,
                               test_case_id bigint NULL,
                               requirement_id bigint NULL,
                               task_id bigint NULL,
                               defect_code varchar(64) NULL,
                               title varchar(255) NOT NULL,
                               severity varchar(32) NOT NULL,
                               priority varchar(32) NOT NULL,
                               status varchar(32) NOT NULL,
                               reporter_id bigint NULL,
                               assignee_id bigint NULL,
                               description text NULL,
                               resolution varchar(500) NULL,
                               created_at datetime NOT NULL,
                               updated_at datetime NOT NULL,
                               is_deleted tinyint(1) NOT NULL DEFAULT 0,
                               KEY idx_defect_project (project_id),
                               KEY idx_defect_test_case (test_case_id),
                               KEY idx_defect_requirement (requirement_id),
                               KEY idx_defect_task (task_id),
                               KEY idx_defect_severity (severity),
                               KEY idx_defect_status (status),
                               KEY idx_defect_reporter (reporter_id),
                               KEY idx_defect_assignee (assignee_id),
                               CONSTRAINT fk_defect_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                               CONSTRAINT fk_defect_test_case FOREIGN KEY (test_case_id) REFERENCES test_case(id),
                               CONSTRAINT fk_defect_requirement FOREIGN KEY (requirement_id) REFERENCES requirement_info(id),
                               CONSTRAINT fk_defect_task FOREIGN KEY (task_id) REFERENCES project_task(id),
                               CONSTRAINT fk_defect_reporter FOREIGN KEY (reporter_id) REFERENCES sys_user(id),
                               CONSTRAINT fk_defect_assignee FOREIGN KEY (assignee_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.7 沟通矩阵表 communication_matrix
CREATE TABLE communication_matrix (
                                      id bigint NOT NULL PRIMARY KEY,
                                      project_id bigint NOT NULL,
                                      sender_role varchar(64) NOT NULL,
                                      receiver_role varchar(64) NOT NULL,
                                      channel varchar(32) NOT NULL,
                                      frequency varchar(64) NULL,
                                      topic varchar(255) NULL,
                                      created_at datetime NOT NULL,
                                      updated_at datetime NOT NULL,
                                      KEY idx_communication_matrix_project (project_id),
                                      KEY idx_communication_matrix_roles (sender_role, receiver_role),
                                      CONSTRAINT fk_communication_matrix_project FOREIGN KEY (project_id) REFERENCES project_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.8 会议计划表 meeting_plan
CREATE TABLE meeting_plan (
                              id bigint NOT NULL PRIMARY KEY,
                              project_id bigint NOT NULL,
                              meeting_type varchar(32) NOT NULL,
                              title varchar(255) NOT NULL,
                              scheduled_at datetime NOT NULL,
                              duration_minutes int NULL,
                              host_id bigint NULL,
                              location varchar(255) NULL,
                              status varchar(32) NOT NULL,
                              created_at datetime NOT NULL,
                              updated_at datetime NOT NULL,
                              KEY idx_meeting_plan_project (project_id),
                              KEY idx_meeting_plan_type (meeting_type),
                              KEY idx_meeting_plan_host (host_id),
                              KEY idx_meeting_plan_status (status),
                              KEY idx_meeting_plan_schedule (scheduled_at),
                              CONSTRAINT fk_meeting_plan_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                              CONSTRAINT fk_meeting_plan_host FOREIGN KEY (host_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.9 沟通记录表 communication_record
CREATE TABLE communication_record (
                                      id bigint NOT NULL PRIMARY KEY,
                                      project_id bigint NOT NULL,
                                      meeting_id bigint NULL,
                                      record_type varchar(32) NOT NULL,
                                      title varchar(255) NOT NULL,
                                      content longtext NOT NULL,
                                      recorder_id bigint NULL,
                                      created_at datetime NOT NULL,
                                      updated_at datetime NOT NULL,
                                      is_deleted tinyint(1) NOT NULL DEFAULT 0,
                                      KEY idx_communication_record_project (project_id),
                                      KEY idx_communication_record_meeting (meeting_id),
                                      KEY idx_communication_record_type (record_type),
                                      KEY idx_communication_record_recorder (recorder_id),
                                      CONSTRAINT fk_communication_record_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                      CONSTRAINT fk_communication_record_meeting FOREIGN KEY (meeting_id) REFERENCES meeting_plan(id),
                                      CONSTRAINT fk_communication_record_recorder FOREIGN KEY (recorder_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.10 任务评论表 task_comment
CREATE TABLE task_comment (
                              id bigint NOT NULL PRIMARY KEY,
                              project_id bigint NOT NULL,
                              task_id bigint NOT NULL,
                              user_id bigint NOT NULL,
                              content text NOT NULL,
                              reply_to_id bigint NULL,
                              created_at datetime NOT NULL,
                              updated_at datetime NOT NULL,
                              is_deleted tinyint(1) NOT NULL DEFAULT 0,
                              KEY idx_task_comment_project (project_id),
                              KEY idx_task_comment_task (task_id),
                              KEY idx_task_comment_user (user_id),
                              KEY idx_task_comment_reply (reply_to_id),
                              CONSTRAINT fk_task_comment_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                              CONSTRAINT fk_task_comment_task FOREIGN KEY (task_id) REFERENCES project_task(id),
                              CONSTRAINT fk_task_comment_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
                              CONSTRAINT fk_task_comment_reply FOREIGN KEY (reply_to_id) REFERENCES task_comment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.11 供应商表 supplier_info
CREATE TABLE supplier_info (
                               id bigint NOT NULL PRIMARY KEY,
                               project_id bigint NOT NULL,
                               supplier_name varchar(255) NOT NULL,
                               contact_name varchar(64) NULL,
                               contact_phone varchar(32) NULL,
                               contact_email varchar(128) NULL,
                               address varchar(255) NULL,
                               remark varchar(500) NULL,
                               created_at datetime NOT NULL,
                               updated_at datetime NOT NULL,
                               is_deleted tinyint(1) NOT NULL DEFAULT 0,
                               KEY idx_supplier_project (project_id),
                               KEY idx_supplier_name (supplier_name),
                               CONSTRAINT fk_supplier_project FOREIGN KEY (project_id) REFERENCES project_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.12 采购项表 procurement_item
CREATE TABLE procurement_item (
                                  id bigint NOT NULL PRIMARY KEY,
                                  project_id bigint NOT NULL,
                                  supplier_id bigint NULL,
                                  cost_plan_id bigint NULL,
                                  item_name varchar(255) NOT NULL,
                                  quantity int NOT NULL,
                                  unit_price decimal(14,2) NULL,
                                  total_amount decimal(14,2) NULL,
                                  status varchar(32) NOT NULL,
                                  expected_delivery_date date NULL,
                                  actual_delivery_date date NULL,
                                  created_at datetime NOT NULL,
                                  updated_at datetime NOT NULL,
                                  is_deleted tinyint(1) NOT NULL DEFAULT 0,
                                  KEY idx_procurement_project (project_id),
                                  KEY idx_procurement_supplier (supplier_id),
                                  KEY idx_procurement_plan (cost_plan_id),
                                  KEY idx_procurement_status (status),
                                  CONSTRAINT fk_procurement_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                                  CONSTRAINT fk_procurement_supplier FOREIGN KEY (supplier_id) REFERENCES supplier_info(id),
                                  CONSTRAINT fk_procurement_plan FOREIGN KEY (cost_plan_id) REFERENCES cost_plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10.13 合同表 contract_info
CREATE TABLE contract_info (
                               id bigint NOT NULL PRIMARY KEY,
                               project_id bigint NOT NULL,
                               supplier_id bigint NULL,
                               contract_no varchar(64) NOT NULL,
                               contract_name varchar(255) NOT NULL,
                               contract_type varchar(32) NOT NULL,
                               sign_date date NULL,
                               total_amount decimal(14,2) NULL,
                               deliverables text NULL,
                               payment_terms text NULL,
                               status varchar(32) NOT NULL,
                               created_at datetime NOT NULL,
                               updated_at datetime NOT NULL,
                               is_deleted tinyint(1) NOT NULL DEFAULT 0,
                               UNIQUE KEY uk_contract_no (project_id, contract_no),
                               KEY idx_contract_project (project_id),
                               KEY idx_contract_supplier (supplier_id),
                               KEY idx_contract_status (status),
                               CONSTRAINT fk_contract_project FOREIGN KEY (project_id) REFERENCES project_info(id),
                               CONSTRAINT fk_contract_supplier FOREIGN KEY (supplier_id) REFERENCES supplier_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 恢复外键检查 */
SET FOREIGN_KEY_CHECKS = 1;

/* 预置基础数据 */
-- 预置角色
INSERT INTO sys_role (id, role_code, role_name, description, created_at, updated_at) VALUES
                                                                                         (1, 'SYS_ADMIN', '系统管理员', '系统最高权限', NOW(), NOW()),
                                                                                         (2, 'PROJECT_MANAGER', '项目经理', '项目管理权限', NOW(), NOW()),
                                                                                         (3, 'TEAM_MEMBER', '项目成员', '项目执行权限', NOW(), NOW()),
                                                                                         (4, 'READ_ONLY', '只读用户', '仅查看权限', NOW(), NOW());

-- 预置项目模板
INSERT INTO project_template (id, name, type, status, created_at, updated_at) VALUES
                                                                                  (1, '默认瀑布模板', 'WATERFALL', 'ENABLED', NOW(), NOW()),
                                                                                  (2, '默认敏捷模板', 'AGILE', 'ENABLED', NOW(), NOW());
