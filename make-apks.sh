#!/bin/bash

apps="treebolicOwlServices treebolicFilesServices treebolicWordNetServices"

for a in $apps; do
	echo "*** $a"
	./gradlew :${a}:assembleRelease
done
