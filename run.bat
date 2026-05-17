@echo off
setlocal enabledelayedexpansion
title Vehicle Verification System - Server Console

:: ══════════════════════════════════════════════════════
::   VEHICLE VERIFICATION SYSTEM (VVS) - STARTUP
:: ══════════════════════════════════════════════════════

echo.
echo  [1/4] Configuring Environment...
set MONGODB_URI=mongodb+srv://parabshounak6_db_user:parab1122@vehicle.qpwzwzc.mongodb.net/?appName=vehicle
set MONGODB_DB=vehicleDB
set PORT=8080

echo  [2/4] Verifying System Dependencies...
:: Java Check
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install JDK 17+.
    pause & exit /b 1
)

:: Maven Check
set MVN_CMD=mvn
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    if exist "C:\Tools\apache-maven-3.9.6\bin\mvn.cmd" (
        set MVN_CMD="C:\Tools\apache-maven-3.9.6\bin\mvn.cmd"
    ) else (
        echo [ERROR] Maven not found.
        pause & exit /b 1
    )
)

echo  [3/4] Initializing Server...
echo        - Port: %PORT%
echo        - DB: %MONGODB_DB% (Atlas)
echo.
echo [INFO] Compiling project and launching server...
echo [INFO] A browser window will open automatically once initialized.
echo.

:: Launch Browser in background
start /B cmd /c "timeout /t 12 /nobreak >nul && start http://localhost:%PORT%/"

:: Start Server
call %MVN_CMD% clean compile exec:java -Dexec.mainClass="com.vehicleverify.main.MainServer"

if %errorlevel% neq 0 (
    echo.
    echo ══════════════════════════════════════════════════
    echo [CRITICAL ERROR] Server failed to start.
    echo Please check your MongoDB connection or Maven logs.
    echo ══════════════════════════════════════════════════
    pause
)

endlocal
