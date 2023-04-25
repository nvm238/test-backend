#!/bin/bash -e

CONTAINER_NAME="medicinfo-postgres-codegen"
POSTGRES_PASSWORD="admin"

function stop_postgres {
  # Kill already running containers
  IDS=$(docker ps -q --filter name=$CONTAINER_NAME --format="{{.ID}}")
  echo "Running ids are $IDS"
  if [[ "$IDS" != "" ]]; then
    echo "Stopping existing Postgres"
    docker stop $IDS
  fi
}

stop_postgres

# Run postgres container
echo "Starting Postgres"
CONTAINER_ID=$(docker run -d --rm --name $CONTAINER_NAME \
  -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD postgres:12.7)

# Get ipv4 address of container
POSTGRES_HOST=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' \
  $CONTAINER_ID)

echo "Postgres running on $POSTGRES_HOST"

# give postgres time to start up
sleep 2

echo "Generating jOOQ code"
rm -rf src/generated/java

# set variable for generation script
export POSTGRES_DATABASE="postgres"
export POSTGRES_HOST
export POSTGRES_PASSWORD
export POSTGRES_USER="postgres"

(cd ../; ./gradlew dbschema:generate)

echo "Shutting down Postgres"
stop_postgres
