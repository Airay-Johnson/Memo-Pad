<template>
  <!-- AI 助手弹窗 -->
  <el-dialog v-model="visible" title="AI 文字助手" width="550px" :close-on-click-modal="false">
    <!-- 操作选择 -->
    <div style="margin-bottom: 16px;">
      <span style="margin-right: 12px; font-weight: 500;">选择操作：</span>
      <el-select v-model="action" style="width: 200px;">
        <el-option label="润色文字" value="polish" />
        <el-option label="总结内容" value="summarize" />
        <el-option label="扩写内容" value="expand" />
        <el-option label="翻译成英文" value="translate" />
      </el-select>
    </div>

    <!-- 原文展示 -->
    <div style="margin-bottom: 16px;">
      <div style="font-weight: 500; margin-bottom: 8px;">原文：</div>
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="5"
        placeholder="将使用当前笔记内容"
      />
    </div>

    <!-- AI 结果 -->
    <div v-if="aiResult" style="margin-bottom: 16px;">
      <div style="font-weight: 500; margin-bottom: 8px;">AI 结果：</div>
      <el-input
        v-model="aiResult"
        type="textarea"
        :rows="5"
        readonly
        style="background: #f8f9ff;"
      />
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" style="text-align: center; color: #667eea; margin: 16px 0;">
      <el-icon class="is-loading" style="margin-right: 6px;"><Loading /></el-icon>
      AI 正在思考中...
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="sendToAi">
        {{ loading ? '请求中...' : '发送给 AI' }}
      </el-button>
      <el-button v-if="aiResult" type="success" @click="applyResult">
        替换到笔记
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

// === Props：父组件传进来的数据 ===
const props = defineProps({
  modelValue: Boolean,    // 控制弹窗显示/隐藏
  content: String         // 当前笔记内容
})

// === Emits：通知父组件的事件 ===
const emit = defineEmits(['update:modelValue', 'replace'])

// === 本地状态 ===
const visible = ref(false)
const action = ref('polish')
const inputText = ref('')
const aiResult = ref('')
const loading = ref(false)

// === 同步弹窗显示状态 ===
// 父组件设置 modelValue=true → 弹窗打开
// 父组件设置 modelValue=false → 弹窗关闭
watch(() => props.modelValue, (val) => {
  visible.value = val
  // 弹窗打开时，自动填入笔记内容
  if (val) {
    inputText.value = props.content || ''
    aiResult.value = ''
  }
})

// 弹窗关闭时，同步回去
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// === 发送请求到 AI ===
const sendToAi = () => {
  const text = inputText.value.trim()
  if (!text) {
    ElMessage.warning('请先输入或粘贴要处理的文字')
    return
  }
  loading.value = true
  aiResult.value = ''

  // 调用后端 /ai/chat 接口
  // timeout 60秒——AI 响应比较慢
  request.post('/ai/chat', { text, action: action.value }, { timeout: 60000 })
    .then(res => {
      loading.value = false
      if (res.code === '200') {
        // 后端返回的是通义千问的原始 JSON，需要提取 choices[0].message.content
        try {
          const aiJson = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          aiResult.value = aiJson.choices[0].message.content
        } catch (e) {
          // 解析失败就直接显示原始内容
          aiResult.value = res.data
        }
      } else {
        ElMessage.error(res.msg || 'AI 调用失败')
      }
    })
    .catch(err => {
      loading.value = false
      ElMessage.error('请求失败：' + (err.message || '网络错误'))
    })
}

// === 将 AI 结果替换到笔记 ===
const applyResult = () => {
  emit('replace', aiResult.value)
  visible.value = false
  ElMessage.success('已替换到笔记')
}
</script>
