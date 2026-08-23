#!/bin/bash
export JAVA_HOME=/home/roopesh-baliga/.gemini/antigravity/scratch/tools/jdk-21.0.4+7
export PATH=$JAVA_HOME/bin:/home/roopesh-baliga/.gemini/antigravity/scratch/tools/apache-maven-3.9.9/bin:$PATH
mvn test
