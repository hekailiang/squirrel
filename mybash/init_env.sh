#!/bin/bash
export SCRIPT_HOME=~/myscripts
mkdir -p $SCRIPT_HOME
echo "==> Download Scripts"
sleep 2
mysite=hekailiang.github.io/squirrel/mybash
wget http://$mysite/update-apt-get.sh -P $SCRIPT_HOME
wget http://$mysite/init_sys_env.sh -P $SCRIPT_HOME
wget http://$mysite/setup_eclipse.sh -P $SCRIPT_HOME
wget http://$mysite/install_mysql.sh -P $SCRIPT_HOME
wget http://$mysite/install_mongodb.sh -P $SCRIPT_HOME
wget http://$mysite/setup_maven_repo.sh -P $SCRIPT_HOME
wget http://$mysite/build_deploy_project.sh -P $SCRIPT_HOME
wget http://$mysite/build_project.sh -P $SCRIPT_HOME
wget http://$mysite/deploy_project.sh -P $SCRIPT_HOME
sleep 4
chmod 755 /$SCRIPT_HOME/*.sh

cd $SCRIPT_HOME
echo "==> Update apt-get Source"
sleep 2
sudo ./update-apt-get.sh

echo "==> Initial System environment"
sleep 2
sudo ./init_sys_env.sh

#echo "==> Setup Eclipse"
#sleep 2
#sudo ./setup_eclipse.sh

echo "==> Install MySql Server"
sleep 2
sudo ./install_mysql.sh

echo "==> Install MongoDB"
sleep 2
sudo ./install_mongodb.sh

echo "==> Setup Maven Repository"
sleep 2
./setup_maven_repo.sh
