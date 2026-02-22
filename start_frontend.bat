@echo off
chcp 65001 >nul
echo ==========================================
echo      Starting Frontend Admin (By Trae)
echo ==========================================
echo.
echo [1/3] Entering vue-admin directory...
cd vue-admin

echo [2/3] Installing dependencies (this may take a while)...
call npm install

echo [3/3] Starting development server...
call npm run dev

pause
