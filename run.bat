@echo off
setlocal enabledelayedexpansion

set "file1="
set "file2="
set "file3="
set "counter=1"

for /f "tokens=*" %%a in (input.txt) do (
    if !counter!==1 set "file1=%%a"
    if !counter!==2 set "file2=%%a"
    if !counter!==3 set "file3=%%a"
    set /a counter+=1
)

java -jar target/lightdb-1.0.0-jar-with-dependencies.jar "!file1!" "!file2!" "!file3!"
