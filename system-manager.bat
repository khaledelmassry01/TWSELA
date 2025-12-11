@echo off
title Twsela System Manager

:menu
cls
echo ================================
echo    Twsela System Manager
echo ================================
echo.
echo Unified Backend + Frontend System
echo (Single Spring Boot Application)
echo.
echo 1. Start System (Backend + Frontend)
echo 2. Stop System  
echo 3. Check Status
echo 4. Show Processes
echo 5. Open in Browser
echo 6. Exit
echo.
set /p choice="Enter choice (1-6): "

if "%choice%"=="1" goto start
if "%choice%"=="2" goto stop
if "%choice%"=="3" goto status
if "%choice%"=="4" goto processes
if "%choice%"=="5" goto browser
if "%choice%"=="6" goto exit
goto menu

:start
cls
echo Starting Twsela System...
echo.
echo ========================================
echo    Twsela Unified System (Backend + Frontend)
echo ========================================
echo.
echo The system runs both backend and frontend on the same server (Port 8080)
echo Frontend files are served by the Spring Boot application
echo.
echo Starting Spring Boot Application...
echo This will start both:
echo   - Backend APIs (REST services)
echo   - Frontend (HTML, CSS, JS files)
echo.
echo Please wait for the application to start...
echo.

start "Twsela System" cmd /k "cd /d %~dp0twsela && mvn spring-boot:run"

echo.
echo ========================================
echo    System Starting...
echo ========================================
echo.
echo Backend + Frontend: http://127.0.0.1:8080
echo Login Page: http://127.0.0.1:8080/login.html
echo API Health: http://127.0.0.1:8080/api/auth/health
echo.
echo The application will open in a new window.
echo Please wait for "Started TwselaApplication" message.
echo.
echo Press any key to return to menu...
pause >nul
goto menu

:stop
cls
echo Stopping Twsela System...
echo.
echo ========================================
echo    Stopping Unified System
echo ========================================
echo.
echo Stopping Spring Boot Application (Backend + Frontend)...
taskkill /f /im java.exe
echo.
echo System stopped successfully!
echo Both backend and frontend services have been terminated.
echo.
pause
goto menu

:status
cls
echo Twsela System Status
echo ====================
echo.
echo ========================================
echo    Unified System Status Check
echo ========================================
echo.
echo Checking Spring Boot Application (Backend + Frontend)...
echo.
echo Port 8080 (Main Application):
netstat -an | findstr ":8080"
echo.
echo Testing API Health...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://127.0.0.1:8080/api/auth/health' -Method GET -TimeoutSec 5; Write-Host 'API Status: OK' -ForegroundColor Green; Write-Host 'Response:' $response.Content } catch { Write-Host 'API Status: NOT RUNNING' -ForegroundColor Red }"
echo.
echo Testing Frontend Access...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://127.0.0.1:8080/login.html' -Method GET -TimeoutSec 5; Write-Host 'Frontend Status: OK' -ForegroundColor Green } catch { Write-Host 'Frontend Status: NOT ACCESSIBLE' -ForegroundColor Red }"
echo.
echo ========================================
echo    Access URLs
echo ========================================
echo Main Application: http://127.0.0.1:8080
echo Login Page: http://127.0.0.1:8080/login.html
echo API Health: http://127.0.0.1:8080/api/auth/health
echo.
pause
goto menu

:processes
cls
echo Running Processes
echo ==================
echo.
echo ========================================
echo    Twsela System Processes
echo ========================================
echo.
echo Java Processes (Spring Boot Application):
tasklist | findstr java
echo.
echo ========================================
echo    Process Details
echo ========================================
echo.
echo Looking for Twsela-related processes...
tasklist | findstr /i "java"
echo.
echo Note: The main Twsela process should be running as java.exe
echo This single process handles both backend and frontend services.
echo.
pause
goto menu

:browser
cls
echo Opening Twsela in Browser...
echo.
echo ========================================
echo    Opening Twsela Application
echo ========================================
echo.
echo Checking if system is running...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://127.0.0.1:8080/api/auth/health' -Method GET -TimeoutSec 3; Write-Host 'System is running. Opening browser...' -ForegroundColor Green; Start-Process 'http://127.0.0.1:8080/login.html' } catch { Write-Host 'System is not running. Please start the system first.' -ForegroundColor Red; Write-Host 'Press any key to return to menu...'; $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown') }"
echo.
pause
goto menu

:exit
exit
