#!/bin/bash

main() {
  javac ./*.java
  jar --create --file minesweeper.jar --main-class Main ./*.class
  rm ./*.class
  cp minesweeper.jar ..
}

main

