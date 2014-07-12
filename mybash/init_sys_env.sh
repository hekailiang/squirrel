#!/bin/bash
echo "==> Initial System Environment"

export DEBIAN_FRONTEND=noninteractive

echo "==> Install OpenJDK6"
apt-get install openjdk-6-jdk

echo "==> Install Maven2"
apt-get install maven2

echo "==> Install Git"
apt-get install git

echo "==> Install Subversion"
apt-get install subversion

echo "==> Install Tomcat7"
apt-get --force-yes --yes remove tomcat*
apt-get --force-yes --yes install tomcat7
ln -s /var/lib/tomcat7/webapps ~/mywebapps

echo "==> Install Vim"
apt-get --force-yes --yes install vim

echo "==> Install CURL"
apt-get install curl

echo "==> Install SSH Server"
# apt-get --force-yes --yes remove openssh-server*
apt-get --force-yes --yes install openssh-server

echo "==> Initial System Done!"

