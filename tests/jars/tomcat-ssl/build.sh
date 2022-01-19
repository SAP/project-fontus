#!/bin/bash
main() {
  mvn package
  cp "$(pwd)/target/tomcat-ssl.jar" ../tomcatssl.jar
  mvn clean
}

main

