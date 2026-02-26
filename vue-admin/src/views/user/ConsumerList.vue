<template>
  <div class="user-list-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="输入用户名搜索" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round @click="loadData">
            <img src="/chaxun.png" style="width: 14px; height: 14px; margin-right: 5px; vertical-align: middle;" />
            查询
          </el-button>
          <el-button round @click="resetQuery">
            <img src="/chongzhi.png" style="width: 14px; height: 14px; margin-right: 5px; vertical-align: middle;" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>消费者用户管理</span>
        </div>
      </template>
      
      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border 
        stripe 
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="150" align="center" />
        <el-table-column label="头像" width="100" align="center">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar" icon="UserFilled" />
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" align="center" />
        <el-table-column prop="email" label="邮箱" width="200" align="center" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag type="success">消费者</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'danger' : 'success'">
              {{ scope.row.status === 1 ? '已禁用' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              link 
              :type="scope.row.status === 1 ? 'success' : 'warning'" 
              :icon="scope.row.status === 1 ? 'Check' : 'Close'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :total="total"
          :page-size="query.pageSize"
          :current-page="query.pageNum"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="500px">
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" type="email" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-upload
              class="avatar-uploader"
              action="http://localhost:9090/file/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
              accept="image/jpeg,image/png"
            >
              <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="avatar-tip">支持 JPG、PNG 格式，大小不超过 2MB</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, UserFilled, Check, Close, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
  id: null as number | null,
  username: '',
  phone: '',
  email: '',
  avatar: ''
})

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  username: ''
})

const uploadHeaders = ref({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

const loadData = () => {
  loading.value = true
  request.get('/user/admin/consumer-list', { params: query }).then((res: any) => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).catch((error: any) => {
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
  }).finally(() => {
    loading.value = false
  })
}

const resetQuery = () => {
  query.username = ''
  query.pageNum = 1
  loadData()
}

const handleCurrentChange = (val: number) => {
  query.pageNum = val
  loadData()
}

const handleEdit = (row: any) => {
  editForm.id = row.id
  editForm.username = row.username
  editForm.phone = row.phone || ''
  editForm.email = row.email || ''
  editForm.avatar = row.avatar || ''
  editDialogVisible.value = true
}

const saveEdit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.put('/user/admin/update', editForm)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        loadData()
      } catch (error: any) {
        ElMessage.error('更新失败：' + (error.message || '未知错误'))
      }
    }
  })
}

const handleToggleStatus = (row: any) => {
  const newStatus = row.status === 1 ? 0 : 1
  request.put('/user/admin/status', {
    userId: row.id,
    status: newStatus
  }).then(() => {
    ElMessage.success('状态更新成功')
    loadData()
  }).catch((error: any) => {
    ElMessage.error('状态更新失败：' + (error.message || '未知错误'))
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    request.delete(`/user/admin/${row.id}`).then(() => {
      ElMessage.success('删除成功')
      loadData()
    }).catch((error: any) => {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    })
  }).catch(() => {
    // 用户取消
  })
}

const handleAvatarSuccess = (response: any) => {
  if (response && response.code === '200') {
    editForm.avatar = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('头像上传失败：' + (response?.msg || '未知错误'))
  }
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败，请检查网络或文件格式')
}

const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  uploadHeaders.value.Authorization = `Bearer ${localStorage.getItem('token') || ''}`
  return true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-list-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar {
  width: 120px;
  height: 120px;
  object-fit: cover;
  display: block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
