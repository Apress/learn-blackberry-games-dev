@echo off
echo Running %1 with LibusbJava...

java -Djava.library.path="d:\libusbjava"  -cp "d:\libusbjava\ch.ntb.usb-0.5.9.jar;."  %1

echo Finished.
