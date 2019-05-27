#!/bin/bash

main() {
  javac ./*.java
  jar --create --file tictactoe.jar --main-class Main ./*.class
  rm ./*.class
  cp tictactoe.jar ..
}

main

