#!/bin/bash

main() {
  javac ./*.java
  jar --create --file sit.jar --main-class Main ./*.class
  rm ./*.class
  cp sit.jar ..
}

main

