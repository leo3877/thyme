#!/usr/bin/env bash
WORK_DIR=$(cd $(dirname $0);pwd)
set -e
export JAVA_HOME=~/.jenv/candidates/java/1.8.0_77/
alias mvn3=/home/admin/.jenv/candidates/maven/3.3.3/bin/mvn
shopt -s  expand_aliases
if [ -f ${WORK_DIR}/sub_proj.txt ];then
    while read sub_name
    do
        cd ${WORK_DIR}/${sub_name}
        mvn3 deploy -Dmaven.test.skip=true -e
    done < ${WORK_DIR}/sub_proj.txt
else
    mvn3 deploy -Dmaven.test.skip=true -e
fi