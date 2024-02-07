#!/bin/bash

main() {
  javac ./*.java
  jar cfe minesweeper.jar Main ./*.class
  rm ./*.class
  cp minesweeper.jar ..
}

main

