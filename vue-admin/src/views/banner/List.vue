<template>
  <div class="banner-list-container">
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>广告轮播图管理</span>
          <el-button type="primary" round @click="handleAdd">
            <img src="/fabu.png" style="width: 14px; height: 14px; margin-right: 5px; vertical-align: middle;" />
            添加Banner
          </el-button>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="Banner图片" width="200" align="center">
          <template #default="scope">
            <el-image 
              :src="scope.row.imageUrl" 
              style="width: 180px; height: 100px; border-radius: 4px;"
              fit="cover"
              :preview-src-list="[scope.row.imageUrl]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="150" align="center" />
        <el-table-column label="关联酒店" width="200" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.hotelName" type="primary">{{ scope.row.hotelName }}</el-tag>
            <span v-else style="color: #909399;">酒店ID: {{ scope.row.hotelId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" sortable />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑Banner' : '添加Banner'" 
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="Banner标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入Banner标题（可选）" />
        </el-form-item>
        <el-form-item label="Banner图片" prop="imageUrl" required>
          <el-upload
            class="banner-uploader"
            action="http://localhost:9090/file/upload"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
            accept="image/jpeg,image/png"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="banner-image" />
            <el-icon v-else class="banner-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸 750x300，支持 JPG、PNG 格式，大小不超过 10MB</div>
        </el-form-item>
        <el-form-item label="关联酒店" prop="hotelId" required>
          <el-select
            v-model="form.hotelId"
            filterable
            remote
            :remote-method="searchHotels"
            :loading="hotelSearchLoading"
            placeholder="请搜索并选择酒店"
            style="width: 100%"
          >
            <el-option
              v-for="hotel in hotelOptions"
              :key="hotel.id"
              :label="`${hotel.name} (${hotel.city})`"
              :value="hotel.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number 
            v-model="form.sortOrder" 
            :min="0" 
            :max="999" 
            placeholder="数字越小越靠前，默认0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
          />
          <span style="margin-left: 10px; color: #909399;">
            {{ form.status === 1 ? '启用' : '禁用' }}
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveBanner">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const hotelSearchLoading = ref(false)
const hotelOptions = ref<any[]>([])

const form = reactive({
  id: undefined as number | undefined,
  title: '',
  imageUrl: '',
  hotelId: undefined as number | undefined,
  sortOrder: 0,
  status: 1
})

const rules = reactive<FormRules>({
  imageUrl: [
    { required: true, message: '请上传Banner图片', trigger: 'blur' }
  ],
  hotelId: [
    { required: true, message: '请选择关联酒店', trigger: 'change' }
  ]
})

// 上传请求头
const uploadHeaders = ref({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
})

const loadData = () => {
  loading.value = true
  request.get('/banner/admin/list').then((res: any) => {
    tableData.value = res.data || []
    // 加载酒店名称
    loadHotelNames()
  }).catch((error: any) => {
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
  }).finally(() => {
    loading.value = false
  })
}

const loadHotelNames = async () => {
  try {
    const hotels = await request.get('/hotel/admin/list', { params: { pageNum: 1, pageSize: 1000 } })
    const hotelMap = new Map<number, string>()
    ;(hotels.data?.records || []).forEach((hotel: any) => {
      hotelMap.set(hotel.id, hotel.name)
    })
    tableData.value.forEach((banner: any) => {
      banner.hotelName = hotelMap.get(banner.hotelId)
    })
  } catch (error) {
    console.error('加载酒店名称失败', error)
  }
}

const searchHotels = async (query: string) => {
  if (!query) {
    // 如果没有输入，加载已发布的酒店列表
    hotelSearchLoading.value = true
    try {
      const res = await request.get('/hotel/admin/list', {
        params: { pageNum: 1, pageSize: 50, status: 1 }
      })
      hotelOptions.value = res.data?.records || []
    } catch (error: any) {
      console.error('加载酒店列表失败', error)
    } finally {
      hotelSearchLoading.value = false
    }
    return
  }
  hotelSearchLoading.value = true
  try {
    const res = await request.get('/hotel/admin/list', {
      params: { pageNum: 1, pageSize: 20, name: query }
    })
    hotelOptions.value = res.data?.records || []
  } catch (error: any) {
    ElMessage.error('搜索酒店失败：' + (error.message || '未知错误'))
  } finally {
    hotelSearchLoading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.id = undefined
  form.title = ''
  form.imageUrl = ''
  form.hotelId = undefined
  form.sortOrder = 0
  form.status = 1
  hotelOptions.value = []
  dialogVisible.value = true
  // 加载已发布的酒店列表供选择
  nextTick(() => {
    searchHotels('')
  })
}

const handleEdit = (row: any) => {
  isEdit.value = true
  form.id = row.id
  form.title = row.title || ''
  form.imageUrl = row.imageUrl
  form.hotelId = row.hotelId
  form.sortOrder = row.sortOrder || 0
  form.status = row.status
  
  // 加载当前酒店信息
  searchHotels('')
  request.get('/hotel/admin/list', { params: { pageNum: 1, pageSize: 1000 } }).then((res: any) => {
    const hotel = (res.data?.records || []).find((h: any) => h.id === row.hotelId)
    if (hotel) {
      hotelOptions.value = [hotel]
    }
  })
  
  dialogVisible.value = true
}

const saveBanner = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await request.put('/banner/admin/update', form)
          ElMessage.success('更新成功')
        } else {
          await request.post('/banner/admin/add', form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error: any) {
        ElMessage.error('保存失败：' + (error.message || '未知错误'))
      }
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除Banner "${row.title || 'ID: ' + row.id}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    request.delete(`/banner/admin/${row.id}`).then(() => {
      ElMessage.success('删除成功')
      loadData()
    }).catch((error: any) => {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    })
  }).catch(() => {
    // 用户取消
  })
}

const handleStatusChange = (row: any) => {
  request.put('/banner/admin/update', {
    id: row.id,
    status: row.status
  }).then(() => {
    ElMessage.success('状态更新成功')
  }).catch((error: any) => {
    ElMessage.error('状态更新失败：' + (error.message || '未知错误'))
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  })
}

const handleImageSuccess = (response: any) => {
  if (response && response.code === '200') {
    form.imageUrl = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败：' + (response?.msg || '未知错误'))
  }
}

const beforeImageUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isJPG) {
    ElMessage.error('图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
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
.banner-list-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.banner-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-uploader:hover {
  border-color: #409EFF;
}

.banner-uploader :deep(.el-upload) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.banner-uploader-icon {
  font-size: 48px;
  color: #8c939d;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
