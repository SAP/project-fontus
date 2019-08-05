#!/bin/bash

main() {
  javac ./*.java
  jar cfe media_types.jar Main ./*.class
  rm ./*.class
  cp media_types.jar ..
}

main
