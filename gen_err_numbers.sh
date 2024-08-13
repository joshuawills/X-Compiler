#!/usr/bin/env bash

EXE="build/libs/XC-1.0-SNAPSHOT.jar"

errors=$(java -jar "$EXE" -q "$1")
cleaned_errors=$(echo "$errors" | sed 's/\x1b\[[0-9;]*m//g')
sorted_numbers=$(echo "$cleaned_errors" | grep -Eo "\*[0-9]+" | sed -E 's/\*//g'| sort -n)

echo "$sorted_numbers"
