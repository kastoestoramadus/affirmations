#!/bin/sh

docker-compose up

sbt it:test

docker-compose down