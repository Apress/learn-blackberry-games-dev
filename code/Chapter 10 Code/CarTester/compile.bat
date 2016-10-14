@echo off
echo Compiling %1 with LibusbJava...

javac -cp "d:\libusbjava\ch.ntb.usb-0.5.9.jar;."  %1
echo Finished.
