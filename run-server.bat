@echo off
title Apex Bank Web Server
echo ===================================================
echo       APEX BANK MANAGEMENT SYSTEM WEB SERVER        
echo ===================================================
echo.

cd /d "%~dp0BankJavaApp"

echo [1/3] Compiling Java classes...
javac *.java
if %errorlevel% neq 0 (
    echo [ERROR] Java compilation failed! Please check your JDK installation.
    pause
    exit /b %errorlevel%
)

echo [2/3] Starting Java HTTP Web Server on port 8080...
echo.
echo ===================================================
echo   Server is running at: http://localhost:8080
echo   Press Ctrl + C to stop the server anytime.
echo ===================================================
echo.

:: Automatically open the web browser after a brief delay
start "" "http://localhost:8080"

:: Launch the Java HTTP Server
java BankWebServer

pause
