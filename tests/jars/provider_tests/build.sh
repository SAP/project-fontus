#!/bin/bash

main() {
  javac ./*.java
  jar cfe provider_tests.jar Main ./*.class
  rm ./*.class
  cp provider_tests.jar ..
}

main

