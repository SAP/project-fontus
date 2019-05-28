#!/bin/bash

main() {
  javac ./*.java
  jar --create --file bst.jar --main-class Main ./*.class
  rm ./*.class
  cp bst.jar ..
}

main

