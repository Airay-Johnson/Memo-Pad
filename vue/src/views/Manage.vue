<template>
<div style="display: flex;">
  <!--左侧区域开始-->
  <div style=" display:block;">

    <!--logo区域-->
    <div style="width: 180px;height:60px;display: flex; align-items: center;border-right: black; padding-left: 20px;background-color:whitesmoke">
      <el-dropdown @command="handleDropdown">
        <img style="width:40px;height:40px;border-radius: 50%;" src="@/assets/imgs/头像.jpg" alt="">
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="password">修改密码</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <div style="margin-left: 5px;">
        <span style="font-size: 20px; font-weight: bold; color: black; display: block;">{{ user.name || '潇潇' }}</span>
        <span style="font-size: 10px;  color: rgba(0,0,0,0.29); display: block;">个人账户</span>
      </div>
    </div>

    <!--菜单区域-->
    <div style="display:flex; width: 180px; border-right: 1px solid #ddd; min-height: calc(100vh - 60px); background-color: #ffffff">
      <div style="width: 180px">
        <el-menu
            class="el-menu-vertical-demo"
            router
            default-active="/home"
            padding-right: 40px;
        >
          <el-menu-item index="/home">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-sub-menu index="1">
            <template #title>
              <el-icon><MessageBox /></el-icon>
              <span>分组</span>
              <el-icon class="submenu-new-group-btn" style="cursor: pointer; margin-left: 8px" title="新建分组"><CirclePlus /></el-icon>
            </template>

            <!-- 新建分组 -->
            <el-menu-item v-if="isAddingGroup" style="cursor: default; padding: 4px 12px" @click.stop>
              <el-icon><Files /></el-icon>
              <el-input
                  ref="newGroupInput"
                  v-model="newGroupName"
                  placeholder=" "
                  size="small"
                  @keyup.enter="confirmCreateGroup"
                  @blur="confirmCreateGroup"
              />
            </el-menu-item>

            <!-- 已有分组 -->
            <el-menu-item v-for="group in groups" :key="group.id" :index="'/group?groupId=' + group.id" style="padding-left: 12px; display: flex; align-items: center; justify-content: space-between">
              <div style="display: flex; align-items: center; flex: 1; min-width: 0">
                <el-icon style="font-size: 14px; margin-right: 4px; flex-shrink: 0"><Files /></el-icon>
                <span style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ group.name }}</span>
              </div>
              <el-icon class="delete-group-btn" style="font-size: 14px; color: #ccc; cursor: pointer; flex-shrink: 0; margin-left: 4px" title="删除分组" @click.stop="(e) => { e.stopPropagation(); e.preventDefault(); deleteGroup(group.id, group.name) }"><Close /></el-icon>
            </el-menu-item>

            <!-- 暂无分组占位 -->
            <el-menu-item v-if="!isAddingGroup && groups.length === 0" index="/group">
              <span style="color: #bbb">暂无分组</span>
            </el-menu-item>



          </el-sub-menu>

          <el-menu-item index="/trash">
            <el-icon><DeleteFilled /></el-icon>
            <span>回收站</span>
          </el-menu-item>
        </el-menu>
      </div>
    </div>

  </div>

  <!--内容区域-->
  <div style="flex: 1;">
    <RouterView />
  </div>

  <!-- 个人信息弹窗 -->
  <el-dialog v-model="showProfileDialog" title="个人信息" width="400px">
    <el-input v-model="profileForm.name" placeholder="昵称" style="margin-bottom: 16px" />
    <template #footer>
      <el-button @click="showProfileDialog = false">取消</el-button>
      <el-button type="primary" @click="updateProfile">保存</el-button>
    </template>
  </el-dialog>

  <!-- 修改密码弹窗 -->
  <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
    <el-input v-model="passwordForm.newPassword" placeholder="新密码" type="password" style="margin-bottom: 16px" />
    <el-input v-model="passwordForm.confirmPassword" placeholder="确认密码" type="password" style="margin-bottom: 16px" />
    <template #footer>
      <el-button @click="showPasswordDialog = false">取消</el-button>
      <el-button type="primary" @click="updatePassword">确认修改</el-button>
    </template>
  </el-dialog>

</div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue"
import { useRouter } from "vue-router"
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";

const router = useRouter()
const user = ref({})

const groups = ref([])
const selectedGroup = ref(null)
const isAddingGroup = ref(false)
const newGroupName = ref('')
const newGroupInput = ref(null)

onMounted(() => {
  // 从 localStorage 读取用户信息
  const stored = localStorage.getItem('user')
  if (stored) {
    try { user.value = JSON.parse(stored) } catch(e) {}
  }

  request.get('/noteGroup/selectAll').then(res => {
    if (res.code === '200') {
      groups.value = res.data
    }
  })

  // 原生 DOM 拦截：点 + 图标展开输入框
  document.addEventListener('click', (e) => {
    const target = e.target.closest('.submenu-new-group-btn')
    if (target) {
      e.stopPropagation()
      e.preventDefault()
      isAddingGroup.value = true
      newGroupName.value = ''
      nextTick(() => {
        // 输入框出现后自动聚焦
        const input = document.querySelector('.submenu-new-group-input .el-input__inner')
        if (input) input.focus()
      })
    }
  }, true)
})

const confirmCreateGroup = () => {
  const name = newGroupName.value.trim()
  if (!name) {
    // 空内容：取消编辑状态
    isAddingGroup.value = false
    return
  }
  request.post('/noteGroup/insertGroup', { name }).then(res => {
    if (res.code === '200') {
      newGroupName.value = ''
      isAddingGroup.value = false
      request.get('/noteGroup/selectAll').then(res => {
        if (res.code === '200') {
          groups.value = res.data
          selectedGroup.value = groups.value[0]
          ElMessage.success('创建成功')
        }
      })
    } else {
      ElMessage.error(res.msg || '创建失败')
    }
  })
}

const deleteGroup = (groupId, groupName) => {
  if (!confirm('确定删除分组 "' + groupName + '" 吗？该分组下的笔记不会被删除。')) return
  request.delete('/noteGroup/deleteGroup', { data: { id: groupId } }).then(res => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      request.get('/noteGroup/selectAll').then(res => {
        if (res.code === '200') {
          groups.value = res.data
        }
      })
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  })
}

const handleDropdown = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('user')
    router.push('/login')
    ElMessage.success('已退出登录')
  } else if (command === 'profile') {
    showProfileDialog.value = true
    profileForm.value = { name: user.value.name || '', avatar: user.value.avatar || '' }
  } else if (command === 'password') {
    showPasswordDialog.value = true
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  }
}

// ===== 个人信息弹窗 =====
const showProfileDialog = ref(false)
const profileForm = ref({ name: '', avatar: '' })

const updateProfile = () => {
  request.put('/admin/updateProfile', { id: user.value.id, name: profileForm.value.name, avatar: profileForm.value.avatar }).then(res => {
    if (res.code === '200') {
      user.value.name = profileForm.value.name
      user.value.avatar = profileForm.value.avatar
      localStorage.setItem('user', JSON.stringify(user.value))
      showProfileDialog.value = false
      ElMessage.success('修改成功')
    }
  })
}

// ===== 修改密码弹窗 =====
const showPasswordDialog = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const updatePassword = () => {
  if (!passwordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  request.put('/admin/updatePassword', { id: user.value.id, password: passwordForm.value.newPassword }).then(res => {
    if (res.code === '200') {
      showPasswordDialog.value = false
      ElMessage.success('密码修改成功，请重新登录')
      localStorage.removeItem('user')
      router.push('/login')
    }
  })
}
</script>

<style>
.el-menu > .el-menu-item:hover {
  background-color: #bbbbbb;
  color: #333;
  border-radius: 16px;
}

.el-menu > .el-menu-item.is-active {
  color: #333 !important;
  background-color: #d5d5d5;
  font-weight: bold;
  border-radius: 16px;
}

.el-sub-menu .el-menu-item:hover {
  background-color: #e8e8e8;
  color: #333;
  border-radius: 16px;
}

.el-sub-menu .el-menu-item.is-active {
  color: #333 !important;
  background-color: #d5d5d5;
  font-weight: bold;
  border-radius: 16px;
}

.el-sub-menu__title:hover {
  background-color: #f5f5f5;
  color: #333;
  border-radius: 16px;
}

.el-dropdown-menu__item:hover{
  background-color: #bbbbbb !important;
  color: black !important;
  border-radius: 16px;
}
</style>
