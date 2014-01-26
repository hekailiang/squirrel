#!/bin/bash
#This script installs mysql (latest build)
#Install MYSQL Server
mysql_pass=1111
export DEBIAN_FRONTEND=noninteractive 
debconf-set-selections <<< 'mysql-server-5.1 mysql-server/root_password password '$mysql_pass''
debconf-set-selections <<< 'mysql-server-5.1 mysql-server/root_password_again password '$mysql_pass''
apt-get -y install mysql-server
#Configure Password and Settings for Remote Access
#cp /etc/mysql/my.cnf /etc/mysql/my.bak.cnf
#ip=`ifconfig eth0 | grep "inet addr"| cut -d ":" -f2 | cut -d " " -f1` ; sed -i "s/\(bind-address[\t ]*\)=.*/\1= $ip/" /etc/mysql/my.cnf
#mysql -uroot -e "UPDATE mysql.user SET Password=PASSWORD('"$mysql_pass"') WHERE User='root'; FLUSH PRIVILEGES;"
#sleep 10
#mysql -uroot -p$mysql_pass -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '"$mysql_pass"'; FLUSH PRIVILEGES;"
#Restart
#service mysql restart
echo "MySQL Installation and Configuration is Complete."

echo "drop database if exists sp20; create database sp20 default character set utf8" | mysql -uroot -p$mysql_pass
echo "database 'sp20' created."
