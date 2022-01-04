#!/bin/bash

main() {
  javac ./*.java
  jar cfe reflected_constructor.jar Main ./*.class
  rm ./*.class
  cp reflected_constructor.jar ..
}

main

