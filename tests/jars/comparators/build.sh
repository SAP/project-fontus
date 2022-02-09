#!/bin/bash

main() {
  javac ./*.java
  jar cfe comparators.jar Main ./*.class
  rm ./*.class
  cp comparators.jar ..
}

main

