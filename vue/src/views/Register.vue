<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/>
            <rect x="8" y="2" width="8" height="4" rx="1"/>
            <path d="M9 14h6M9 18h6M13 10h2"/>
          </svg>
        </div>
        <div class="app-name">创建账号</div>
        <div class="app-slogan">加入 Memo Pad，开始高效记录</div>
      </div>

      <div class="input-group">
        <el-input
          v-model="form.email"
          placeholder="请输入邮箱"
          size="large"
          :prefix-icon="icons.Message"
          clearable
        />
      </div>
      <div class="input-group">
        <el-input
          v-model="form.password"
          placeholder="密码"
          type="password"
          size="large"
          :prefix-icon="icons.Lock"
          show-password
        />
      </div>
      <div class="input-group">
        <el-input
          v-model="form.password2"
          placeholder="确认密码"
          type="password"
          size="large"
          :prefix-icon="icons.Lock"
          show-password
          @keyup.enter="register"
        />
      </div>

      <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="register">
        注 册
      </el-button>

      <div class="register-link">
        <span>已有账号？</span>
        <router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef } from "vue"
import { useRouter } from "vue-router"
import request from "@/utils/request.js"
import { ElMessage } from "element-plus"
import { Message, Lock } from "@element-plus/icons-vue"

const router = useRouter()
const loading = ref(false)
const form = ref({ email: '', password: '', password2: '' })
const icons = { Message: shallowRef(Message), Lock: shallowRef(Lock) }

const register = () => {
  if (!form.value.email || !form.value.password) {
    ElMessage.warning('请填写邮箱和密码')
    return
  }
  if (form.value.password !== form.value.password2) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  loading.value = true
  request.post('/admin/register', {
    email: form.value.email,
    password: form.value.password
  }).then(res => {
    loading.value = false
    if (res.code === '200') {
      localStorage.removeItem('saved_email')
      localStorage.removeItem('login_password')
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

<style scoped>
.login-page {
  width: 100vw; height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #5a3f8a 100%);
  position: relative; overflow: hidden;
}
.login-page::before {
  content: '';
  position: absolute; width: 600px; height: 600px;
  border-radius: 50%; background: rgba(255,255,255,0.04);
  top: -200px; right: -100px;
}
.login-page::after {
  content: '';
  position: absolute; width: 400px; height: 400px;
  border-radius: 50%; background: rgba(255,255,255,0.04);
  bottom: -150px; left: -80px;
}
.login-card {
  position: relative; z-index: 1;
  width: 400px; background: rgba(255,255,255,0.97);
  backdrop-filter: blur(20px); border-radius: 20px;
  padding: 44px 40px 36px;
  box-shadow: 0 24px 80px rgba(0,0,0,0.15);
}
.login-header { text-align: center; margin-bottom: 36px; }
.logo-icon {
  display: inline-flex; align-items: center; justify-content: center;
  width: 60px; height: 60px; border-radius: 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; margin-bottom: 16px;
}
.app-name { font-size: 26px; font-weight: 800; color: #1a1a2e; margin-bottom: 6px; letter-spacing: -0.5px; }
.app-slogan { font-size: 13px; color: #aaa; letter-spacing: 2px; }
.input-group { margin-bottom: 14px; }
.login-btn {
  width: 100%; height: 48px; font-size: 16px; font-weight: 600;
  border-radius: 12px; background: linear-gradient(135deg, #667eea, #764ba2);
  border: none; box-shadow: 0 4px 16px rgba(102,126,234,0.35); transition: all 0.3s;
}
.login-btn:hover { box-shadow: 0 6px 24px rgba(102,126,234,0.5); transform: translateY(-1px); }
.register-link { text-align: center; margin-top: 20px; font-size: 13px; color: #999; }
.register-link a { color: #667eea; font-weight: 600; text-decoration: none; margin-left: 4px; }
.register-link a:hover { text-decoration: underline; }
</style>
