#!/bin/bash
set -e
export JAVA_HOME=~/.jenv/candidates/java/1.8.0_77/
alias mvn3=/home/admin/.jenv/candidates/maven/3.3.3/bin/mvn
shopt -s  expand_aliases
mvn3 clean package install -Dmaven.test.skip=true