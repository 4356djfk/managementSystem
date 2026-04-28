<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

import UserAvatar from '@/components/ui/UserAvatar.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const profileSaving = ref(false)
const currentUser = computed(() => authStore.user || null)
const profileForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
})

const profileDisplayName = computed(() => profileForm.realName || profileForm.username || '未登录')
const accountStatusLabel = computed(() => {
  const map = {
    ACTIVE: '正常',
    DISABLED: '已停用',
    LOCKED: '已锁定',
  }

  return map[currentUser.value?.status] || currentUser.value?.status || '未知'
})

watch(
  currentUser,
  (user) => {
    profileForm.username = user?.username || ''
    profileForm.realName = user?.realName || ''
    profileForm.email = user?.email || ''
    profileForm.phone = user?.phone || ''
  },
  { immediate: true },
)

async function saveProfile() {
  if (!profileForm.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }

  if (!profileForm.realName.trim()) {
    ElMessage.warning('请输入姓名')
    return
  }

  profileSaving.value = true
  try {
    await authStore.updateProfileAction({
      username: profileForm.username.trim(),
      realName: profileForm.realName.trim(),
      email: profileForm.email.trim(),
      phone: profileForm.phone.trim(),
    })
    ElMessage.success('个人信息已更新')
  } catch (error) {
    ElMessage.error(error.message || '个人信息更新失败')
  } finally {
    profileSaving.value = false
  }
}
</script>

<template>
  <section class="profile-view surface-card">
    <template v-if="currentUser">
      <header class="page-header">
        <div class="page-copy">
          <h1>个人资料</h1>
          <p>维护当前登录账号的基础资料，保存后会同步更新系统显示。</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存更改</el-button>
        </div>
      </header>

      <section class="profile-panel">
        <div class="profile-strip">
          <UserAvatar
            :avatar-url="currentUser.avatarUrl"
            :username="profileForm.username"
            :display-name="profileDisplayName"
            :size="64"
            shape="square"
            appearance="flat"
          />
          <div class="summary-user">
            <strong>{{ profileDisplayName }}</strong>
            <span>@{{ profileForm.username || 'username' }}</span>
          </div>
          <span class="status-chip profile-status">状态：{{ accountStatusLabel }}</span>
        </div>

        <div class="panel-head">
          <div class="panel-copy">
            <strong>资料详情</strong>
            <span>支持修改用户名、姓名与联系方式。</span>
          </div>
        </div>

        <el-form label-position="top" class="profile-form">
          <div class="profile-form-grid">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="可选" />
            </el-form-item>
            <el-form-item label="手机">
              <el-input v-model="profileForm.phone" placeholder="可选" />
            </el-form-item>
          </div>
        </el-form>
      </section>
    </template>

    <div v-else class="profile-empty">
      <strong>未获取到当前用户信息</strong>
      <span>请重新登录后再查看个人资料。</span>
    </div>
  </section>
</template>

<style scoped>
.profile-view {
  min-height: 100vh;
  height: auto;
  display: grid;
  grid-template-rows: auto auto;
  align-content: start;
  gap: 12px;
  padding: 16px;
  border-radius: 0;
  background: #f5f6f8;
  border: 0;
  box-shadow: none;
  overflow: visible;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.page-copy {
  display: grid;
  gap: 6px;
}

.page-header h1 {
  margin: 0;
  font-size: 1.2rem;
  line-height: 1.2;
  color: #0f172a;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #475467;
  font-size: 0.92rem;
  line-height: 1.6;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  justify-content: flex-end;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  color: #1d4ed8;
  font-size: 0.84rem;
  font-weight: 500;
  background: #eaf2ff;
  border: 1px solid #bfdbfe;
}

.profile-panel,
.profile-empty {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid #d9dde5;
  background: #ffffff;
  min-height: 0;
}

.profile-strip {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #e5e7eb;
}

.summary-user {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.summary-user strong {
  color: #0f172a;
  font-size: 1rem;
  line-height: 1.2;
  font-weight: 600;
}

.summary-user span {
  color: #667085;
  font-size: 0.85rem;
  line-height: 1.4;
  word-break: break-all;
}

.profile-status {
  justify-self: end;
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e5e7eb;
}

.panel-copy {
  display: grid;
  gap: 2px;
}

.panel-copy strong,
.profile-empty strong {
  color: #0f172a;
  font-size: 0.95rem;
  font-weight: 600;
}

.panel-copy span,
.profile-empty span {
  color: #667085;
  font-size: 0.84rem;
  line-height: 1.5;
}

.profile-form {
  min-width: 0;
}

.profile-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.profile-view :deep(.el-form-item) {
  margin-bottom: 16px;
}

.profile-view :deep(.el-form-item__label) {
  padding-bottom: 6px;
  color: #475467;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
}

.profile-view :deep(.el-input__wrapper) {
  border-radius: 0;
  box-shadow: inset 0 0 0 1px #d0d7e2;
  background: #ffffff;
}

.profile-view :deep(.el-input__wrapper:hover) {
  box-shadow: inset 0 0 0 1px #b8c1ce;
}

.profile-view :deep(.el-input__wrapper.is-focus) {
  box-shadow: inset 0 0 0 1px #2563eb;
}

.profile-view :deep(.el-button) {
  border-radius: 0;
}

@media (max-width: 640px) {
  .profile-view {
    padding: 16px;
  }

  .page-header,
  .panel-head {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: flex-start;
  }

  .profile-strip {
    grid-template-columns: auto 1fr;
  }

  .profile-status {
    justify-self: start;
    grid-column: 1 / -1;
  }

  .profile-form-grid {
    grid-template-columns: 1fr;
  }

  .header-actions :deep(.el-button),
  .panel-head :deep(.el-button) {
    width: 100%;
  }
}
</style>
