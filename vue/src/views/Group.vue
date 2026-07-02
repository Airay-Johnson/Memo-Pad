<template>
  <div class="group-layout">
    <div class="left-panel" style="width:300px">
      <div class="panel-header">
        <span>{{ selectedGroup ? selectedGroup.name : '分组' }}</span>
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
        <div v-if="notes.length === 0 && selectedGroup" class="empty-list">暂无笔记，点新建创建一个吧</div>
      </div>
    </div>
    <div class="right-panel">
      <template v-if="selectedNote">
        <div class="editor-header">
          <el-input v-model="selectedNote.title" class="no-border-input title-input" placeholder="标题" @input="update" />
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
      <div v-else class="empty-hint">选择一个笔记</div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { Plus, Close } from '@element-plus/icons-vue'

const route = useRoute()
const notes = ref([])
const selectedNote = ref(null)
const selectedGroup = ref(null)
const groups = ref([])

const loadGroups = () => {
  request.get('/noteGroup/selectAll').then(res => {
    if (res.code === '200') groups.value = res.data
  })
}

const selectGroup = (group) => {
  selectedGroup.value = group
  selectedNote.value = null
  request.get('/note/selectByGroupId?groupId=' + group.id).then(res => {
    if (res.code === '200') notes.value = res.data
  })
}

const selectNote = (note) => { selectedNote.value = { ...note } }

const insertNewNote = () => {
  if (!selectedGroup.value) { ElMessage.warning('请先选择一个分组'); return }
  request.post('/note/insertNewNote', { title: '新建笔记', content: '', groupId: selectedGroup.value.id }).then(res => {
    if (res.code === '200') selectGroup(selectedGroup.value)
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
      if (res.code === '200' && selectedGroup.value) selectGroup(selectedGroup.value)
    })
  }, 500)
}

const moveToTrash = (noteId) => {
  const id = noteId !== undefined ? noteId : selectedNote.value?.id
  if (!id) return
  request.put('/note/moveToTrash', { id }).then(res => {
    if (res.code === '200') {
      if (selectedNote.value && selectedNote.value.id === id) selectedNote.value = null
      ElMessage.success('已移至回收站')
      if (selectedGroup.value) selectGroup(selectedGroup.value)
    }
  })
}

const formatTime = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// 初始加载
loadGroups()

// URL 参数变化时，自动切换分组
const checkRoute = () => {
  const gid = route.query.groupId
  if (gid && groups.value.length > 0) {
    const g = groups.value.find(item => item.id === Number(gid))
    if (g) selectGroup(g)
  }
}

watch(() => route.query.groupId, () => { checkRoute() })
watch(() => groups.value.length, () => { checkRoute() })

onMounted(() => { nextTick(() => { checkRoute() }) })
</script>

<style scoped>
.group-layout { display: flex; height: 100vh; }
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
.no-border-input >>> .el-input__wrapper { box-shadow: none !important; background: transparent; }
.title-input >>> .el-input__inner { font-size: 22px; font-weight: 600; }
.content-input >>> .el-textarea__inner { font-size: 16px; line-height: 1.8; border: none; box-shadow: none; resize: none; }
.empty-hint { color: #ccc; font-size: 18px; text-align: center; margin-top: 200px; }
.empty-list { color: #bbb; text-align: center; padding: 40px 20px; font-size: 14px; }
</style>
