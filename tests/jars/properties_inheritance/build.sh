#!/bin/bash

main() {
  javac ./*.java
  jar cfe properties_inheritance.jar Main ./*.class
  rm ./*.class
  cp properties_inheritance.jar ..
}

main