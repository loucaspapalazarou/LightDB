#!/bin/bash

# Define the folders
folder1="samples/output"
folder2="samples/expected_output"

# Define colors
RED='\033[0;31m'
NC='\033[0m' # No Color

# Get the list of CSV files in folder 1
files1=("$folder1"/*.csv)

# Iterate over each file
for file1 in "${files1[@]}"; do
    # Extract the filename
    filename=$(basename "$file1")
    # Construct the corresponding file path in folder 2
    file2="$folder2/$filename"
    
    # Check if the file exists in folder 2
    if [ -e "$file2" ]; then
        # Compare the content of the two files
        if cmp -s "$file1" "$file2"; then
            echo "Files $filename have identical content."
        else
            if diff -q "$file1" "$file2" >/dev/null; then
                echo "Files $filename have same content but different ordering."
            else
                echo -e "${RED}Files $filename have different content. (Check ordering)${NC}"
            fi
        fi
    else
        echo "File $filename not found in $folder2."
    fi
done
