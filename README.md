./# Url Shortener

Tiny service to make a short url from incoming one. Shortened URL can be used for redirect to original one.

See api documentation: localhost:8080/swagger-ui

## Build and run

- Local
```
./gradlew clean build

java -jar build/libs/UrlShortner-all.jar
```


- Docker
```
export DOCKER_REGISTRY=<val>

./gradlew jibDockerBuild

docker run -it  -p8080:8080 -eDB_URL=<value> -eDB_HOST=<value> -eDB_PASS=<value> <registry_name>/urlshortner:<app:version>
```

Note: additionaly export DOCKER_USERNAME and DOCKER_PWD variables for non-local registry

- Docker-compose
```
export DOCKER_REGISTRY=<val>

./gradlew jibDockerBuild

docker-compose up
```