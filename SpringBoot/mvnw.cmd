@REM Maven Wrapper startup script
@setlocal
set MAVEN_PROJECTBASEDIR=%CD%
if exist "%USERPROFILE%\mavenrc_post.cmd" call "%USERPROFILE%\mavenrc_post.cmd"
set "MAVEN_OPTS=-Xmx1024m"
if not "%~1" == "" set "APP_ARGS=%~1 %~2 %~3 %~4 %~5 %~6 %~7 %~8 %~9"
%JAVA_HOME%\bin\java.exe -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %APP_ARGS%
@endlocal
