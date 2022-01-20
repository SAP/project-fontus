#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/unbescape.jar" ../unbescape.jar
  mvn clean
}

main

