@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\backend\FitWiseBackend.java
if errorlevel 1 (
  echo Backend compilation failed. Install JDK 17 or newer and ensure javac is on PATH.
  pause
  exit /b 1
)
java -cp out backend.FitWiseBackend
pause
