#!/bin/bash

# find * -type d -maxdepth 0 -exec sh -c "cd {} && mvn clean package && docker build -t jackvasc/{}:1.0.0 . && docker push jackvasc/{}:1.0.0" \;
# find * -type d -maxdepth 0 -exec sh -c "cd {} && docker build -t jackvasc/{}:1.0.0 . && docker push jackvasc/{}:1.0.0" \;

cd /Users/jacksoncastro/git/blueperf-speedup || exit

REPOSITORY='jackvasc'
TAG='1.0.0'
PROJECTS=(
    acmeair-mainservice-java
    acmeair-authservice-java
    acmeair-customerservice-java
    acmeair-bookingservice-java
    acmeair-flightservice-java
)
for PROJECT in "${PROJECTS[@]}"
do
    pushd "${PROJECT}" || exit
    IMAGE="${REPOSITORY}/${PROJECT}:${TAG}"
    mvn clean package
    docker build -t "$IMAGE" .
    docker push "$IMAGE"
    popd || exit
done
