#!/usr/bin/env pwsh
# Rebuild Spring Boot Application

Write-Host "Building Twsela Backend..." -ForegroundColor Cyan
Set-Location C:\Users\micro\Desktop\Twsela\twsela
mvn clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Build successful!" -ForegroundColor Green
    Write-Host "Starting application on port 8080...`n" -ForegroundColor Cyan
    java -jar target\twsela-0.0.1-SNAPSHOT.jar
} else {
    Write-Host "`n❌ Build failed!" -ForegroundColor Red
    exit 1
}
