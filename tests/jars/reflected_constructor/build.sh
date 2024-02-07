#!/bin/bash

main() {
  mvn package
  cp "$(pwd)/target/reflected-constructor-0.1-SNAPSHOT-jar-with-dependencies.jar" ../reflected_constructor.jar
  mvn clean
}

main

