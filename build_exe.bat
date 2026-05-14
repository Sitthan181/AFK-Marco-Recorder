@echo off
:: ============================================================
::  AFK — สร้าง .exe ด้วย jpackage (Windows)
::  วางไฟล์นี้ไว้ในโฟลเดอร์เดียวกับ AFK.jar แล้วดับเบิลคลิก
:: ============================================================

setlocal
set APP_NAME=AFK
set APP_VERSION=1.0.0
set JAR=AFK.jar
set MAIN_CLASS=afk.MainFrame
set ICON=icons\app.ico
set OUT_DIR=exe_output

echo.
echo  ╔══════════════════════════════════╗
echo  ║   AFK .exe Builder               ║
echo  ╚══════════════════════════════════╝
echo.

:: -- Check jpackage ------------------------------------------
where jpackage >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ไม่พบ jpackage
    echo         กรุณาติดตั้ง JDK 17+ และเพิ่มลงใน PATH
    echo         ดาวน์โหลด: https://adoptium.net/
    pause & exit /b 1
)

:: -- Check AFK.jar -------------------------------------------
if not exist "%JAR%" (
    echo [ERROR] ไม่พบ %JAR% กรุณาวางไฟล์นี้ไว้ในโฟลเดอร์เดียวกับ AFK.jar
    pause & exit /b 1
)

:: -- Clean output dir ----------------------------------------
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"

:: -- Build with icon (ถ้ามี) หรือไม่มี icon ก็ได้ -----------
echo [1/2] กำลังสร้าง .exe ...
if exist "%ICON%" (
    jpackage ^
      --type exe ^
      --input . ^
      --name "%APP_NAME%" ^
      --main-jar "%JAR%" ^
      --main-class "%MAIN_CLASS%" ^
      --app-version "%APP_VERSION%" ^
      --description "AFK Macro Recorder" ^
      --vendor "AFK" ^
      --icon "%ICON%" ^
      --win-shortcut ^
      --win-menu ^
      --win-dir-chooser ^
      --dest "%OUT_DIR%"
) else (
    jpackage ^
      --type exe ^
      --input . ^
      --name "%APP_NAME%" ^
      --main-jar "%JAR%" ^
      --main-class "%MAIN_CLASS%" ^
      --app-version "%APP_VERSION%" ^
      --description "AFK Macro Recorder" ^
      --vendor "AFK" ^
      --win-shortcut ^
      --win-menu ^
      --win-dir-chooser ^
      --dest "%OUT_DIR%"
)

if errorlevel 1 (
    echo.
    echo [ERROR] jpackage ล้มเหลว
    echo         ตรวจสอบว่า JDK 17+ ติดตั้งอยู่และ PATH ถูกต้อง
    pause & exit /b 1
)

echo.
echo [2/2] สำเร็จ!
echo.
echo  ไฟล์ .exe อยู่ที่:  %OUT_DIR%\AFK-1.0.0.exe
echo.
echo  ติดตั้งด้วยการดับเบิลคลิกที่ไฟล์ .exe
echo  โปรแกรมจะถูกติดตั้งพร้อม shortcut บน Desktop และ Start Menu
echo.

:: -- Open output folder --------------------------------------
explorer "%OUT_DIR%"
pause
