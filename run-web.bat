@echo off
echo Compiling Hotel Booking System...
javac -d out src\hotel\model\*.java src\hotel\*.java src\hotel\web\*.java
if errorlevel 1 (
    echo.
    echo Compilation failed. Please check for errors above.
    pause
    exit /b 1
)
echo Compilation successful.
echo.
echo ========================================
echo  HOTEL BOOKING SYSTEM - WEB APPLICATION
echo ========================================
echo.
java --enable-native-access=ALL-UNNAMED -cp "out;lib\sqlite-jdbc-3.53.2.0.jar" hotel.web.WebServer

