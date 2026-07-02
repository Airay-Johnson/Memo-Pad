<template>
  <div class="home-layout">
    <div class="left-panel" style="width:300px">
      <div class="panel-header">
        <span>笔记列表</span>
        <el-button type="primary" size="small" :icon="Plus" @click="insertNewNote">新建</el-button>
      </div>
      <div class="note-list">
        <div
          v-for="note in notes"
          :key="note.id"
          class="note-card"
          :class="{ active: selectedNote && selectedNote.id === note.id }"
          @click="selectNote(note)"
        >
          <div class="card-title">{{ note.title || '无标题' }}</div>
          <div class="card-meta">{{ formatTime(note.updateTime) }}</div>
          <el-icon class="delete-icon" @click.stop="moveToTrash(note.id)"><Close /></el-icon>
        </div>
      </div>
    </div>
    <div class="right-panel">
      <template v-if="selectedNote">
        <div class="editor-header">
          <el-input v-model="selectedNote.title" class="no-border-input title-input" placeholder="标题" @input="update" />
          <!-- ★ AI 助手按钮 -->
          <el-button type="primary" size="small" @click="showAiDialog = true" :icon="MagicStick">
            AI 助手
          </el-button>
          <el-icon class="trash-icon" @click="moveToTrash(selectedNote.id)"><Delete /></el-icon>
        </div>
        <el-input
          v-model="selectedNote.content"
          type="textarea"
          class="no-border-input content-input"
          :autosize="{ minRows: 20 }"
          placeholder="开始写点什么..."
          @input="update"
        />
      </template>
      <div v-else class="empty-hint">选择或新建一个笔记</div>
    </div>

    <!-- ★ AI 助手弹窗 -->
    <AiAssistant
      v-model="showAiDialog"
      :content="selectedNote?.content"
      @replace="onAiReplace"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { Plus, Close, Delete, MagicStick } from '@element-plus/icons-vue'
import AiAssistant from '@/components/AiAssistant.vue'

const notes = ref([])
const selectedNote = ref(null)
const showAiDialog = ref(false)

const loadNotes = () => {
  request.get('/note/selectAll').then(res => {
    if (res.code === '200') notes.value = res.data
  })
}

const selectNote = (note) => { selectedNote.value = { ...note } }

const insertNewNote = () => {
  request.post('/note/insertNewNote', { title: '新建笔记', content: '' }).then(res => {
    if (res.code === '200') loadNotes()
  })
}

let updateTimer = null
const update = () => {
  if (!selectedNote.value) return
  clearTimeout(updateTimer)
  updateTimer = setTimeout(() => {
    request.put('/note/updateNote', {
      id: selectedNote.value.id,
      title: selectedNote.value.title,
      content: selectedNote.value.content
    }).then(res => {
      if (res.code === '200') loadNotes()
    })
  }, 500)
}

const moveToTrash = (noteId) => {
  request.put('/note/moveToTrash', { id: noteId }).then(res => {
    if (res.code === '200') {
      if (selectedNote.value && selectedNote.value.id === noteId) selectedNote.value = null
      ElMessage.success('已移至回收站')
      loadNotes()
    }
  })
}

// ★ AI 替换回调：把 AI 结果写入笔记内容
const onAiReplace = (newContent) => {
  if (selectedNote.value) {
    selectedNote.value.content = newContent
    update() // 自动保存
  }
}

const formatTime = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(loadNotes)
</script>

<style scoped>
.home-layout { display: flex; height: 100vh; }
.left-panel { background: #fafafa; border-right: 1px solid #eee; display: flex; flex-direction: column; }
.panel-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 12px; border-bottom: 1px solid #eee; font-weight: 600; }
.note-list { flex: 1; overflow-y: auto; }
.note-card {
  position: relative; margin: 6px 12px; padding: 12px 14px;
  border: 1px solid #e0e0e0; border-radius: 8px; cursor: pointer; transition: all 0.2s;
}
.note-card:hover { border-color: #667eea; box-shadow: 0 2px 8px rgba(102,126,234,0.15); }
.note-card.active { border-color: #667eea; background: #f0f2ff; }
.card-title { font-size: 14px; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding-right: 20px; }
.card-meta { font-size: 12px; color: #999; margin-top: 4px; }
.delete-icon { position: absolute; top: 12px; right: 10px; font-size: 14px; color: #ccc; cursor: pointer; }
.delete-icon:hover { color: #f56c6c; }
.right-panel { flex: 1; padding: 20px 30px; overflow-y: auto; }
.editor-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.trash-icon { font-size: 18px; color: #aaa; cursor: pointer; }
.trash-icon:hover { color: #f56c6c; }
.no-border-input :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent; }
.title-input :deep(.el-input__inner) { font-size: 22px; font-weight: 600; }
.content-input :deep(.el-textarea__inner) { font-size: 16px; line-height: 1.8; border: none; box-shadow: none; resize: none; }
.empty-hint { color: #ccc; font-size: 18px; text-align: center; margin-top: 200px; }
</style>
