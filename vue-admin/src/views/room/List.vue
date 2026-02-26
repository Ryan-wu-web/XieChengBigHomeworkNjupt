<template>
  <div class="room-list-container">
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ hotelName }} - 房型管理</span>
          <el-button type="primary" link @click="addRoom">
             <img src="/fabu.png" style="width: 14px; height: 14px; margin-right: 5px; vertical-align: middle;" />
             添加房型
          </el-button>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column label="图片" width="90" align="center">
            <template #default="scope">
                <el-image v-if="scope.row.imageUrl" :src="scope.row.imageUrl" fit="cover" style="width: 60px; height: 44px; border-radius: 6px"
                    :preview-src-list="[scope.row.imageUrl]" />
                <div v-else style="width: 60px; height: 44px; border-radius: 6px; background: #f2f2f2; display:flex; align-items:center; justify-content:center; color:#999; font-size:12px;">无图</div>
            </template>
        </el-table-column>
        <el-table-column prop="name" label="房型名称" />
        <el-table-column label="标签" min-width="120">
            <template #default="scope">
                <el-tag v-for="t in (scope.row.tags ? scope.row.tags.split(',') : [])" :key="t" size="small" style="margin-right: 4px">{{ t }}</el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="areaRange" label="面积" width="110" />
        <el-table-column prop="floorRange" label="楼层" width="110" />
        <el-table-column prop="price" label="价格">
            <template #default="scope">
                ¥ {{ scope.row.price }}
            </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" />
        <el-table-column label="上架" width="90" align="center">
            <template #default="scope">
                <el-switch
                    v-model="scope.row.status"
                    :active-value="1"
                    :inactive-value="0"
                    inline-prompt
                    active-text="上架"
                    inactive-text="下架"
                    @change="toggleStatus(scope.row)"
                />
            </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="editRoom(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="deleteRoom(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑房型' : '添加房型'" width="760px">
      <el-scrollbar height="70vh">
        <el-form :model="form" label-width="110px">
            <el-form-item label="房型名称" required>
                <el-input v-model="form.name" placeholder="例如：豪华大床房" />
            </el-form-item>
            <el-form-item label="快捷标签">
                <el-select
                    v-model="tagsList"
                    multiple
                    filterable
                    allow-create
                    default-first-option
                    placeholder="如：山景、智能马桶、亲子"
                    style="width: 100%">
                    <el-option label="山景" value="山景" />
                    <el-option label="海景" value="海景" />
                    <el-option label="智能马桶" value="智能马桶" />
                    <el-option label="免费Wi-Fi" value="免费Wi-Fi" />
                    <el-option label="有窗" value="有窗" />
                    <el-option label="禁烟" value="禁烟" />
                </el-select>
            </el-form-item>
            <el-form-item label="价格" required>
                <el-input-number v-model="form.price" :min="0" :step="1" :precision="2" style="width: 260px" />
                <span style="margin-left: 8px; color:#909399;">元</span>
            </el-form-item>
            <el-form-item label="库存" required>
                <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 260px" />
                <span style="margin-left: 8px; color:#909399;">间</span>
            </el-form-item>
            <el-form-item label="上架状态">
                <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="房型图片">
                <el-upload
                    :action="uploadUrl"
                    :show-file-list="false"
                    :on-success="handleImageSuccess"
                    :before-upload="beforeUpload">
                    <img v-if="form.imageUrl" :src="form.imageUrl" class="room-img" />
                    <el-button v-else type="primary" plain>上传图片</el-button>
                </el-upload>
            </el-form-item>
            <el-form-item label="面积范围" required>
              <el-row :gutter="10" style="width: 100%">
                <el-col :span="8">
                  <el-input-number v-model="areaMin" :min="0" :step="1" style="width: 100%" />
                </el-col>
                <el-col :span="1" style="display:flex;align-items:center;justify-content:center;color:#909399;">-</el-col>
                <el-col :span="8">
                  <el-input-number v-model="areaMax" :min="0" :step="1" style="width: 100%" />
                </el-col>
                <el-col :span="3" style="display:flex;align-items:center;color:#909399;">㎡</el-col>
              </el-row>
            </el-form-item>
            <el-form-item label="楼层范围" required>
              <el-row :gutter="10" style="width: 100%">
                <el-col :span="8">
                  <el-input-number v-model="floorMin" :min="0" :step="1" style="width: 100%" />
                </el-col>
                <el-col :span="1" style="display:flex;align-items:center;justify-content:center;color:#909399;">-</el-col>
                <el-col :span="8">
                  <el-input-number v-model="floorMax" :min="0" :step="1" style="width: 100%" />
                </el-col>
                <el-col :span="3" style="display:flex;align-items:center;color:#909399;">层</el-col>
              </el-row>
            </el-form-item>
            <el-form-item label="基础配置">
                <el-checkbox v-model="wifiFreeChecked" label="Wi-Fi 免费" />
                <el-checkbox v-model="windowChecked" label="有窗" />
                <el-checkbox v-model="noSmokingChecked" label="禁烟" />
            </el-form-item>
            <el-form-item label="床信息">
                <el-row :gutter="10" style="width: 100%">
                    <el-col :span="8">
                        <el-input-number v-model="form.bedCount" :min="0" :step="1" style="width: 100%" />
                    </el-col>
                    <el-col :span="8">
                        <el-select v-model="form.bedType" placeholder="床型" style="width: 100%">
                          <el-option label="特大床" value="特大床" />
                          <el-option label="大床" value="大床" />
                          <el-option label="双床" value="双床" />
                          <el-option label="单人床" value="单人床" />
                        </el-select>
                    </el-col>
                    <el-col :span="8">
                        <el-input-number v-model="bedSize" :min="0" :step="0.01" :precision="2" style="width: 100%" />
                        <div style="font-size: 12px; color:#909399; margin-top: 4px">单位：米</div>
                    </el-col>
                </el-row>
            </el-form-item>
            <el-form-item label="可加床">
                <el-switch v-model="extraBedChecked" active-text="可加床" inactive-text="不可加床" />
            </el-form-item>
            <el-form-item label="早餐信息">
                <el-row :gutter="10" style="width: 100%">
                    <el-col :span="8">
                        <el-input-number v-model="form.breakfastCount" :min="0" :step="1" style="width: 100%" />
                    </el-col>
                    <el-col :span="8">
                        <el-input v-model="form.breakfastType" placeholder="类型(自助餐)" />
                    </el-col>
                    <el-col :span="8">
                        <el-time-picker
                          v-model="breakfastTimeRange"
                          is-range
                          value-format="HH:mm"
                          format="HH:mm"
                          range-separator="-"
                          start-placeholder="开始"
                          end-placeholder="结束"
                          style="width: 100%"
                        />
                    </el-col>
                </el-row>
            </el-form-item>
            <el-form-item label="早餐菜品">
              <el-select
                v-model="breakfastDishesList"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="如：中式、清真餐、素食"
                style="width: 100%">
                <el-option label="中式" value="中式" />
                <el-option label="清真餐" value="清真餐" />
                <el-option label="素食" value="素食" />
              </el-select>
            </el-form-item>
            <el-form-item label="早餐加价">
                <el-input-number v-model="form.breakfastExtraPrice" :min="0" :step="1" :precision="2" style="width: 260px" />
                <span style="margin-left: 8px; color:#909399;">元/人</span>
            </el-form-item>
            <el-form-item label="描述">
                <el-input v-model="form.description" type="textarea" />
            </el-form-item>
            <el-form-item label="会员权益">
                <el-input v-model="form.memberBenefits" type="textarea" :rows="2" placeholder="可填写要点或JSON" />
            </el-form-item>
            <el-form-item label="政策与服务">
                <el-input v-model="form.cancelPolicy" type="textarea" :rows="3" placeholder="取消规则等（可填写要点或JSON）" />
            </el-form-item>
        </el-form>
      </el-scrollbar>
        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="save">确定</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const hotelId = route.params.hotelId
const hotelName = ref('')
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const uploadUrl = 'http://localhost:9090/file/upload'
const tagsList = ref<string[]>([])
const wifiFreeChecked = ref(true)
const windowChecked = ref(true)
const noSmokingChecked = ref(true)
const extraBedChecked = ref(false)
const breakfastDishesList = ref<string[]>([])
const breakfastTimeRange = ref<string[]>([])
const areaMin = ref<number | null>(null)
const areaMax = ref<number | null>(null)
const floorMin = ref<number | null>(null)
const floorMax = ref<number | null>(null)
const bedSize = ref<number | null>(1.81)

const form = reactive({
    id: undefined,
    hotelId: hotelId,
    name: '',
    price: '',
    stock: '',
    description: '',
    imageUrl: '',
    status: 1,
    tags: '',
    areaRange: '',
    floorRange: '',
    wifiFree: 1,
    windowFlag: 1,
    noSmoking: 1,
    bedCount: 1,
    bedType: '特大床',
    bedSize: '1.81米',
    extraBedAllowed: 0,
    breakfastCount: 0,
    breakfastType: '',
    breakfastDishes: '',
    breakfastTime: '',
    breakfastExtraPrice: 0,
    memberBenefits: '',
    cancelPolicy: ''
})

const parseRangeNumbers = (value: string | undefined | null) => {
    if (!value) return []
    const nums = value.match(/(\d+(\.\d+)?)/g)
    if (!nums) return []
    return nums.map(n => parseFloat(n)).filter(n => !Number.isNaN(n))
}

const loadData = () => {
    loading.value = true
    // Get hotel name first
    request.get(`/hotel/detail/${hotelId}`).then((res: any) => {
        console.log('Response data:', res)
        if (res && res.data && res.data.hotel) {
            hotelName.value = res.data.hotel.name
            tableData.value = res.data.rooms || []
        } else {
            ElMessage.error('获取酒店信息失败')
        }
    }).catch((error: any) => {
        console.error('Error loading data:', error)
        ElMessage.error('加载数据失败，请重试')
    }).finally(() => loading.value = false)
}

const addRoom = () => {
    isEdit.value = false
    form.id = undefined
    form.name = ''
    form.price = ''
    form.stock = ''
    form.description = ''
    form.imageUrl = ''
    form.status = 1
    tagsList.value = []
    form.tags = ''
    form.areaRange = ''
    form.floorRange = ''
    wifiFreeChecked.value = true
    windowChecked.value = true
    noSmokingChecked.value = true
    extraBedChecked.value = false
    form.wifiFree = 1
    form.windowFlag = 1
    form.noSmoking = 1
    form.bedCount = 1
    form.bedType = '特大床'
    form.bedSize = '1.81米'
    form.extraBedAllowed = 0
    form.breakfastCount = 0
    form.breakfastType = ''
    form.breakfastDishes = ''
    form.breakfastTime = ''
    form.breakfastExtraPrice = 0
    form.memberBenefits = ''
    form.cancelPolicy = ''

    // strict inputs
    areaMin.value = null
    areaMax.value = null
    floorMin.value = null
    floorMax.value = null
    bedSize.value = 1.81
    breakfastTimeRange.value = []
    breakfastDishesList.value = []
    dialogVisible.value = true
}

const editRoom = (row: any) => {
    isEdit.value = true
    Object.assign(form, row)
    tagsList.value = row.tags ? row.tags.split(',') : []
    wifiFreeChecked.value = row.wifiFree === 1
    windowChecked.value = row.windowFlag === 1
    noSmokingChecked.value = row.noSmoking === 1
    extraBedChecked.value = row.extraBedAllowed === 1

    // strict inputs - parse formatted strings
    const areaNums = parseRangeNumbers(row.areaRange)
    areaMin.value = areaNums.length > 0 ? areaNums[0] : null
    areaMax.value = areaNums.length > 1 ? areaNums[1] : areaMin.value

    const floorNums = parseRangeNumbers(row.floorRange)
    floorMin.value = floorNums.length > 0 ? floorNums[0] : null
    floorMax.value = floorNums.length > 1 ? floorNums[1] : floorMin.value

    const bedNums = parseRangeNumbers(row.bedSize)
    bedSize.value = bedNums.length > 0 ? bedNums[0] : null

    breakfastDishesList.value = row.breakfastDishes ? row.breakfastDishes.split(',') : []
    breakfastTimeRange.value = row.breakfastTime ? String(row.breakfastTime).split('-') : []
    dialogVisible.value = true
}

const beforeUpload = (file: any) => {
    const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isJPG) ElMessage.error('上传图片只能是 JPG/PNG 格式!');
    if (!isLt10M) ElMessage.error('上传图片大小不能超过 10MB!');
    return isJPG && isLt10M;
}

const handleImageSuccess = (res: any) => {
    if(res.code === '200') {
        form.imageUrl = res.data
    } else {
        ElMessage.error('上传失败')
    }
}

const save = () => {
    if(!form.name || !form.price || !form.stock) {
        ElMessage.warning('请填写完整信息')
        return
    }
    if (areaMin.value == null || areaMax.value == null) {
        ElMessage.warning('请填写面积范围（数字）')
        return
    }
    if (floorMin.value == null || floorMax.value == null) {
        ElMessage.warning('请填写楼层范围（数字）')
        return
    }
    if (bedSize.value == null) {
        ElMessage.warning('请填写床尺寸（数字，单位：米）')
        return
    }
    form.tags = tagsList.value.join(',')
    form.wifiFree = wifiFreeChecked.value ? 1 : 0
    form.windowFlag = windowChecked.value ? 1 : 0
    form.noSmoking = noSmokingChecked.value ? 1 : 0
    form.extraBedAllowed = extraBedChecked.value ? 1 : 0

    // format strict values into backend string fields
    const a1 = Math.min(areaMin.value, areaMax.value)
    const a2 = Math.max(areaMin.value, areaMax.value)
    form.areaRange = `${a1}-${a2}㎡`

    const f1 = Math.min(floorMin.value, floorMax.value)
    const f2 = Math.max(floorMin.value, floorMax.value)
    form.floorRange = `${f1}-${f2}层`

    form.bedSize = `${bedSize.value}米`

    form.breakfastDishes = breakfastDishesList.value.join(',')
    form.breakfastTime = breakfastTimeRange.value && breakfastTimeRange.value.length === 2 ? `${breakfastTimeRange.value[0]}-${breakfastTimeRange.value[1]}` : ''

    request.post('/room/save', form).then((res: any) => {
        console.log('Save room response:', res)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        loadData()
    }).catch((error: any) => {
        console.error('Error saving room:', error)
        ElMessage.error('保存失败，请重试')
    })
}

const toggleStatus = (row: any) => {
    request.post('/room/save', row).then((res: any) => {
        console.log('Toggle status response:', res)
        ElMessage.success('已更新上架状态')
    }).catch((error: any) => {
        console.error('Error toggling status:', error)
        ElMessage.error('更新状态失败，请重试')
    })
}

const deleteRoom = (id: number) => {
    request.delete(`/room/${id}`).then((res: any) => {
        console.log('Delete room response:', res)
        ElMessage.success('删除成功')
        loadData()
    }).catch((error: any) => {
        console.error('Error deleting room:', error)
        ElMessage.error('删除失败，请重试')
    })
}

onMounted(() => {
    if(hotelId) {
        loadData()
    }
})
</script>

<style scoped>
.room-list-container {
    padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.room-img{
  width: 140px;
  height: 90px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
}
</style>
