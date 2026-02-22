<template>
  <div class="hotel-detail-container">
    <el-page-header @back="$router.back()" :content="isEdit ? '编辑酒店信息' : '发布新酒店'" class="page-header" />
    
    <div class="content-wrapper">
       <el-row :gutter="20">
          <el-col :span="24">
             <el-card shadow="never" class="section-card">
                <template #header>
                   <div class="card-header">
                      <span>基础信息</span>
                   </div>
                </template>
                <el-form :model="form" label-width="100px" status-icon>
                    <el-row>
                        <el-col :span="12">
                            <el-form-item label="酒店名称" required>
                                <el-input v-model="form.name" placeholder="请输入酒店名称" />
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="英文名称">
                                <el-input v-model="form.nameEn" placeholder="请输入酒店英文名称" />
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="12">
                             <el-form-item label="星级" required>
                                <el-select v-model="form.starRating" placeholder="请选择星级" style="width: 100%">
                                    <el-option label="1星" value="1星" />
                                    <el-option label="2星" value="2星" />
                                    <el-option label="3星" value="3星" />
                                    <el-option label="4星" value="4星" />
                                    <el-option label="5星" value="5星" />
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                             <el-form-item label="开业日期">
                                <el-date-picker v-model="form.openDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-form-item label="酒店位置" required>
                        <div class="location-selector">
                            <el-input 
                                v-model="locationSearchText" 
                                placeholder="搜索地点（如：北京市朝阳区建国门外大街1号）"
                                clearable
                                @keyup.enter="searchLocation"
                                style="margin-bottom: 10px;"
                            >
                                <template #append>
                                    <el-button @click="searchLocation" :icon="Search">搜索</el-button>
                                </template>
                            </el-input>
                            <div id="map-container" style="width: 100%; height: 400px; border: 1px solid #dcdfe6; border-radius: 4px;"></div>
                            <div class="location-info" v-if="form.latitude && form.longitude">
                                <el-tag type="success" style="margin-top: 10px;">
                                    <el-icon><Location /></el-icon>
                                    已选择位置：{{ form.address }} ({{ form.city }})
                                </el-tag>
                                <div style="margin-top: 5px; font-size: 12px; color: #909399;">
                                    经纬度：{{ form.latitude }}, {{ form.longitude }}
                                </div>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item label="酒店描述">
                        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入酒店特色描述" />
                    </el-form-item>
                    <el-form-item label="周边环境">
                        <el-input v-model="form.surroundings" type="textarea" :rows="3" placeholder="请输入周边景点、交通及商场信息" />
                    </el-form-item>
                    <el-form-item label="优惠活动">
                        <el-input v-model="form.promotionInfo" type="textarea" :rows="2" placeholder="请输入打折优惠场景（如：节日8折）" />
                    </el-form-item>
                </el-form>
             </el-card>

             <el-card shadow="never" class="section-card" style="margin-top: 20px">
                <template #header>
                   <div class="card-header">
                      <span>图片管理</span>
                   </div>
                </template>
                <el-form label-width="100px">
                    <el-form-item label="封面图片" required>
                        <el-upload
                            class="avatar-uploader"
                            :action="uploadUrl"
                            :show-file-list="false"
                            :on-success="handleCoverSuccess"
                            :before-upload="beforeUpload">
                            <img v-if="form.coverImage" :src="form.coverImage" class="avatar" />
                            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                        </el-upload>
                        <div class="upload-tip">建议尺寸 800x600，仅限一张</div>
                    </el-form-item>
                    <el-form-item label="轮播图片">
                        <el-upload
                            v-model:file-list="fileList"
                            :action="uploadUrl"
                            list-type="picture-card"
                            :on-success="handleCarouselSuccess"
                            :on-remove="handleCarouselRemove"
                            :before-upload="beforeUpload">
                            <el-icon><Plus /></el-icon>
                        </el-upload>
                        <div class="upload-tip">展示在详情页顶部的轮播图，支持多张</div>
                    </el-form-item>
                </el-form>
             </el-card>

             <el-card shadow="never" class="section-card" style="margin-top: 20px">
                <template #header>
                   <div class="card-header">
                      <span>设施与标签</span>
                   </div>
                </template>
                 <el-form label-width="100px">
                     <el-form-item label="快捷标签">
                        <el-select
                            v-model="tagsList"
                            multiple
                            filterable
                            allow-create
                            default-first-option
                            placeholder="请选择或输入标签"
                            style="width: 100%">
                            <el-option label="亲子" value="亲子" />
                            <el-option label="豪华" value="豪华" />
                            <el-option label="免费停车" value="免费停车" />
                            <el-option label="情侣" value="情侣" />
                            <el-option label="商务" value="商务" />
                            <el-option label="海景" value="海景" />
                            <el-option label="地铁周边" value="地铁周边" />
                        </el-select>
                        <div class="upload-tip">用于前台快速筛选，如：亲子、豪华、免费停车等</div>
                     </el-form-item>
                     <el-form-item label="基础设施">
                         <el-checkbox-group v-model="facilities">
                            <el-checkbox label="Wifi" border />
                            <el-checkbox label="停车场" border />
                            <el-checkbox label="游泳池" border />
                            <el-checkbox label="健身房" border />
                            <el-checkbox label="餐厅" border />
                            <el-checkbox label="会议室" border />
                            <el-checkbox label="接送机" border />
                        </el-checkbox-group>
                     </el-form-item>
                 </el-form>
             </el-card>
             
             <div class="footer-actions">
                <el-button type="primary" size="large" round @click="save" style="width: 200px">保存并提交</el-button>
             </div>
          </el-col>
       </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick, onUnmounted } from 'vue'
import request from '../../utils/request'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, Location } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStr = localStorage.getItem('user')
const user = userStr ? JSON.parse(userStr) : {}

const isEdit = computed(() => !!route.params.id)
const facilities = ref<string[]>([])
const tagsList = ref<string[]>([])
const uploadUrl = 'http://localhost:9090/file/upload'
const fileList = ref<any[]>([])

const form = reactive({
  id: undefined,
  name: '',
  nameEn: '',
  city: '',
  address: '',
  latitude: null as number | null,
  longitude: null as number | null,
  starRating: '',
  openDate: '',
  description: '',
  surroundings: '',
  promotionInfo: '',
  merchantId: user.id,
  facilities: '',
  tags: '',
  status: 0,
  coverImage: '',
  images: '' // JSON string
})

const locationSearchText = ref('')
let map: any = null
let marker: any = null
let placeSearch: any = null
let geocoder: any = null

onMounted(() => {
  if (isEdit.value) {
    loadHotel(route.params.id)
  }
  nextTick(() => {
    initMap()
  })
})

onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})

const initMap = () => {
  if (typeof AMap === 'undefined') {
    ElMessage.warning('地图API加载失败，请检查网络连接或联系管理员配置地图密钥')
    return
  }
  
  // 初始化地图
  const center = form.longitude && form.latitude 
    ? [form.longitude, form.latitude] 
    : [116.397428, 39.90923] // 默认北京天安门
  
  map = new AMap.Map('map-container', {
    zoom: form.longitude && form.latitude ? 15 : 11,
    center: center,
    mapStyle: 'amap://styles/normal'
  })
  
  // 初始化地点搜索
  placeSearch = new AMap.PlaceSearch({
    map: map,
    pageSize: 10
  })
  
  // 初始化地理编码
  geocoder = new AMap.Geocoder({
    city: '全国'
  })
  
  // 如果已有位置，添加标记
  if (form.longitude && form.latitude) {
    addMarker([form.longitude, form.latitude])
  }
  
  // 地图点击事件
  map.on('click', (e: any) => {
    const lng = e.lnglat.getLng()
    const lat = e.lnglat.getLat()
    addMarker([lng, lat])
    reverseGeocode(lng, lat)
  })
}

const addMarker = (position: number[]) => {
  if (marker) {
    marker.setPosition(position)
  } else {
    marker = new AMap.Marker({
      position: position,
      draggable: true
    })
    map.add(marker)
    
    // 标记拖拽事件
    marker.on('dragend', () => {
      const pos = marker.getPosition()
      reverseGeocode(pos.getLng(), pos.getLat())
    })
  }
  map.setCenter(position)
  map.setZoom(15)
}

const reverseGeocode = (lng: number, lat: number) => {
  geocoder.getAddress([lng, lat], (status: string, result: any) => {
    if (status === 'complete' && result.info === 'OK') {
      const addressComponent = result.regeocode.addressComponent
      form.longitude = lng
      form.latitude = lat
      // 只保存市级别，去掉区县等更细的级别
      // addressComponent.city 是市级别（如：北京市、上海市）
      // 如果city为空，使用province（省级别）作为后备
      let cityName = addressComponent.city || addressComponent.province || ''
      // 去掉"市"、"省"等后缀，统一格式
      if (cityName) {
        cityName = cityName.replace(/市$/, '').replace(/省$/, '')
      }
      form.city = cityName
      form.address = result.regeocode.formattedAddress || `${addressComponent.district}${addressComponent.street}${addressComponent.streetNumber}`
      ElMessage.success('位置已选择')
    } else {
      // 检查是否是API密钥错误
      if (result && result.info) {
        if (result.info === 'USERKEY_PLAT_NOMATCH' || result.infocode === '10009') {
          ElMessage.error({
            message: 'API密钥类型不匹配！请确保申请的是"Web端（JS API）"类型的密钥。',
            duration: 5000
          })
        } else if (result.info === 'INVALID_USER_SCODE' || result.infocode === '10008') {
          ElMessage.error({
            message: '安全密钥无效！请检查安全密钥是否正确配置在 index.html 中。',
            duration: 5000
          })
        } else {
          ElMessage.error('获取地址信息失败：' + result.info + (result.infocode ? ' (错误代码：' + result.infocode + ')' : ''))
        }
      } else {
        ElMessage.error('获取地址信息失败')
      }
    }
  })
}

const searchLocation = () => {
  if (!locationSearchText.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  if (!placeSearch) {
    ElMessage.error('地图未初始化')
    return
  }
  
  placeSearch.search(locationSearchText.value, (status: string, result: any) => {
    if (status === 'complete' && result.poiList && result.poiList.pois.length > 0) {
      const poi = result.poiList.pois[0]
      const location = poi.location
      addMarker([location.lng, location.lat])
      form.longitude = location.lng
      form.latitude = location.lat
      // 只保存市级别，去掉区县等更细的级别
      // poi.cityname 是市级别（如：北京市、上海市）
      // poi.adname 可能是区县级别（如：朝阳区、定南县），需要提取市的部分
      let cityName = ''
      if (poi.cityname) {
        // 优先使用cityname（市级别）
        cityName = poi.cityname
      } else if (poi.adname) {
        // 如果只有adname，尝试提取市的部分
        // adname格式可能是："XX市XX区"、"XX县"、"XX区"等
        // 如果adname包含"市"，提取市的部分
        if (poi.adname.includes('市')) {
          const cityMatch = poi.adname.match(/(.+?市)/)
          if (cityMatch) {
            cityName = cityMatch[1]
          } else {
            // 如果无法匹配，使用adname（可能是"XX市"格式）
            cityName = poi.adname
          }
        } else {
          // 如果是县或区，无法确定市，使用adname但去掉县/区后缀
          // 注意：这种情况下可能不准确，但至少有个值
          cityName = poi.adname.replace(/县$/, '').replace(/区$/, '')
        }
      }
      // 去掉"市"后缀，统一格式（与Android端保持一致）
      if (cityName) {
        cityName = cityName.replace(/市$/, '')
      }
      form.city = cityName
      form.address = poi.address + poi.name
      ElMessage.success('已找到位置')
    } else {
      // 检查是否是API密钥错误
      if (result && result.info) {
        if (result.info === 'USERKEY_PLAT_NOMATCH' || result.infocode === '10009') {
          ElMessage.error({
            message: 'API密钥类型不匹配！请确保申请的是"Web端（JS API）"类型的密钥，而不是"Web服务"类型。',
            duration: 5000
          })
        } else if (result.info === 'INVALID_USER_SCODE' || result.infocode === '10008') {
          ElMessage.error({
            message: '安全密钥无效！请检查安全密钥是否正确配置在 index.html 中。',
            duration: 5000
          })
        } else {
          ElMessage.error('搜索失败：' + result.info + (result.infocode ? ' (错误代码：' + result.infocode + ')' : ''))
        }
      } else {
        ElMessage.error('未找到相关地点，请尝试其他关键词')
      }
    }
  })
}

const loadHotel = (id: any) => {
  request.get(`/hotel/detail/${id}`).then((res: any) => {
    Object.assign(form, res.hotel)
    // 如果有经纬度，在地图上显示
    if (form.longitude && form.latitude && map) {
      nextTick(() => {
        addMarker([form.longitude!, form.latitude!])
      })
    }
    if (res.hotel.facilities) {
        facilities.value = res.hotel.facilities.split(',')
    }
    if (res.hotel.tags) {
        tagsList.value = res.hotel.tags.split(',')
    }
    // Handle images
    if (res.hotel.images) {
        try {
            const imgs = JSON.parse(res.hotel.images)
            fileList.value = imgs.map((url: string) => ({ name: 'img', url }))
        } catch (e) {
            // fallback if not json
            if(res.hotel.images.includes(',')) {
                 fileList.value = res.hotel.images.split(',').map((url: string) => ({ name: 'img', url }))
            } else {
                 fileList.value = [{ name: 'img', url: res.hotel.images }]
            }
        }
    }
  })
}

const handleCoverSuccess = (res: any) => {
    if(res.code === '200') {
        form.coverImage = res.data
    } else {
        ElMessage.error('上传失败')
    }
}

const handleCarouselSuccess = (res: any, file: any) => {
    if(res.code === '200') {
        const idx = fileList.value.findIndex(f => f.uid === file.uid)
        if(idx !== -1) {
            fileList.value[idx].url = res.data
        }
    }
}

const handleCarouselRemove = (file: any, uploadFiles: any) => {
    // fileList is synced automatically
}

const beforeUpload = (file: any) => {
    const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
    const isLt2M = file.size / 1024 / 1024 < 10; 

    if (!isJPG) {
        ElMessage.error('上传图片只能是 JPG/PNG 格式!');
    }
    if (!isLt2M) {
        ElMessage.error('上传图片大小不能超过 10MB!');
    }
    return isJPG && isLt2M;
}

const save = () => {
  form.facilities = facilities.value.join(',')
  form.tags = tagsList.value.join(',')
  // Process carousel images
  const imageUrls = fileList.value.map(f => f.url || f.response?.data).filter(url => url)
  form.images = JSON.stringify(imageUrls)
  
  request.post('/hotel/save', form).then((res: any) => {
    ElMessage.success('保存成功')
    if(!isEdit.value) {
       router.push('/dashboard')
    }
  })
}
</script>

<style scoped>
.hotel-detail-container {
    padding: 0;
}
.page-header {
    background: #fff;
    padding: 16px 24px;
    border-bottom: 1px solid #eee;
    margin: -20px -20px 20px -20px;
}
.section-card {
    margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.footer-actions {
    text-align: center;
    margin-top: 30px;
    padding-bottom: 30px;
}
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
.upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
}

.location-selector {
    width: 100%;
}

.location-info {
    margin-top: 10px;
}

#map-container {
    border-radius: 4px;
}
</style>
