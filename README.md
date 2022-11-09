## Preparation

`sbt docker:publishLocal`

## Running service with DB

`docker-compose up`

Swagger is under: `http://localhost:8080/docs`

## Running the integration tests

`./runIt.sh`

WARN: integrations tests use the same instance of App