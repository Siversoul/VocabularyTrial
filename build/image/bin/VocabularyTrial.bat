@echo off
set JLINK_VM_OPTIONS=
set DIR=%~dp0
start javaw %JLINK_VM_OPTIONS% -m visparu.vocabularytrial/com.visparu.vocabularytrial.root.Main %*
