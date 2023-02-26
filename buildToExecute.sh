#!/bin/bash

cd $(dirname $0)
WORKDIR=$PWD

if [ -z "${TELLUR_HOME}" ]; then
  echo "TELLUR_HOME is not set."
  exit 0
fi

mkdir "${TELLUR_HOME}"
mkdir "${TELLUR_HOME}"/config
mvn clean install

cp target/tellur-1.0.0.jar "${TELLUR_HOME}"
cp src/main/resources/application.properties "${TELLUR_HOME}"/config
cp -r demo/* "${TELLUR_HOME}"

echo "------ DONE"
#java -jar "${TELLUR_HOME}"/tellur-1.0.0.jar anConsealedArgParam "file:${TELLUR_HOME}/demo.html"
