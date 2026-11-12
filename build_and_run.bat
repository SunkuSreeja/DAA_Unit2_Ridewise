@echo off
echo =========================================
echo  RideWise -- Build and Run
echo =========================================
echo.
if not exist out mkdir out
javac -d out -sourcepath src src/Main.java src/models/*.java src/algorithms/*.java src/data/*.java src/ui/*.java
if %errorlevel% equ 0 (
    echo Build OK. Launching...
    java -cp out Main
) else (
    echo Build failed!
    pause
)
