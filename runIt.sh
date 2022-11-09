#!/usr/bin/env bash

docker-compose create
docker-compose up -d

sbt "project it; test"

docker-compose down