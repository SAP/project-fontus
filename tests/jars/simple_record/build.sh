#!/bin/bash

main() {
  javac ./*.java
  jar cfe simple_record.jar Main ./*.class
  rm ./*.class
  cp simple_record.jar ..
}

main
