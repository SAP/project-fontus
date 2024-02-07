#!/bin/bash

main() {
  javac ./*.java
  jar cfe deserialize.jar Main ./*.class
  rm -f ./*.class
  rm -f ./*.dat
  java -jar deserialize.jar init
  cp deserialize.jar ..
  cp deserialized.dat ..
}

main

