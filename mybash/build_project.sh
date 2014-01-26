#!/bin/bash
echo "==> Build Project"
export MAVEN_OPTS=-"Xms256m -Xmx1024m -XX:MaxPermSize=256m"
export PROJECT_BASE=~/project/sp20/SP20_Main
export PROJECT_HOME=$PROJECT_BASE/trunk/salesplatform

mkdir -p $PROJECT_BASE
cd $PROJECT_BASE
echo "==> svn co --force https://svn03.ebaotech.com/svn/GSstargate/SP20_Main/trunk"
svn co --force https://svn03.ebaotech.com/svn/GSstargate/SP20_Main/trunk

sleep 5

cd $PROJECT_HOME
echo "==> mvn clean install -PInitial-DB"
mvn clean install -PInitial-DB
