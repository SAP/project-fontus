#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/ical4j-0.1-SNAPSHOT-jar-with-dependencies.jar" ../ical4j.jar
  mvn clean
}

main

