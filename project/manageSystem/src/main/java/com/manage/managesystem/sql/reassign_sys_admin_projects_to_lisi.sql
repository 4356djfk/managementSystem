-- 将系统管理员作为创建者或负责人的项目迁移给 lisi
-- 适用场景：
-- 1. created_by 指向系统管理员
-- 2. owner_id 指向系统管理员
--
-- 执行前请确认：
-- 1. sys_user 中存在 username = 'lisi' 的账号
-- 2. lisi 具备业务用户能力（当前系统兼容 TEAM_MEMBER / PROJECT_MANAGER / READ_ONLY 旧角色并归一为 USER）

START TRANSACTION;

SET @lisi_id := (
    SELECT id
    FROM sys_user
    WHERE username = 'lisi'
      AND is_deleted = 0
    LIMIT 1
);

-- 修正创建者为系统管理员的项目
UPDATE project_info
SET created_by = @lisi_id,
    updated_by = @lisi_id,
    updated_at = NOW()
WHERE is_deleted = 0
  AND created_by IN (
      SELECT user_id
      FROM (
          SELECT ur.user_id
          FROM sys_user_role ur
                   INNER JOIN sys_role r ON r.id = ur.role_id
          WHERE r.role_code = 'SYS_ADMIN'
      ) sys_admin_users
  );

-- 修正负责人为系统管理员的项目
UPDATE project_info
SET owner_id = @lisi_id,
    updated_by = @lisi_id,
    updated_at = NOW()
WHERE is_deleted = 0
  AND owner_id IN (
      SELECT user_id
      FROM (
          SELECT ur.user_id
          FROM sys_user_role ur
                   INNER JOIN sys_role r ON r.id = ur.role_id
          WHERE r.role_code = 'SYS_ADMIN'
      ) sys_admin_users
  );

COMMIT;
