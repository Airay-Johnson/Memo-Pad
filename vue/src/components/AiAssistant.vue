<template>
  <el-dialog v-model="visible" :title="agentMode ? (planMode ? 'AI 规划执行' : 'AI Agent 助手') : 'AI 文字助手'" width="680px" :close-on-click-modal="false">
    <div style="margin-bottom: 16px; display: flex; gap: 8px; flex-wrap: wrap;">
      <el-radio-group v-model="agentMode" size="small">
        <el-radio-button :value="false">文字处理</el-radio-button>
        <el-radio-button :value="true">Agent</el-radio-button>
      </el-radio-group>
      <span style="font-size: 12px; color: #999; line-height: 28px;">
        {{ agentMode ? (planMode ? '先规划再执行复杂任务' : '查看、搜索、创建、修改、删除') : '选中文字后选操作' }}
      </span>
    </div>

    <div v-if="agentMode" style="margin-bottom: 16px; display: flex; gap: 8px; align-items: center;">
      <el-switch v-model="planMode" size="small" />
      <span style="font-size: 13px; color: #666;">规划模式</span>
      <el-switch v-model="streamMode" size="small" style="margin-left: 8px;" />
      <span style="font-size: 13px; color: #666;">流式输出</span>
    </div>

    <template v-if="agentMode">
      <div style="margin-bottom: 16px;">
        <el-input v-model="agentMessage" type="textarea" :rows="3"
          :placeholder="planMode
            ? '例如：搜索所有工作相关的笔记，读取出内容，总结要点，创建一篇汇总笔记'
            : '例如：查看我所有笔记 / 搜索关于会议的内容 / 帮我新建一篇日记'" />
      </div>

      <div v-if="aiResult || streaming" style="margin-bottom: 16px;">
        <div style="font-weight: 500; margin-bottom: 8px;">
          AI 回答{{ streaming ? '（实时生成中...）' : '' }}：
        </div>
        <div
          ref="streamOutput"
          class="ai-output"
          v-html="renderedResult"
          style="background: #f8f9ff; border-radius: 8px; padding: 14px; min-height: 60px; max-height: 400px; overflow-y: auto; line-height: 1.8; font-size: 14px; white-space: pre-wrap; word-break: break-word;"
        />
      </div>
    </template>

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

    <div v-if="loading && !streaming" style="text-align: center; color: #667eea; margin: 16px 0;">
      <el-icon class="is-loading" style="margin-right: 6px;"><Loading /></el-icon>
      {{ planMode ? 'AI 正在规划和执行中...' : 'AI 正在思考和执行中...' }}
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading && !streaming" @click="sendToAi" :disabled="streaming">
        {{ streaming ? '生成中...' : (loading ? '请求中...' : '发送给 AI') }}
      </el-button>
      <el-button v-if="aiResult && !agentMode" type="success" @click="applyResult">
        替换到笔记
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue'
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
const streamMode = ref(true)
const agentMessage = ref('')
const action = ref('polish')
const userRequest = ref('')
const inputText = ref('')
const aiResult = ref('')
const loading = ref(false)
const streaming = ref(false)
const streamOutput = ref(null)

const sessionId = ref('')

const renderedResult = computed(() => {
  const text = aiResult.value
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<b>$1</b>')
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    inputText.value = props.content || ''
    userRequest.value = ''
    agentMessage.value = ''
    action.value = 'polish'
    aiResult.value = ''
    streaming.value = false
    if (!sessionId.value) {
      sessionId.value = crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(36)
    }
  }
})
watch(visible, (val) => { emit('update:modelValue', val) })

const sendToAi = () => {
  if (!agentMode.value) {
    sendChatMode()
  } else if (streamMode.value && !planMode.value) {
    sendStreamMode()
  } else if (planMode.value) {
    sendPlanMode()
  } else {
    sendAgentMode()
  }
}

const sendStreamMode = () => {
  const msg = agentMessage.value.trim()
  if (!msg) { ElMessage.warning('请输入你的需求'); return }
  streaming.value = true
  loading.value = true
  aiResult.value = ''

  request.streamPost('/ai/agent/stream', { message: msg, sessionId: sessionId.value }, {
    onToken: (text) => {
      aiResult.value += text
      nextTick(() => {
        const el = streamOutput.value
        if (el) el.scrollTop = el.scrollHeight
      })
    },
    onDone: () => {
      loading.value = false
      streaming.value = false
    },
    onError: (err) => {
      loading.value = false
      streaming.value = false
      ElMessage.error('流式请求失败：' + (err.message || '网络错误'))
    }
  })
}

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

const applyResult = () => {
  emit('replace', aiResult.value)
  visible.value = false
  ElMessage.success('已替换到笔记')
}
</script>

<style scoped>
.ai-output {
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.ai-output :deep(b) {
  color: #333;
}
</style>
