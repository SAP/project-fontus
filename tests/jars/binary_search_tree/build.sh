#!/bin/bash

main() {
  javac ./*.java
  jar cfe bst.jar Main ./*.class
  rm ./*.class
  cp bst.jar ..
}

main

