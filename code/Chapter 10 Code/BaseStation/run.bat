@echo off
echo Running %1 with BlueCove and LibusbJava...

java -Djava.library.path="d:\libusbjava"  -cp "bluecove-2.1.0.jar;d:\libusbjava\ch.ntb.usb-0.5.9.jar;."  %1

echo Finished.
