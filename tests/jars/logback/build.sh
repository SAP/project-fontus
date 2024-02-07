#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/logback-0.1-SNAPSHOT-jar-with-dependencies.jar" ../logback.jar
  mvn clean
}

main

