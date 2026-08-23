#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Unix Script
# ----------------------------------------------------------------------------
set -e

# Resolve project directory
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
BASE_DIR=`dirname "$PRG"`
BASE_DIR=`cd "$BASE_DIR" >/dev/null; pwd`

# Check for mvn command in path
if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
fi

# Fallback: check if local maven distribution exists in ~/.m2/wrapper
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.9-bin/apache-maven-3.9.9"
if [ -x "$MAVEN_HOME/bin/mvn" ]; then
    exec "$MAVEN_HOME/bin/mvn" "$@"
fi

echo "Maven not found in PATH or ~/.m2/wrapper."
echo "Please install Maven (e.g. 'sudo apt-get install maven' or download Apache Maven) to build/run via command line, or open this folder in IntelliJ IDEA / VS Code / Eclipse."
exit 1
