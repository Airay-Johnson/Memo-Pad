<template>
  <el-dialog v-model="visible" title="RAG 知识库" width="550px" :close-on-click-modal="false">
    <!-- 上传 -->
    <div style="margin-bottom: 20px;">
      <div style="font-weight: 500; margin-bottom: 8px;">上传文档到知识库</div>
      <el-input v-model="fileName" placeholder="文档名称" style="margin-bottom: 8px;" />
      <el-input v-model="docContent" type="textarea" :rows="8"
        placeholder="粘贴文档内容（支持纯文本、Markdown）" />
      <el-button type="primary" style="margin-top: 8px;" :loading="uploading" @click="uploadDoc">
        存入知识库
      </el-button>
    </div>

    <el-divider />

    <!-- 搜索 -->
    <div>
      <div style="font-weight: 500; margin-bottom: 8px;">知识库搜索</div>
      <div style="display: flex; gap: 8px;">
        <el-input v-model="searchQuery" placeholder="输入问题，从知识库检索相关内容" @keyup.enter="searchDocs" />
        <el-button type="primary" :loading="searching" @click="searchDocs">搜索</el-button>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searchResults.length > 0" style="margin-top: 16px;">
      <div style="font-weight: 500; margin-bottom: 8px;">检索结果（Top {{ searchResults.length }}）：</div>
      <div v-for="(item, i) in searchResults" :key="i"
        style="padding: 10px; margin-bottom: 8px; background: #f8f9ff; border-radius: 6px; font-size: 13px; line-height: 1.6;">
        <div style="color: #999; font-size: 12px; margin-bottom: 4px;">来源分数: {{ (item.score || 0).toFixed(3) }}</div>
        {{ item.text || item }}
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const fileName = ref('')
const docContent = ref('')
const searchQuery = ref('')
const searchResults = ref([])
const uploading = ref(false)
const searching = ref(false)

watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })

const uploadDoc = () => {
  const content = docContent.value.trim()
  if (!content) { ElMessage.warning('请粘贴文档内容'); return }
  uploading.value = true
  request.post('/ai/rag/upload', {
    content,
    fileName: fileName.value || '未命名文档'
  }).then(res => {
    uploading.value = false
    if (res.code === '200') {
      ElMessage.success(res.data)
      docContent.value = ''
      fileName.value = ''
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  }).catch(err => {
    uploading.value = false
    ElMessage.error('请求失败: ' + (err.message || '网络错误'))
  })
}

const searchDocs = () => {
  const q = searchQuery.value.trim()
  if (!q) { ElMessage.warning('请输入搜索内容'); return }
  searching.value = true
  searchResults.value = []

  request.get('/ai/rag/search', { params: { query: q } })
    .then(res => {
      searching.value = false
      if (res.code === '200') {
        searchResults.value = Array.isArray(res.data) ? res.data : []
        if (searchResults.value.length === 0) {
          ElMessage.info('未找到相关内容')
        }
      } else {
        ElMessage.error(res.msg || '搜索失败')
      }
    })
    .catch(err => {
      searching.value = false
      ElMessage.error('请求失败: ' + (err.message || '网络错误'))
    })
}
</script>
