@REM Maven wrapper script for Windows
@echo off
setlocal
if not defined JAVA_HOME (
  set "JAVA_HOME=C:\Program Files\Java\jdk-17"
)
set "PATH=%JAVA_HOME%\bin;C:\Program Files\apache-maven-3.9.15\bin;%PATH%"

if exist "C:\Program Files\apache-maven-3.9.15\bin\mvn.cmd" (
  "C:\Program Files\apache-maven-3.9.15\bin\mvn.cmd" %*
) else (
  mvn %*
)
