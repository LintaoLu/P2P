#!/bin/sh

echo "start compiling..."
javac -d . src/main/java/Common/*.java
javac -d . src/main/java/Client/*.java
javac -d . src/test/java/*.java
echo "done!"
