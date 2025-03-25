#!/bin/bash

main() {
  javac ./*.java
  jar cfe inheritance.jar Main ./*.class
  rm ./*.class
  cp inheritance.jar ..
}

main
