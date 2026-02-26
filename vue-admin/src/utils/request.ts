import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:9090',
  timeout: 5000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  config.headers['Content-Type'] = 'application/json;charset=utf-8';
  return config
}, error => {
  return Promise.reject(error)
})

request.interceptors.response.use(response => {
  let res = response.data
  console.log('Response:', res)
  if (res.code === '200') {
    return res
  } else {
    ElMessage.error(res.msg || 'System Error')
    return Promise.reject(res.msg || 'System Error')
  }
}, error => {
  console.error('Error:', error)
  ElMessage.error(error.message || 'System Error')
  return Promise.reject(error)
})

export default request
