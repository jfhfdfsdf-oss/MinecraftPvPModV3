@echo off
setlocal
set GRADLE_VERSION=9.2.1
set CACHE=%USERPROFILE%\.gradle\wrapper\dists\manual-gradle-%GRADLE_VERSION%
set BIN=%CACHE%\gradle-%GRADLE_VERSION%\bin\gradle.bat
if not exist "%BIN%" (
  if not exist "%CACHE%" mkdir "%CACHE%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip -OutFile '%CACHE%\gradle.zip'; Expand-Archive -Force '%CACHE%\gradle.zip' '%CACHE%'; Remove-Item '%CACHE%\gradle.zip'"
)
call "%BIN%" -p "%~dp0" %*
endlocal
