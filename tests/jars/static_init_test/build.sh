#!/bin/bash

main() {
  javac ./*.java
  jar cfe sit.jar Main ./*.class
  rm ./*.class
  cp sit.jar ..
}

main

