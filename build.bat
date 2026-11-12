@echo off
echo =========================================
echo  RideWise -- Smart Cab Management System
echo  Build Script for Windows
echo =========================================
echo.

REM Create output directory
if not exist out mkdir out

echo Compiling Java source files...
javac -d out -sourcepath src src/Main.java src/models/*.java src/algorithms/*.java src/data/*.java src/ui/*.java

if %errorlevel% equ 0 (
    echo.
    echo Compilation SUCCESSFUL!
    echo.
    echo To run the application:
    echo   java -cp out Main
    echo.
    echo OR run run.bat
) else (
    echo.
    echo Compilation FAILED. Check errors above.
    echo Make sure Java JDK is installed and javac is in your PATH.
    echo Download from: https://www.oracle.com/java/technologies/downloads/
)

pause
