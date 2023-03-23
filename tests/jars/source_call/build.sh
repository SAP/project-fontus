#!/bin/bash

main() {
  javac ./*.java
  jar cfe source_call.jar Main ./*.class
  #rm ./*.class
  #cp bst.jar ..
}

main

