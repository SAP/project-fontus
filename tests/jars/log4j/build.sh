#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/log4j-0.1-SNAPSHOT-jar-with-dependencies.jar" ../log4j.jar
  mvn clean
}

main

