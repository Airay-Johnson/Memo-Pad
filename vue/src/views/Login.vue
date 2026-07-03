<template>
  <div style="width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
    <div style="width: 400px; background: #fff; border-radius: 16px; padding: 40px; box-shadow: 0 20px 60px rgba(0,0,0,0.15)">
      <div style="text-align: center; margin-bottom: 32px">
        <div style="font-size: 28px; font-weight: bold; color: #333; margin-bottom: 8px">Memo Pad</div>
        <div style="font-size: 14px; color: #999">轻量笔记，高效生活</div>
      </div>

      <el-input v-model="form.username" placeholder="用户名" size="large" style="margin-bottom: 16px" />
      <el-input v-model="form.password" placeholder="密码" type="password" size="large" style="margin-bottom: 24px" @keyup.enter="login" />

      <el-button type="primary" size="large" style="width: 100%; height: 44px; font-size: 16px" :loading="loading" @click="login">
        登 录
      </el-button>

      <div style="text-align: center; margin-top: 16px">
        <span style="color: #999; font-size: 13px">还没有账号？</span>
        <router-link to="/register" style="color: #667eea; font-size: 13px; cursor: pointer">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import request from "@/utils/request.js"
import { ElMessage } from "element-plus"

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', password: '' })

const login = () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  request.post('/admin/login', form.value).then(res => {
    loading.value = false
    if (res.code === '200') {
      localStorage.setItem('user', JSON.stringify(res.data))
      ElMessage.success('登录成功')
      router.push('/home')
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  }).catch(() => {
    loading.value = false
    ElMessage.error('登录失败，请检查网络')
  })
}
</script>
