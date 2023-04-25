#!/bin/bash -e

# this helps us recognize leftover containers, in case of script failures
POSTGRES_CONTAINER_NAME=medicinfo-server-postgres-ci
# this cache contains gradle + all dependencies, for faster build times
GRADLE_CACHE_VOLUME_NAME=medicinfo-server-gradle-cache
# the mount point inside the container for the gradle cache volume
CONTAINER_CACHE_PATH=/root/.gradle

# always stop postgres after termination
trap cleanup EXIT
function cleanup() {
  docker stop $POSTGRES_CONTAINER_NAME || true
}

# Execute a gradle command, in docker, using a bind mount for the repo on /code
# arg1: gradle args, as 1 string
function docker_gradle() {
  docker run \
    -v $GRADLE_CACHE_VOLUME_NAME:$CONTAINER_CACHE_PATH \
    -v $(pwd):/code \
    -w /code \
    --rm \
    openjdk:11 \
    ./gradlew --no-daemon $1
}

echo
echo "Building..."
docker_gradle assemble

# Start starting postgres, so we don't have to wait for it after unit tests
# stop any running containers, if needed
EXISTING_IDS=$(docker ps -q --filter name=${POSTGRES_CONTAINER_NAME})
if [[ "$EXISTING_IDS" != "" ]]; then
  echo "Stopping existing postgres: $EXISTING_IDS"
  docker stop $EXISTING_IDS || true
fi
CONTAINER_ID=$(docker run -d --rm --name $POSTGRES_CONTAINER_NAME -e POSTGRES_HOST_AUTH_METHOD=trust postgres:12.7)
echo
echo "Postgres container ID is $CONTAINER_ID"

echo
echo "Running unit tests..."
docker_gradle check

### Integration tests

echo
echo "Running integration tests"
# wait for postgres startup
for i in $(seq 1 5); do
  if ! docker exec -i $CONTAINER_ID /usr/bin/psql -U postgres -c "SELECT 1" &>/dev/null; then
    echo "Waiting for Postgres..."
    sleep 1
  else
    echo "Postgres ready"
    break
  fi
done

POSTGRES_HOST=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $CONTAINER_ID)
echo "Started Postgres at $POSTGRES_HOST"
# create config file in integrationtest folder, which is on the default load path for
# spring configuration files (file:./)
cat <<EOF >testutil/src/main/resources/application-test.local.properties
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://$POSTGRES_HOST:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=admin
EOF

docker_gradle integrationTest

# trap will cleanup postgres container
