#!/bin/bash

main() {
  javac ./*.java
  jar cfe annotation_test.jar Main ./*.class
  rm ./*.class
  cp annotation_test.jar ..
}

main

