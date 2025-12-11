@echo off
cd /d "C:\Users\micro\Desktop\Twsela\twsela"
echo.
echo ========================================
echo   Building Twsela Backend
echo ========================================
echo.
call mvn clean package -DskipTests
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   Starting Backend on port 8443
    echo   Swagger UI: http://localhost:8443/swagger-ui.html
    echo ========================================
    echo.
    java -jar target\twsela-0.0.1-SNAPSHOT.jar
) else (
    echo.
    echo Build failed! Please check the errors above.
    pause
)
