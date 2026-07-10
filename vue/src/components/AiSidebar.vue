<template>
  <Transition name="slide">
    <div v-if="visible" class="sidebar-overlay" @click.self="$emit('close')">
      <div class="sidebar-panel" @click.stop>
        <div class="sidebar-header">
          <div class="header-title">
            <span class="ai-badge">AI</span>
            Memo 智能助手
          </div>
          <button class="close-btn" @click="$emit('close')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>

        <div class="quick-actions">
          <div
            v-for="action in quickActions"
            :key="action.key"
            class="quick-card"
            @click="runQuickAction(action)"
          >
            <span class="quick-icon">{{ action.icon }}</span>
            <div class="quick-info">
              <div class="quick-label">{{ action.label }}</div>
              <div class="quick-desc">{{ action.desc }}</div>
            </div>
          </div>
        </div>

        <div class="chat-area" ref="chatArea">
          <div v-if="!messages.length && !streaming" class="welcome-msg">
            <div class="welcome-emoji">👋</div>
            <div>点击上方快捷指令，或直接输入需求</div>
          </div>

          <div v-for="(msg, i) in messages" :key="i" :class="['msg-row', msg.role]">
            <div class="msg-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
            <div class="msg-bubble" v-html="renderMsg(msg)"></div>
          </div>

          <div v-for="(tc, i) in toolCalls" :key="'tc'+i" class="tool-card">
            <span class="tool-dot"></span> 调用工具：{{ tc.name }}
            <span v-if="tc.result" class="tool-result">✓ {{ tc.result }}</span>
          </div>

          <div v-if="streaming" id="stream-target" class="msg-row ai">
            <div class="msg-avatar">🤖</div>
            <div class="msg-bubble streaming">{{ streamingText || (toolCalls.length ? '分析中...' : '思考中...') }}</div>
          </div>

          <div v-if="loading && !streaming" class="msg-row ai">
            <div class="msg-avatar">🤖</div>
            <div class="msg-bubble typing"><span></span><span></span><span></span></div>
          </div>
        </div>

        <div class="input-area">
          <div class="input-options">
            <label class="opt-label"><input type="checkbox" v-model="streamMode" /> 流式</label>
          </div>
          <div class="input-row">
            <input
              v-model="inputText"
              class="msg-input"
              placeholder="输入需求，Enter 发送..."
              @keydown.enter="send"
              :disabled="streaming"
            />
            <button class="send-btn" @click="send" :disabled="!inputText.trim() || streaming">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'

const props = defineProps({ visible: Boolean })
const emit = defineEmits(['close'])

const inputText = ref('')
const messages = ref([])
const loading = ref(false)
const streaming = ref(false)
const streamingText = ref('')
const toolCalls = ref([])
const streamMode = ref(true)
const chatArea = ref(null)
const sessionId = ref(crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(36))

const quickActions = [
  { key: 'list', icon: '📋', label: '查看笔记', desc: '列出所有笔记', prompt: '查看我所有笔记' },
  { key: 'search', icon: '🔍', label: '搜索笔记', desc: '按关键词搜索', prompt: '帮我搜索关于会议的笔记' },
  { key: 'create', icon: '📝', label: '新建笔记', desc: '创建一篇笔记', prompt: '帮我创建一篇新笔记' },
  { key: 'stats', icon: '📊', label: '笔记统计', desc: '查看统计信息', prompt: '帮我统计笔记情况' },
  { key: 'export', icon: '📄', label: '导出笔记', desc: 'Markdown导出', prompt: '帮我导出最新的一篇笔记为Markdown' },
  { key: 'summarize', icon: '✨', label: '智能总结', desc: '总结所有笔记要点', prompt: '读取我所有笔记，帮我总结要点' },
]

const runQuickAction = (action) => {
  inputText.value = action.prompt
  nextTick(() => send())
}

const send = () => {
  const msg = inputText.value.trim()
  if (!msg || streaming.value) return
  inputText.value = ''
  messages.value.push({ role: 'user', content: msg })

  if (streamMode.value) {
    sendStream(msg)
  } else {
    sendSync(msg)
  }
}

const sendStream = (msg) => {
  streaming.value = true
  streamingText.value = ''
  toolCalls.value = []
  loading.value = false

  request.streamPost('/ai/agent/stream', { message: msg, sessionId: sessionId.value }, {
    onToken: (text) => {
      streamingText.value += text
      nextTick(scrollBottom)
    },
    onTool: (name) => {
      toolCalls.value.push({ name, result: '' })
      nextTick(scrollBottom)
    },
    onDone: () => {
      if (toolCalls.value.length) {
        toolCalls.value[toolCalls.value.length - 1].result = '完成'
      }
      messages.value.push({ role: 'ai', content: streamingText.value })
      streamingText.value = ''
      streaming.value = false
      nextTick(scrollBottom)
    },
    onError: (err) => {
      streaming.value = false
      ElMessage.error('请求失败：' + (err.message || '网络错误'))
    }
  })
}

const sendSync = (msg) => {
  loading.value = true
  request.post('/ai/agent', { message: msg, sessionId: sessionId.value }, { timeout: 120000 })
    .then(res => {
      loading.value = false
      if (res.code === '200') {
        messages.value.push({ role: 'ai', content: res.data })
      } else {
        ElMessage.error(res.msg || '执行失败')
      }
      nextTick(scrollBottom)
    })
    .catch(err => {
      loading.value = false
      ElMessage.error('请求失败：' + (err.message || '网络错误'))
    })
}

const renderMsg = (msg) => {
  if (msg.role === 'user') return escapeHtml(msg.content)
  let html = escapeHtml(msg.content)
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<b>$1</b>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
  html = html.replace(/(Thought|思考)[:：](.*?)(?=<br>|$)/gi,
    '<span class="think-chain">💭 $1: $2</span>')
  html = html.replace(/(Action|行动)[:：](.*?)(?=<br>|$)/gi,
    '<span class="action-chain">🔧 $1: $2</span>')
  html = html.replace(/(Observation|观察)[:：](.*?)(?=<br>|$)/gi,
    '<span class="obs-chain">📋 $1: $2</span>')
  return html
}

const escapeHtml = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')

const scrollBottom = () => {
  const el = chatArea.value
  if (el) el.scrollTop = el.scrollHeight
}

watch(() => props.visible, (v) => {
  if (v) { nextTick(scrollBottom) }
})
</script>

<style scoped>
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.25);
  z-index: 9995;
  display: flex;
  justify-content: flex-end;
}
.sidebar-panel {
  width: 440px;
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: -8px 0 40px rgba(0, 0, 0, 0.1);
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}
.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 10px;
}
.ai-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 14px;
  font-weight: 800;
}
.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  transition: all 0.2s;
}
.close-btn:hover { background: #e8e8e8; color: #333; }

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}
.quick-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8f9ff;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.quick-card:hover {
  background: #f0f2ff;
  border-color: #d4d8f7;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}
.quick-icon { font-size: 22px; flex-shrink: 0; }
.quick-label { font-size: 13px; font-weight: 600; color: #333; }
.quick-desc { font-size: 11px; color: #999; margin-top: 2px; }

.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}
.welcome-msg {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 40px 20px;
}
.welcome-emoji { font-size: 48px; margin-bottom: 12px; }

.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
}
.msg-row.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}
.msg-bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
}
.msg-row.user .msg-bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-row.ai .msg-bubble {
  background: #f5f5f7;
  color: #333;
  border-bottom-left-radius: 4px;
}
.msg-bubble.streaming {
  background: #f5f5f7;
  color: #333;
  border-bottom-left-radius: 4px;
}

.typing span {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #999;
  margin: 0 2px;
  animation: typing 1.4s infinite;
}
.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: translateY(0); }
  30% { opacity: 1; transform: translateY(-4px); }
}

.input-area {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}
.input-options {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
}
.opt-label {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}
.input-row {
  display: flex;
  gap: 8px;
}
.msg-input {
  flex: 1;
  border: 2px solid #e8e8ee;
  border-radius: 12px;
  padding: 10px 16px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}
.msg-input:focus { border-color: #667eea; }
.send-btn {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}
.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}
.send-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.slide-enter-active { transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1); }
.slide-leave-active { transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.slide-enter-from .sidebar-panel { transform: translateX(100%); }
.slide-enter-to .sidebar-panel { transform: translateX(0); }
.slide-leave-from .sidebar-panel { transform: translateX(0); }
.slide-leave-to .sidebar-panel { transform: translateX(100%); }
.slide-enter-from { background: transparent; }
.slide-enter-to { background: rgba(0, 0, 0, 0.25); }
.slide-leave-from { background: rgba(0, 0, 0, 0.25); }
.slide-leave-to { background: transparent; }
</style>

<style>
.msg-bubble .think-chain {
  display: block;
  background: rgba(102, 126, 234, 0.06);
  border-left: 3px solid #667eea;
  padding: 4px 10px; margin: 4px 0;
  border-radius: 0 6px 6px 0;
  font-size: 12px; color: #5568d3;
}
.msg-bubble .action-chain {
  display: block;
  background: rgba(255, 152, 0, 0.06);
  border-left: 3px solid #ff9800;
  padding: 4px 10px; margin: 4px 0;
  border-radius: 0 6px 6px 0;
  font-size: 12px; color: #e68900;
}
.msg-bubble .obs-chain {
  display: block;
  background: rgba(76, 175, 80, 0.06);
  border-left: 3px solid #4caf50;
  padding: 4px 10px; margin: 4px 0;
  border-radius: 0 6px 6px 0;
  font-size: 12px; color: #388e3c;
}
.msg-bubble code {
  background: rgba(0,0,0,0.06);
  padding: 1px 5px; border-radius: 4px;
  font-size: 12px;
}
.tool-card {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px; margin: 0 24px 8px;
  background: #fff8e1; border: 1px solid #ffe082;
  border-radius: 10px; font-size: 12px; color: #e65100;
}
.tool-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #ff9800; flex-shrink: 0;
  animation: tool-pulse 1s infinite;
}
@keyframes tool-pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
.tool-result { color: #2e7d32; margin-left: auto; }
</style>
