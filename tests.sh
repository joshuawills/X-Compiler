#!/usr/bin/env bash

TEMP="tests/.temp"
EXE="build/libs/XC-1.0-SNAPSHOT.jar"

FAIL=0
PASS=0

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RESET='\033[0m'

help() {
    echo "How to use test suite:"
    echo "    Create a file with the format test_[0-9]+.x in the tests directory"
    echo "    Make the first line a comment with the expected exit code: E.g. // exit 1"
    echo "    If there's any stdout you want to test, create a file with the same name but with .txt instead of .x"
    echo "    The script will then do a diff compare to make sure it's the same"
    echo "    No need to provide the .txt file if there's no output to test"
    echo "    If you expect the program to have a build fail place 'FAIL' on the first line rather than a number"
    echo "    Any files in 'tokens' folder will compare tokens"
    echo "    Any files in 'parse' folder will compare parse trees"
    echo "    Any files in 'error' folder will compare errors"
    echo
    exit 0
}

# Helpful details for shell script
if [ "$#" -ne "0" ]
then
    if [ "$1" = "--help" ] || [ "$1" = "-h" ]
    then
        help
        exit 0
    fi
fi

# Building
# gradle build >> /dev/null 2>&1

# Handle token tests first
echo -e "${YELLOW}TOKEN TESTS: ${RESET}"
while IFS= read -r file
do

  if echo "$file" | grep -vE "\.x" >> /dev/null 2>&1
  then
    continue
  fi

  java -jar "$EXE" "$file" -t -q > "$TEMP"
  real_file=$(echo "$file" | sed -E 's/x$/txt/g')

  message=$(head -n1 "$file")

  if ! [ -f "$real_file" ]
  then
    continue
  fi

  output=$(diff -q "$TEMP" "$real_file")
  if [ -n "$output" ]
  then
    echo -e "    '$(basename "$file")' ${RED}FAILED${RESET} ${message}"
    FAIL=$((FAIL+1))
  else
    echo -e "    '$(basename "$file")' ${GREEN}PASSED${RESET} ${message}"
    PASS=$((PASS+1))
  fi

done < <(find "tests/tokens" -type f)

echo -e "${YELLOW}RUNNING TESTS: ${RESET}"
while IFS= read -r file
do

  if echo "$file" | grep -vE "\.x" >> /dev/null 2>&1
  then
    continue
  fi

  message=$(head -n1 "$file")

  java -jar "$EXE" "$file" -q -r > "$TEMP"
  real_file=$(echo "$file" | sed -E 's/x$/txt/g')

  output=$(diff -q "$TEMP" "$real_file")
  if [ -n "$output" ]
  then
    echo -e "    '$(basename "$file")' ${RED}FAILED${RESET} ${message}"
    FAIL=$((FAIL+1))
  else
    echo -e "    '$(basename "$file")' ${GREEN}PASSED${RESET} ${message}"
    PASS=$((PASS+1))
  fi

done < <(find "tests/running" -type f)


echo -e "${YELLOW}FAILING TESTS: ${RESET}"
while IFS= read -r file
do

  if echo "$file" | grep -vE "\.x" >> /dev/null 2>&1
  then
    continue
  fi

  if echo "$file" | grep -vE "_[0-9]+\.x" >> /dev/null 2>&1
  then
    continue
  fi

  message=$(head -n1 "$file")

  errors=$(java -jar "$EXE" -q "$file")
  cleaned_errors=$(echo "$errors" | sed 's/\x1b\[[0-9;]*m//g')
  sorted_numbers=$(echo "$cleaned_errors" | grep -Eo "\*[0-9]+" | sed -E 's/\*//g'| sort -n)
  echo "$sorted_numbers" > "$TEMP"

  real_file=$(echo "$file" | sed -E 's/x$/txt/g')

  output=$(diff -q "$TEMP" "$real_file")
  if [ -n "$output" ]
  then
    echo -e "    '$(basename "$file")' ${RED}FAILED${RESET} ${message}"
    FAIL=$((FAIL+1))
  else
    echo -e "    '$(basename "$file")' ${GREEN}PASSED${RESET} ${message}"
    PASS=$((PASS+1))
  fi

done < <(find "tests/errors" -type f)


echo -e "${YELLOW}MINOR TESTS: ${RESET}"
while IFS= read -r file
do

  if echo "$file" | grep -vE "\.x" >> /dev/null 2>&1
  then
    continue
  fi

  message=$(head -n1 "$file")

  errors=$(java -jar "$EXE" "$file")
  cleaned_errors=$(echo "$errors" | sed 's/\x1b\[[0-9;]*m//g')
  sorted_numbers=$(echo "$cleaned_errors" | grep -Eo "\*[0-9]+" | sed -E 's/\*//g'| sort -n)
  echo "$sorted_numbers" > "$TEMP"

  real_file=$(echo "$file" | sed -E 's/x$/txt/g')

  output=$(diff -q "$TEMP" "$real_file")
  if [ -n "$output" ]
  then
    echo -e "    '$(basename "$file")' ${RED}FAILED${RESET} ${message}"
    FAIL=$((FAIL+1))
  else
    echo -e "    '$(basename "$file")' ${GREEN}PASSED${RESET} ${message}"
    PASS=$((PASS+1))
  fi

done < <(find "tests/minor-errors" -type f)


rm "$TEMP"

echo
echo -e "${YELLOW}TEST SUMMARY: ${RESET}"
if [ "$FAIL" = 0 ]
then
    echo -e "    ${GREEN}All passed${RESET}"
    echo -e "    ${PASS} total"
else
    echo -e "    ${GREEN}${PASS} passed${RESET}"
    echo -e "    ${RED}${FAIL} failed${RESET}"
    echo -e "    $((PASS + FAIL)) total"
fi
