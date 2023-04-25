#!/bin/bash -e

PROJECT_NAME="medicinfo" # If your eb environment name follows format "abc-server-dev", just put "abc" part here.
VALID_ENVIRONMENTS=("dev" "acc" "prod") # Add new environments if required
ARGS="$@"

function check_arguments() {
  # Fail-fast
  BAD_ARGUMENTS=false
  for arg in "$@"; do
    if [[ ! " ${VALID_ENVIRONMENTS[@]} " =~ " ${arg} " ]]; then
      echo "$arg is not a valid environment."
      BAD_ARGUMENTS=true
    fi
  done

  if [ "$BAD_ARGUMENTS" = true ]; then
    echo "Possible parameters are: ${VALID_ENVIRONMENTS[*]}"
    exit 1
  fi
}

function check_eb() {
  if ! grep -q "artifact: build/dist" .elasticbeanstalk/config.yml; then
    printf "Configure deployment artifact in elasticbeanstalk config. See hosting docs.\n"
    exit 1
  fi
}

function build() {
  ./gradlew clean eb
}

function authenticate() {
  source activate-aws
  ./aws-login.sh
}

function deploy() {
  echo "Deploying to $1"
  eb deploy "$PROJECT_NAME-server-$1"
  git tag -f "$1"
  git push origin -f "$1"
}

check_arguments $ARGS
check_eb
build
authenticate
for arg in $ARGS; do
  deploy "$arg"
done
