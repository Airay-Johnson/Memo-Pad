<template>
  <div style="width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
    <div style="width: 400px; background: #fff; border-radius: 16px; padding: 40px; box-shadow: 0 20px 60px rgba(0,0,0,0.15)">
      <div style="text-align: center; margin-bottom: 32px">
        <div style="font-size: 28px; font-weight: bold; color: #333; margin-bottom: 8px">创建账号</div>
        <div style="font-size: 14px; color: #999">加入 Memo Pad，开始记录生活</div>
      </div>

      <el-input v-model="form.username" placeholder="用户名" size="large" style="margin-bottom: 16px" />
      <el-input v-model="form.name" placeholder="昵称（选填）" size="large" style="margin-bottom: 16px" />
      <el-input v-model="form.password" placeholder="密码" type="password" size="large" style="margin-bottom: 8px" />
      <el-input v-model="form.password2" placeholder="确认密码" type="password" size="large" style="margin-bottom: 24px" @keyup.enter="register" />

      <el-button type="primary" size="large" style="width: 100%; height: 44px; font-size: 16px" :loading="loading" @click="register">
        注 册
      </el-button>

      <div style="text-align: center; margin-top: 16px">
        <span style="color: #999; font-size: 13px">已有账号？</span>
        <router-link to="/login" style="color: #667eea; font-size: 13px; cursor: pointer">去登录</router-link>
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
const form = ref({ username: '', password: '', password2: '', name: '' })

const register = () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (form.value.password !== form.value.password2) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  loading.value = true
  request.post('/admin/register', {
    username: form.value.username,
    password: form.value.password,
    name: form.value.name || form.value.username
  }).then(res => {
    loading.value = false
    if (res.code === '200') {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  }).catch(() => {
    loading.value = false
    ElMessage.error('注册失败，请检查网络')
  })
}
</script>
