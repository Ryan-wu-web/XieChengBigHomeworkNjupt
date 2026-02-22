# 高德地图API密钥配置说明

## ⚠️ 重要：密钥类型选择

**当前代码使用的是 Web端（JS API），必须申请对应类型的密钥！**

## 获取API密钥（Web端 JS API）

1. 访问高德开放平台：https://console.amap.com/
2. 注册/登录账号
3. 进入"应用管理" -> "我的应用"
4. 点击"创建新应用"
5. **添加Key时，必须选择"Web端（JS API）"类型** ⚠️
   - 不要选择"Web服务"类型
   - 服务平台选择"Web端（JS API）"
6. 设置安全密钥（白名单）：
   - 可以设置 `*` 允许所有域名（开发环境）
   - 或设置具体域名（生产环境）
7. 复制生成的Key

## 配置密钥

### 1. 在 index.html 中配置 Key

在 `index.html` 文件中，找到以下代码：

```html
<script type="text/javascript" src="https://webapi.amap.com/maps?v=2.0&key=YOUR_AMAP_KEY&plugin=AMap.PlaceSearch,AMap.Geocoder"></script>
```

将 `YOUR_AMAP_KEY` 替换为你申请的高德地图API密钥（Key）。

### 2. 在 index.html 中配置安全密钥

在 `index.html` 文件中，**在引入地图API的script标签之前**，添加安全密钥配置：

```html
<script type="text/javascript">
  window._AMapSecurityConfig = {
    securityJsCode: 'YOUR_SECURITY_KEY'
  }
</script>
<script type="text/javascript" src="https://webapi.amap.com/maps?v=2.0&key=YOUR_AMAP_KEY&plugin=AMap.PlaceSearch,AMap.Geocoder"></script>
```

将 `YOUR_SECURITY_KEY` 替换为你申请的安全密钥。

**注意**：
- 高德地图Web端（JS API）从2021年开始必须同时配置Key和安全密钥才能正常使用
- 安全密钥必须在引入地图API之前配置，通过 `window._AMapSecurityConfig` 全局变量设置
- 安全密钥不能在地图初始化时通过参数传递

## 常见错误

### 错误代码 10008: INVALID_USER_SCODE
**原因**：安全密钥无效或配置不正确
**解决**：
- 确保安全密钥在 `index.html` 中通过 `window._AMapSecurityConfig` 配置
- 安全密钥必须在引入地图API的script标签之前配置
- 检查安全密钥是否正确（与Key匹配）
- 确保安全密钥的白名单设置正确

### 错误代码 10009: USERKEY_PLAT_NOMATCH
**原因**：API密钥的平台类型不匹配
**解决**：
- 确保申请的是"Web端（JS API）"类型的密钥
- 不是"Web服务"类型的密钥
- 检查密钥是否正确配置

### 错误：未找到相关地点
**原因**：
- 搜索关键词不准确
- API密钥权限不足
- 网络问题

**解决**：
- 尝试使用更具体的地点名称（如：北京市朝阳区建国门外大街1号）
- 检查API密钥是否有效
- 检查网络连接

## 注意事项

- API密钥有每日调用次数限制（免费版通常为30万次/天）
- 请妥善保管API密钥，不要提交到公开仓库
- 建议使用环境变量或配置文件管理密钥
- Web端密钥需要设置安全密钥（白名单）才能正常使用
