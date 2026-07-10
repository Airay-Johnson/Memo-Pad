<template>
  <div class="fab-wrapper" @click="$emit('toggle')">
    <div class="fab-btn" :class="{ active: visible }">
      <el-icon :size="20"><ChatDotRound /></el-icon>
    </div>
    <div class="fab-ring"></div>
  </div>
</template>

<script setup>
import { ChatDotRound } from '@element-plus/icons-vue'
defineProps({ visible: Boolean })
defineEmits(['toggle'])
</script>

<style scoped>
.fab-wrapper {
  position: fixed;
  z-index: 9990;
  right: 28px;
  bottom: 28px;
  cursor: pointer;
}
.fab-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
  color: #fff;
}
.fab-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 28px rgba(102, 126, 234, 0.55);
}
.fab-btn.active {
  background: #f5f5f5;
  color: #666;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}
.fab-ring {
  position: absolute;
  top: -5px;
  left: -5px;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  border: 2px solid rgba(102, 126, 234, 0.25);
  animation: pulse 2.5s ease-out infinite;
  z-index: 1;
}
@keyframes pulse {
  0% { transform: scale(0.85); opacity: 1; }
  100% { transform: scale(1.45); opacity: 0; }
}
</style>
