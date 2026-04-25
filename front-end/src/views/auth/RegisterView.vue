<script setup>
import { reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import presentationImage from '@/assets/Presentation.png'
import AuthLayout from '@/layouts/AuthLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: '',
})

const errors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
})

function resetErrors() {
  errors.username = ''
  errors.password = ''
  errors.confirmPassword = ''
  errors.realName = ''
}

async function handleSubmit() {
  resetErrors()

  if (!form.username) {
    errors.username = '请输入登录账号'
  }

  if (!form.realName) {
    errors.realName = '请输入真实姓名'
  }

  if (!form.password) {
    errors.password = '请输入登录密码'
  } else if (form.password.length < 6) {
    errors.password = '密码长度不能少于 6 位'
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = '请再次输入密码'
  } else if (form.confirmPassword !== form.password) {
    errors.confirmPassword = '两次输入的密码不一致'
  }

  if (Object.values(errors).some(Boolean)) {
    return
  }

  try {
    await authStore.registerAction({
      username: form.username,
      password: form.password,
      realName: form.realName,
      email: form.email || undefined,
      phone: form.phone || undefined,
    })

    ElMessage.success('注册成功，正在跳转到登录页')
    setTimeout(() => {
      router.replace('/login')
    }, 900)
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请稍后重试')
  }
}
</script>

<template>
  <AuthLayout
    brand-text="软件项目管理系统"
    title="创建项目管理账户"
    description="填写基础信息后完成注册。"
    :image-src="presentationImage"
    image-alt="项目汇报与数据展示插图"
  >
    <form class="form-body" @submit.prevent="handleSubmit">
      <div class="form-grid">
        <BaseInput
          id="username"
          v-model="form.username"
          label="登录账号"
          placeholder="例如 zhangsan"
          autocomplete="username"
          :error="errors.username"
        />
        <BaseInput
          id="realName"
          v-model="form.realName"
          label="真实姓名"
          placeholder="例如 张三"
          autocomplete="name"
          :error="errors.realName"
        />
        <BaseInput
          id="password"
          v-model="form.password"
          label="登录密码"
          type="password"
          placeholder="不少于 6 位"
          autocomplete="new-password"
          :error="errors.password"
        />
        <BaseInput
          id="confirmPassword"
          v-model="form.confirmPassword"
          label="确认密码"
          type="password"
          placeholder="再次输入密码"
          autocomplete="new-password"
          :error="errors.confirmPassword"
        />
        <BaseInput
          id="email"
          v-model="form.email"
          label="邮箱"
          type="email"
          placeholder="可选"
          autocomplete="email"
        />
        <BaseInput
          id="phone"
          v-model="form.phone"
          label="手机号"
          placeholder="可选"
          autocomplete="tel"
        />
      </div>

      <BaseButton type="submit" block :loading="authStore.loading">注册</BaseButton>
    </form>

    <div class="form-footer">
      <span>已经有账号？</span>
      <RouterLink to="/login">返回登录</RouterLink>
    </div>
  </AuthLayout>
</template>

<style scoped>
.form-body {
  display: grid;
  gap: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 14px;
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

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
