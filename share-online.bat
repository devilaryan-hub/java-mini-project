@echo off
title Apex Bank - Public Web Deployment Launcher
echo =======================================================
echo     APEX BANK MANAGEMENT SYSTEM - ONLINE DEPLOYMENT     
echo =======================================================
echo.

set PROJECT_DIR=%~dp0
set CLOUDFLARED=%PROJECT_DIR%cloudflared.exe

:: 1. Compile Java
cd /d "%PROJECT_DIR%BankJavaApp"
echo [1/3] Compiling Java classes...
javac *.java
if %errorlevel% neq 0 (
    echo [ERROR] Java compilation failed! Please check your JDK.
    pause
    exit /b %errorlevel%
)

:: 2. Start Java Server in background
echo [2/3] Launching Java Web Server on port 8080...
start /b "" java BankWebServer

:: 3. Start Cloudflare Tunnel
echo [3/3] Generating Public Live HTTPS URL...
echo.
echo =======================================================
echo   Access on this PC:        http://localhost:8080
echo   Access on Same Wi-Fi/LAN: http://192.168.0.109:8080
echo =======================================================
echo.
echo Launching Cloudflare Tunnel for worldwide access...
echo (Look below for the "https://....trycloudflare.com" link)
echo.

"%CLOUDFLARED%" tunnel --url http://localhost:8080

pause
