@echo off

echo Compiling %1 Servlet...
javac -cp "d:\tomcat\lib\servlet-api.jar;." %1

echo Finished.
