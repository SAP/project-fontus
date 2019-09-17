#!/bin/bash

main() {
  javac ./*.java
  jar cfe sbasciibytes.jar Main ./*.class
  rm ./*.class
  cp sbasciibytes.jar ..
}

main
