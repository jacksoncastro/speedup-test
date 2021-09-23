docker build -t jackvasc/acmeair-flightservice-java:no-cached .
docker push jackvasc/acmeair-flightservice-java:no-cached

docker build -t jackvasc/acmeair-flightservice-java:cached .
docker push jackvasc/acmeair-flightservice-java:cached
