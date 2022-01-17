#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/super-interfaces-0.1-SNAPSHOT-jar-with-dependencies.jar" ../super-interfaces.jar
  mvn clean
}

main

