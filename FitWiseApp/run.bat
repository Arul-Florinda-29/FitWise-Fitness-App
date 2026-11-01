@echo off
setlocal
if not exist out mkdir out
javac -encoding UTF-8 -d out src\Main.java src\model\*.java src\service\*.java src\data\*.java src\gui\*.java
if errorlevel 1 pause & exit /b 1
java -cp out Main
pause
