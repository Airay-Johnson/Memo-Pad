<template>
  <div class="trash-layout">
    <div class="left-panel" style="width:300px">
      <div class="panel-header"><span>回收站</span></div>
      <div class="note-list">
        <div
          v-for="note in notes"
          :key="note.id"
          class="note-card"
          :class="{ active: selectedNote && selectedNote.id === note.id }"
          @click="selectNote(note)"
        >
          <div class="card-title">{{ note.title || '无标题' }}</div>
          <div class="card-meta">删除于 {{ formatTime(note.deleteTime) }}</div>
          <el-icon class="restore-icon" @click.stop="restoreNote(note.id)"><RefreshLeft /></el-icon>
        </div>
        <div v-if="notes.length === 0" class="empty-list">回收站空空如也</div>
      </div>
    </div>
    <div class="right-panel">
      <template v-if="selectedNote">
        <div class="detail-header">
          <div class="detail-meta">
            <span>删除时间：{{ formatFull(selectedNote.deleteTime) }}</span>
          </div>
          <div class="detail-actions">
            <el-button type="primary" size="small" @click="restoreNote(selectedNote.id)">恢复</el-button>
            <el-button type="danger" size="small" @click="deleteForever(selectedNote.id)">彻底删除</el-button>
          </div>
        </div>
        <h3 class="detail-title">{{ selectedNote.title }}</h3>
        <div class="detail-content">{{ selectedNote.content }}</div>
      </template>
      <div v-else class="empty-hint">选择查看已删除的笔记</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft } from '@element-plus/icons-vue'

const notes = ref([])
const selectedNote = ref(null)

const loadTrash = () => {
  request.get('/note/selectTrash').then(res => {
    if (res.code === '200') notes.value = res.data
  })
}

const selectNote = (note) => { selectedNote.value = { ...note } }

const restoreNote = (noteId) => {
  request.put('/note/restoreNote', { id: noteId }).then(res => {
    if (res.code === '200') {
      ElMessage.success('已恢复')
      if (selectedNote.value && selectedNote.value.id === noteId) selectedNote.value = null
      loadTrash()
    }
  })
}

const deleteForever = (noteId) => {
  ElMessageBox.confirm('确定要永久删除吗？不可恢复！', '警告', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    request.delete('/note/deleteForever?id=' + noteId).then(res => {
      if (res.code === '200') {
        ElMessage.success('已永久删除')
        if (selectedNote.value && selectedNote.value.id === noteId) selectedNote.value = null
        loadTrash()
      }
    })
  }).catch(() => {})
}

const formatTime = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}
const formatFull = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(loadTrash)
</script>

<style scoped>
.trash-layout { display: flex; height: 100vh; }
.left-panel { background: #fafafa; border-right: 1px solid #eee; display: flex; flex-direction: column; }
.panel-header { padding: 16px 12px; border-bottom: 1px solid #eee; font-weight: 600; }
.note-list { flex: 1; overflow-y: auto; }
.note-card {
  position: relative; margin: 6px 12px; padding: 12px 14px;
  border: 1px solid #e0e0e0; border-radius: 8px; cursor: pointer; transition: all 0.2s;
}
.note-card:hover { border-color: #667eea; box-shadow: 0 2px 8px rgba(102,126,234,0.15); }
.note-card.active { border-color: #667eea; background: #f0f2ff; }
.card-title { font-size: 14px; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding-right: 24px; }
.card-meta { font-size: 12px; color: #999; margin-top: 4px; }
.restore-icon { position: absolute; top: 12px; right: 10px; font-size: 14px; color: #67c23a; cursor: pointer; }
.restore-icon:hover { color: #529b2e; }
.right-panel { flex: 1; padding: 20px 30px; overflow-y: auto; }
.detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #eee; }
.detail-meta { font-size: 13px; color: #999; }
.detail-actions { display: flex; gap: 8px; }
.detail-title { font-size: 20px; margin-bottom: 16px; }
.detail-content { font-size: 15px; line-height: 1.8; color: #555; white-space: pre-wrap; }
.empty-hint { color: #ccc; font-size: 18px; text-align: center; margin-top: 200px; }
.empty-list { color: #bbb; text-align: center; padding: 40px 20px; font-size: 14px; }
</style>
