<template>
  <el-dialog v-model="visible" :title="agentMode ? (planMode ? 'AI 规划执行' : 'AI Agent 助手') : 'AI 文字助手'" width="620px" :close-on-click-modal="false">
    <!-- 模式切换 -->
    <div style="margin-bottom: 16px; display: flex; gap: 8px; flex-wrap: wrap;">
      <el-radio-group v-model="agentMode" size="small">
        <el-radio-button :value="false">文字处理</el-radio-button>
        <el-radio-button :value="true">Agent</el-radio-button>
      </el-radio-group>
      <span style="font-size: 12px; color: #999; line-height: 28px;">
        {{ agentMode ? (planMode ? 'AI 先规划再执行复杂任务' : '查看、搜索、创建、修改、删除') : '选中文字后选操作' }}
      </span>
    </div>

    <!-- Agent 子模式：普通 vs 规划 -->
    <div v-if="agentMode" style="margin-bottom: 16px; display: flex; gap: 8px; align-items: center;">
      <el-switch v-model="planMode" size="small" />
      <span style="font-size: 13px; color: #666;">规划模式（适合多步复杂任务）</span>
      <el-button v-if="sessionId" size="small" text type="danger" @click="clearMemory" style="margin-left: auto;">
        清除记忆
      </el-button>
    </div>

    <!-- === Agent 模式 === -->
    <template v-if="agentMode">
      <div style="margin-bottom: 16px;">
        <div style="font-weight: 500; margin-bottom: 8px;">
          {{ planMode ? '请描述你的复杂需求，AI 会自动规划并执行：' : '你想让 AI 做什么？' }}
        </div>
        <el-input v-model="agentMessage" type="textarea" :rows="3"
          :placeholder="planMode
            ? '例如：搜索所有工作相关的笔记，读取出内容，总结要点，创建一篇汇总笔记'
            : '例如：查看我所有笔记 / 搜索关于会议的内容 / 帮我新建一篇日记'" />
      </div>

      <div v-if="aiResult" style="margin-bottom: 16px;">
        <div style="font-weight: 500; margin-bottom: 8px;">AI 回答：</div>
        <el-input v-model="aiResult" type="textarea" :rows="12" readonly
          style="background: #f8f9ff;" />
      </div>
    </template>

    <!-- === 文字处理模式 === -->
    <template v-else>
      <div style="margin-bottom: 16px;">
        <div style="font-weight: 500; margin-bottom: 8px;">你想让 AI 做什么？</div>
        <el-input v-model="userRequest" type="textarea" :rows="2"
          placeholder="例如：把这段话改得更文艺一点 / 翻译成英文 / 总结要点" />
      </div>
      <div style="margin-bottom: 16px;">
        <span style="margin-right: 12px; font-weight: 500;">快捷操作：</span>
        <el-select v-model="action" style="width: 200px;" :disabled="!!userRequest.trim()">
          <el-option label="润色文字" value="polish" />
          <el-option label="总结内容" value="summarize" />
          <el-option label="扩写内容" value="expand" />
          <el-option label="翻译成英文" value="translate" />
        </el-select>
      </div>
      <div style="margin-bottom: 16px;">
        <div style="font-weight: 500; margin-bottom: 8px;">原文：</div>
        <el-input v-model="inputText" type="textarea" :rows="5" placeholder="将使用当前笔记内容" />
      </div>
      <div v-if="aiResult" style="margin-bottom: 16px;">
        <div style="font-weight: 500; margin-bottom: 8px;">AI 结果：</div>
        <el-input v-model="aiResult" type="textarea" :rows="5" readonly style="background: #f8f9ff;" />
      </div>
    </template>

    <!-- 加载状态 -->
    <div v-if="loading" style="text-align: center; color: #667eea; margin: 16px 0;">
      <el-icon class="is-loading" style="margin-right: 6px;"><Loading /></el-icon>
      {{ planMode ? 'AI 正在规划和执行中，可能需要较长时间...' : 'AI 正在思考和执行中...' }}
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="sendToAi">
        {{ loading ? '请求中...' : '发送给 AI' }}
      </el-button>
      <el-button v-if="aiResult && !agentMode" type="success" @click="applyResult">
        替换到笔记
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import request from '@/utils/request.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: Boolean,
  content: String
})
const emit = defineEmits(['update:modelValue', 'replace'])

const visible = ref(false)
const agentMode = ref(false)
const planMode = ref(false)
const agentMessage = ref('')
const action = ref('polish')
const userRequest = ref('')
const inputText = ref('')
const aiResult = ref('')
const loading = ref(false)

// Layer 4 记忆：每个会话一个唯一 ID，跨轮对话保持同一 session
const sessionId = ref('')

// 弹窗打开时生成或沿用 sessionId
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    inputText.value = props.content || ''
    userRequest.value = ''
    agentMessage.value = ''
    action.value = 'polish'
    aiResult.value = ''
    // 首次打开 Agent 生成 sessionId，之后保持（刷新页面会重置）
    if (!sessionId.value) {
      sessionId.value = crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(36)
    }
  }
})
watch(visible, (val) => { emit('update:modelValue', val) })

const sendToAi = () => {
  if (!agentMode.value) {
    sendChatMode()
  } else if (planMode.value) {
    sendPlanMode()
  } else {
    sendAgentMode()
  }
}

// === Agent 普通模式：POST /ai/agent ===
const sendAgentMode = () => {
  const msg = agentMessage.value.trim()
  if (!msg) { ElMessage.warning('请输入你的需求'); return }
  loading.value = true
  aiResult.value = ''

  request.post('/ai/agent', { message: msg, sessionId: sessionId.value }, { timeout: 120000 })
    .then(res => {
      loading.value = false
      if (res.code === '200') {
        aiResult.value = res.data
      } else {
        ElMessage.error(res.msg || 'Agent 执行失败')
      }
    })
    .catch(err => {
      loading.value = false
      ElMessage.error('请求失败：' + (err.message || '网络错误'))
    })
}

// === Agent 规划模式：POST /ai/agent/plan ===
const sendPlanMode = () => {
  const msg = agentMessage.value.trim()
  if (!msg) { ElMessage.warning('请输入你的需求'); return }
  loading.value = true
  aiResult.value = ''

  request.post('/ai/agent/plan', { message: msg, sessionId: sessionId.value }, { timeout: 180000 })
    .then(res => {
      loading.value = false
      if (res.code === '200') {
        aiResult.value = res.data
      } else {
        ElMessage.error(res.msg || '规划执行失败')
      }
    })
    .catch(err => {
      loading.value = false
      ElMessage.error('请求失败：' + (err.message || '网络错误'))
    })
}

// === 文字处理模式 ===
const sendChatMode = () => {
  const text = inputText.value.trim()
  if (!text) { ElMessage.warning('请先输入或粘贴要处理的文字'); return }
  loading.value = true
  aiResult.value = ''

  request.post('/ai/chat', {
    text,
    action: action.value,
    userRequest: userRequest.value.trim()
  }, { timeout: 60000 })
    .then(res => {
      loading.value = false
      if (res.code === '200') {
        try {
          const aiJson = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          aiResult.value = aiJson.choices[0].message.content
        } catch (e) {
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

// === 清除记忆 ===
const clearMemory = () => {
  ElMessageBox.confirm('确定要清除当前会话的 AI 记忆吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete('/ai/memory', { data: { sessionId: sessionId.value } })
      .then(() => {
        ElMessage.success('记忆已清除')
      })
  }).catch(() => {})
}

const applyResult = () => {
  emit('replace', aiResult.value)
  visible.value = false
  ElMessage.success('已替换到笔记')
}
</script>
