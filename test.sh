#!/bin/bash

# Find all .sql files in the input directory
sql_files=$(find samples/input -name "*.sql")

# Loop through each SQL file found
for file in $sql_files; do
    # Extract the file name without the directory path and extension
    filename=$(basename -- "$file")
    filename_no_ext="${filename%.*}"
    
    # Run the Java command with the current SQL file
    java -jar target/lightdb-1.0.0-jar-with-dependencies.jar samples/db "$file" "samples/output/$filename_no_ext.csv"
done
