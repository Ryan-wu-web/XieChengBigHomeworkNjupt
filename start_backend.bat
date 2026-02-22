@echo off
chcp 65001 >nul
echo ==========================================
echo      Starting Backend Server (By Trae)
echo ==========================================
echo.
echo [1/2] Entering server directory...
cd server

echo [2/2] Running Spring Boot application...
call mvn spring-boot:run

pause
