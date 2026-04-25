<script setup>
import { reactive } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import meetingImage from '@/assets/Meeting.png'
import AuthLayout from '@/layouts/AuthLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: '',
})

const errors = reactive({
  username: '',
  password: '',
})

function resetErrors() {
  errors.username = ''
  errors.password = ''
}

async function handleSubmit() {
  resetErrors()

  if (!form.username) {
    errors.username = '请输入登录账号'
  }

  if (!form.password) {
    errors.password = '请输入登录密码'
  }

  if (errors.username || errors.password) {
    return
  }

  try {
    await authStore.loginAction({
      username: form.username,
      password: form.password,
    })
    ElMessage.success('登录成功')
    router.replace(String(route.query.redirect || '/dashboard'))
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请检查账号和密码')
  }
}
</script>

<template>
  <AuthLayout
    brand-text="软件项目管理系统"
    title="欢迎来到项目管理系统"
    description="请输入账号和密码登录。"
    :image-src="meetingImage"
    image-alt="项目团队协作插图"
  >
    <form class="form-body" @submit.prevent="handleSubmit">
      <BaseInput
        id="username"
        v-model="form.username"
        label="登录账号"
        placeholder="请输入用户名"
        autocomplete="username"
        :error="errors.username"
      />
      <BaseInput
        id="password"
        v-model="form.password"
        label="登录密码"
        type="password"
        placeholder="请输入密码"
        autocomplete="current-password"
        :error="errors.password"
      />

      <BaseButton type="submit" block :loading="authStore.loading">登录</BaseButton>
    </form>

    <div class="form-footer">
      <span>还没有账号？</span>
      <RouterLink to="/register">立即注册</RouterLink>
    </div>
  </AuthLayout>
</template>

<style scoped>
.form-body {
  display: grid;
  gap: 12px;
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 14px;
  color: var(--muted-foreground);
  font-size: 0.88rem;
}

.form-footer a {
  color: var(--accent);
  font-weight: 700;
}
</style>
