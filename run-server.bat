@echo off
title "Electricity Bill Generator - Server"
cd /d "%~dp0backend"
echo ============================================================
echo Starting Electricity Bill Generator Server on port 8080...
echo Open your browser at: http://localhost:8080/
echo ============================================================

set "JAVA_CMD=java"
if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe"
)

"%JAVA_CMD%" -cp . Main
pause
