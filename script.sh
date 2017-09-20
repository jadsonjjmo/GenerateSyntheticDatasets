#!/usr/bin/env bash

mvn clean compile assembly:single

java -jar target/GenerateSyntheticDataset-1.0-SNAPSHOT-jar-with-dependencies.jar config.json
