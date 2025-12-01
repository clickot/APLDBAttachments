@echo off

set ERROR_CODE=0

if "%PATH_TO_FX%" == "" "PATH_TO_FX environment variable not set" exit /b 1

if "%JAVACMD%"=="" set JAVACMD=java

%JAVACMD% %JAVA_OPTS% --module-path "%PATH_TO_FX%" --add-modules=javafx.controls,javafx.fxml -jar ${project.name}-${project.version}.jar %$

if %ERRORLEVEL% NEQ 0 goto error
goto end

:error
set ERROR_CODE=%ERRORLEVEL%

:end
if "%FORCE_EXIT_ON_ERROR%" == "on" (
  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
)
exit /B %ERROR_CODE%