#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/xalan-0.1-SNAPSHOT-jar-with-dependencies.jar" ../xalan.jar
  mvn clean
}

main

