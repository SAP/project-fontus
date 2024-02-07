#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/objenesis-0.1-SNAPSHOT-jar-with-dependencies.jar" ../objenesis.jar
  mvn clean
}

main

