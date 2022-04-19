#!/bin/bash

set -e

cd ..
mvn package -DskipTests
docker build msa/tb-node/target -t jcbernack/tb-node:latest
docker build msa/transport/coap/target -t jcbernack/tb-coap-transport:latest
cd docker
docker-compose -f docker-compose-rocknitive.yml up -d
