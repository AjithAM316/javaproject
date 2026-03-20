@echo off
echo ==============================================
echo    Smart Library Management System
echo ==============================================
echo.

IF NOT EXIST "out" (
    mkdir out
)

echo [1/2] Compiling Java source files...
dir /s /B src\*.java > sources.txt
javac -cp "files\mysql-connector-j-9.6.0.jar" -d out @sources.txt

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed! Please check the errors above.
    del sources.txt
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful.
echo.
echo [2/2] Starting application...
echo.

del sources.txt
java -cp "out;files\mysql-connector-j-9.6.0.jar" com.library.ui.MainApp

echo.
pause
