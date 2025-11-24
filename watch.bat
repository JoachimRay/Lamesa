@echo off
setlocal enabledelayedexpansion
mode con: lines=40 cols=120

echo.
echo ========================================
echo JavaFX Live Watch Script
echo ========================================
echo.
echo This window will STAY OPEN so you can see errors
echo Press Ctrl+C to stop
echo.

:loop
echo.
echo [%date% %time%] ========== STARTING APP ==========
echo.

call mvn javafx:run

set exitCode=%errorlevel%
echo.
echo ========================================
if %exitCode% neq 0 (
    echo BUILD/RUN FAILED - Exit Code: %exitCode%
    echo FIX THE ERRORS ABOVE, THEN SAVE YOUR FILES
) else (
    echo APP CLOSED SUCCESSFULLY
)
echo ========================================
echo.
echo Waiting 2 minutes before restarting...
timeout /t 120 /nobreak
goto loop
