#!/bin/bash

main() {
  javac ./*.java
  jar cfe str_array_clone.jar Main ./*.class
  rm ./*.class
  cp str_array_clone.jar ..
}

main

