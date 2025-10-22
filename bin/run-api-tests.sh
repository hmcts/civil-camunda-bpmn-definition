#!/bin/bash
set -e

compare_ft_groups() {
  local ft_groups_csv pr_ft_groups_csv

  # Extract ftGroups array as a comma-separated string (sorted)
  ft_groups_csv=$(jq -r '
    if (.ftGroups == null or (.ftGroups | length == 0)) 
    then "" 
    else (.ftGroups | sort | join(",")) 
    end
  ' "$TEST_FILES_REPORT")

  # Normalize PR_FT_GROUPS (sort, trim spaces, split by comma, then rejoin sorted)
  pr_ft_groups_csv=""
  if [ -n "$PR_FT_GROUPS" ]; then
    pr_ft_groups_csv=$(echo "$PR_FT_GROUPS" | tr ',' '\n' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//' | sort | paste -sd "," -)
  fi

  # Comparison logic
  if [ "$ft_groups_csv" = "$pr_ft_groups_csv" ]; then
    return 0  # true — they match
  else
    return 1  # false — they differ
  fi
}

run_functional_test_groups() {
  command="yarn test:api-nonprod --grep "
  pr_ft_groups=$(echo "$PR_FT_GROUPS" | awk '{print tolower($0)}')
  
  regex_pattern=""

  IFS=',' read -ra ft_groups_array <<< "$pr_ft_groups"

  for ft_group in "${ft_groups_array[@]}"; do
      if [ -n "$regex_pattern" ]; then
          regex_pattern+="|"
      fi
      regex_pattern+="@api-$ft_group"
  done

  command+="'$regex_pattern'"
  echo "Executing: $command"
  eval "$command"
}

run_functional_tests() {
  echo "Running all functional tests on ${ENVIRONMENT} env"
  if [ "$ENVIRONMENT" = "aat" ]; then
    yarn test:api-prod
  elif [ -z "$PR_FT_GROUPS" ]; then
    yarn test:api-nonprod
  else
    run_functional_test_groups
  fi
}

run_failed_not_executed_functional_tests() {
  echo "Running failed and not executed functional test files on ${ENVIRONMENT} env"

  #Move testFilesReport.json to prevTestFilesReport.json
  mv "$TEST_FILES_REPORT" "$PREV_TEST_FILES_REPORT"

  # Collect array elements into a comma-separated string
  PREV_FAILED_TEST_FILES=$(jq -r '.failedTestFiles[]' "$PREV_TEST_FILES_REPORT" | paste -sd "," -)

  # Collect array elements into a comma-separated string
  PREV_NOT_EXECUTED_TEST_FILES=$(jq -r '.notExecutedTestFiles[]' "$PREV_TEST_FILES_REPORT" | paste -sd "," -)

  # Export as environment variable
  export PREV_FAILED_TEST_FILES="$PREV_FAILED_TEST_FILES"
  export PREV_NOT_EXECUTED_TEST_FILES="$PREV_NOT_EXECUTED_TEST_FILES"
  
  run_functional_tests
}

#MAIN SCRIPT
TEST_FILES_REPORT="test-results/functional/testFilesReport.json"
PREV_TEST_FILES_REPORT="test-results/functional/prevTestFilesReport.json"

#Check if RUN_ALL_FUNCTIONAL_TESTS is set to true
if [ "$RUN_ALL_FUNCTIONAL_TESTS" = "true" ]; then
  echo "The label 'runAllFunctionalTests' exists on the PR."
  echo "Running all fucntional tests."
  run_functional_tests

#Check if testFilesReport.json exists and is non-empty
elif [ ! -f "$TEST_FILES_REPORT" ] || [ ! -s "$TEST_FILES_REPORT" ]; then
  echo "testFilesReport.json not found or is empty."
  run_functional_tests

#Check if latest current git commit is the not the same as git commit of test files report 
elif [ "$(jq -r 'if .gitCommitId == null then "__NULL__" else .gitCommitId end' "$TEST_FILES_REPORT")" != "$GIT_COMMIT" ]; then 
  echo "The gitCommitId does not match the current GIT_COMMIT.";
  run_functional_tests

#Check if ft_groups of test files report is the same as current ft_groups.
elif ! compare_ft_groups; then
  echo "ftGroups do NOT match PR_FT_GROUPS"
  run_functional_tests

else
  run_failed_not_executed_functional_tests
fi