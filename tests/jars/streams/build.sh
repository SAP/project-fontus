#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/streams-0.1-SNAPSHOT-jar-with-dependencies.jar" ../streams.jar
  mvn clean
}

main

