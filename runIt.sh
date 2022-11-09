#!/bin/sh

docker-compose up

sbt "project it; test"

docker-compose down