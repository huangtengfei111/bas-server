#!/bin/sh
mvn clean compile exec:java -Dexec.mainClass="app.util.ShiroPasswdGen" -Dexec.args="$1 $2"