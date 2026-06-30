<template>
  <div style="display: flex; height: 100%">

    <!-- ===== 左栏：全部笔记标题列表 ===== -->
    <div style="background-color: whitesmoke; width: 300px; display: flex; flex-direction: column; border-right: 1px solid #ddd">

      <!-- 标题 -->
      <div style="display:flex;padding: 16px 20px; border-bottom: 1px solid #ddd">
        <span style="font-size: 16px; font-weight: bold; color: #333">全部笔记</span>
        <span style="color: #bbb; font-size: 12px; margin-left: 6px">{{ notes.length }} 条</span>
        <span
            style="display: inline-flex;border:1px solid #dddddd;border-radius:50px;padding: 4px 12px ;align-items: center; margin-left: auto; cursor: pointer; font-size: 13px; color: #666"
            @click="insertNewNote">
          <el-icon style="font-size: 18px; margin-right: 4px"><EditPen /></el-icon>
            新建
        </span>
      </div>

      <!-- 有笔记时 -->
      <div v-if="notes.length > 0" style="flex: 1; overflow-y: auto">
        <div v-for="note in notes" :key="note.id"
             style="margin: 6px 12px; padding: 12px 14px; cursor: pointer; border: 1px solid #e0e0e0; border-radius: 8px; transition: all 0.15s; display: flex; align-items: center; justify-content: space-between"
             :style="selectedNote && selectedNote.id === note.id ? { background: '#e8e8e8', borderColor: '#c0c0c0' } : { background: '#fff' }"
             @click="selectedNote = note">
          <div style="flex: 1; min-width: 0">
            <div style="font-size: 14px; color: #333; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ note.title }}</div>
            <div style="font-size: 12px; color: #bbb">{{ note.create_time || '' }}</div>
          </div>
          <el-icon style="color: #ccc; cursor: pointer; flex-shrink: 0; margin-left: 8px" @click.stop="moveToTrash(note.id)"><Delete /></el-icon>
        </div>
      </div>

      <!-- 没有笔记时 -->
      <div v-else style="flex: 1; display: flex; align-items: center; justify-content: center">
        <span style="color: #bbb; font-size: 14px">暂无笔记</span>
      </div>

    </div>

    <!-- ===== 右栏：笔记正文 ===== -->
    <div style="flex: 1; display: flex; flex-direction: column">

      <!-- 选中了笔记 → 显示正文 -->
      <div v-if="selectedNote" style="flex: 1;padding: 0 60px; display: flex; flex-direction: column">
        <div style="border-bottom: 1px solid #eee"></div>
        <el-input v-model="selectedNote.title" @blur="update" class="no-border-input" placeholder="标题" style="font-size: 22px; font-weight: bold" />
        <el-input v-model="selectedNote.content" @blur="update" type="textarea" :autosize="{minRows:10}" class="no-border-input" ></el-input>
      </div>

      <!-- 没选笔记 → 引导图 -->
      <div v-else style="flex: 1; display: flex; align-items: center; justify-content: center">
        <div style="text-align: center">
          <img src="@/assets/imgs/p1.png" alt="" style="width: 200px">
          <div style="color: #bbb; margin-top: 16px">点击左侧笔记查看内容</div>
        </div>
      </div>

    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import request from "@/utils/request.js"
import { ElMessage } from "element-plus"

const notes = ref([])
const selectedNote = ref(null)

onMounted(() => {
  request.get('/note/selectAll').then(res => {
    if (res.code === '200') {
      notes.value = res.data
    }
  })
})

const insertNewNote = () => {
  request.post('/note/insertNewNote', { title: '新建笔记', content: '' }).then(res=>{
    if(res.code==="200"){
      request.get('/note/selectAll').then(res => {
        if (res.code === '200') {
          notes.value = res.data
          selectedNote.value=notes.value[0]
        }
      })
    }
  })
}

const update = () =>{
  request.put('/note/updateNote',{title:selectedNote.value.title,id:selectedNote.value.id, content:selectedNote.value.content}).then(res=>{
    if(res.code==='200'){
      request.get('/note/selectAll').then(res=>{
        if (res.code === '200') {
          notes.value = res.data
        }
      })
    }
  })
}

const moveToTrash = (noteId) => {
  request.put('/note/moveToTrash', { id: noteId }).then(res => {
    if (res.code === '200') {
      if (selectedNote.value && selectedNote.value.id === noteId) {
        selectedNote.value = null
      }
      request.get('/note/selectAll').then(res => {
        if (res.code === '200') {
          notes.value = res.data
        }
      })
    }
  })
}

</script>

<style>

.no-border-input .el-input__wrapper,
.no-border-input .el-textarea__inner {
  box-shadow: none !important;
  border: none !important;
  background: transparent !important;
  padding: 0 !important;
  --el-input-border-color: transparent;
  --el-input-placeholder-color: #c0c4cc;
}

</style>
