#!/bin/sh
export JAVA_HOME="${JAVA_HOME:-/c/Program Files/Java/jdk-17}"
export PATH="$JAVA_HOME/bin:/c/Program Files/apache-maven-3.9.15/bin:$PATH"
mvn "$@"
