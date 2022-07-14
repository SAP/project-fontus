#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/jul-config-0.1-SNAPSHOT-jar-with-dependencies.jar" ../jul-config.jar
  mvn clean
}

main

