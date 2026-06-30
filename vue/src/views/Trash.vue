<template>
<div style="display: flex; height: 100%">

  <!--目录区域-->
  <div style="background-color: whitesmoke; width: 300px; display: flex; flex-direction: column">
    <div style="display: flex; height: 60px; border-bottom: 1px solid #ddd; align-items: center">
      <span style="font-size: 15px; font-weight: bold; padding-left: 20px;">回收站</span>
    </div>

    <!-- 有回收项时 -->
    <div v-if="notes.length > 0" style="flex: 1; overflow-y: auto">
      <div v-for="note in notes" :key="note.id"
           style="margin: 6px 12px; padding: 12px 14px; cursor: pointer; border: 1px solid #e0e0e0; border-radius: 8px; transition: all 0.15s; display: flex; align-items: center; justify-content: space-between"
           :style="selectedNote && selectedNote.id === note.id ? { background: '#e8e8e8', borderColor: '#c0c0c0' } : { background: '#fff' }"
           @click="selectedNote = note">
        <div style="flex: 1; min-width: 0">
          <div style="font-size: 14px; color: #333; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ note.title }}</div>
          <div style="font-size: 12px; color: #bbb">{{ note.update_time || '' }}</div>
        </div>
        <el-icon style="color: #4eb88c; cursor: pointer; flex-shrink: 0; margin-left: 8px" @click.stop="restoreNote(note.id)"><RefreshLeft /></el-icon>
      </div>
    </div>

    <!-- 没回收项时 -->
    <div v-else style="flex: 1; display: flex; align-items: center; justify-content: center">
      <span style="font-size: 14px; color: #bbb">回收站为空</span>
    </div>
  </div>

  <!--内容区域：有选中项时 -->
  <div v-if="selectedNote" style="flex: 1; padding: 40px 60px; overflow-y: auto">
    <div style="font-size: 22px; font-weight: bold; color: #333; margin-bottom: 8px">{{ selectedNote.title }}</div>
    <div style="color: #999; font-size: 13px; margin-bottom: 20px">删除时间：{{ selectedNote.delete_time || selectedNote.update_time || '未知' }}</div>
    <div style="border-bottom: 1px solid #eee; margin-bottom: 20px"></div>
    <div style="line-height: 1.8; color: #555; white-space: pre-wrap">{{ selectedNote.content }}</div>
    <div style="margin-top: 24px">
      <el-button type="danger" @click="deleteForever(selectedNote.id)" :disabled="!selectedNote">彻底删除</el-button>
    </div>
  </div>

  <!--内容区域：没选中项时 -->
  <div v-else style="flex: 1; display: flex; align-items: center; justify-content: center">
    <div style="text-align: center">
      <img src="@/assets/imgs/p2.png" alt="" style="width: 200px">
      <div style="color: #bbb; margin-top: 16px">点滴文字，高效生活</div>
    </div>
  </div>

</div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import request from "@/utils/request.js"
import { ElMessage, ElMessageBox } from "element-plus"

const notes = ref([])
const selectedNote = ref(null)

onMounted(() => {
  loadTrash()
})

const loadTrash = () => {
  request.get('/note/selectTrash').then(res => {
    if (res.code === '200') {
      notes.value = res.data
    }
  })
}

const restoreNote = (noteId) => {
  request.put('/note/restoreNote', { id: noteId }).then(res => {
    if (res.code === '200') {
      ElMessage.success('已恢复')
      if (selectedNote.value && selectedNote.value.id === noteId) {
        selectedNote.value = null
      }
      loadTrash()
    }
  })
}

const deleteForever = (noteId) => {
  ElMessageBox.confirm('确定要彻底删除这条笔记吗？此操作不可撤销。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete('/note/deleteForever', { data: { id: noteId } }).then(res => {
      if (res.code === '200') {
        ElMessage.success('已彻底删除')
        if (selectedNote.value && selectedNote.value.id === noteId) {
          selectedNote.value = null
        }
        loadTrash()
      }
    })
  }).catch(() => {})
}
</script>
