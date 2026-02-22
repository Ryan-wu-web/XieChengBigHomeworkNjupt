# 设置模拟器位置为北京
# 使用方法：在 PowerShell 中运行：.\set_location.ps1

Write-Host "正在设置模拟器位置为中国（北京）..." -ForegroundColor Green
Write-Host ""

# 尝试查找 adb 的路径
$adbPath = $null

# 检查常见的 Android SDK 路径
$possiblePaths = @(
    "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    "$env:USERPROFILE\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    "$env:ANDROID_HOME\platform-tools\adb.exe",
    "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe"
)

foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $adbPath = $path
        Write-Host "找到 adb: $adbPath" -ForegroundColor Green
        break
    }
}

if ($null -eq $adbPath) {
    Write-Host "错误：找不到 adb.exe" -ForegroundColor Red
    Write-Host ""
    Write-Host "请确保：" -ForegroundColor Yellow
    Write-Host "1. Android SDK 已安装"
    Write-Host "2. platform-tools 在 PATH 中，或者"
    Write-Host "3. 设置 ANDROID_HOME 环境变量"
    Write-Host ""
    Write-Host "或者手动在 Android Studio 中设置：" -ForegroundColor Yellow
    Write-Host "1. 打开模拟器"
    Write-Host "2. 点击模拟器工具栏的三个点（...）按钮"
    Write-Host "3. 选择 'Location' 标签"
    Write-Host "4. 输入：纬度 39.9042，经度 116.4074"
    Write-Host "5. 点击 'Set Location'"
    Read-Host "按 Enter 键退出"
    exit 1
}

# 检查设备连接
Write-Host "检查连接的设备..." -ForegroundColor Cyan
& $adbPath devices
Write-Host ""

# 设置位置为北京（纬度 39.9042，经度 116.4074）
Write-Host "正在设置位置为北京（纬度 39.9042，经度 116.4074）..." -ForegroundColor Cyan
$result = & $adbPath emu geo fix 116.4074 39.9042 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ 位置设置成功！" -ForegroundColor Green
    Write-Host "模拟器位置已设置为：北京（纬度 39.9042，经度 116.4074）" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "✗ 位置设置失败" -ForegroundColor Red
    Write-Host ""
    Write-Host "请尝试：" -ForegroundColor Yellow
    Write-Host "1. 确保模拟器正在运行"
    Write-Host "2. 确保只有一个模拟器实例在运行"
    Write-Host "3. 或者手动在 Android Studio 中设置位置"
}

Write-Host ""
Read-Host "按 Enter 键退出"
