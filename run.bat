@echo off
cd /d "%~dp0"
start /b javaw ^
  -Dsun.java2d.dpiaware=true ^
  -Djava.awt.headless=false ^
  -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel ^
  -cp "AFK.jar;lib/JNativeHook.jar" ^
  afk.MainFrame
