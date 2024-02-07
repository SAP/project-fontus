#!/bin/bash

main() {
  javac ./*.java
  jar cfe tictactoe.jar Main ./*.class
  rm ./*.class
  cp tictactoe.jar ..
}

main

