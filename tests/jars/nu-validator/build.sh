#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/nu-validator-0.1-SNAPSHOT-jar-with-dependencies.jar" ../nu-validator.jar
  mvn clean
}

main

