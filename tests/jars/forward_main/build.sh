#!/bin/bash

main() {
  javac ./*.java
  jar cfe forward_main.jar Main ./*.class
  rm ./*.class
  cp forward_main.jar ..
}

main

