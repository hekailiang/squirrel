#!/bin/bash
export SCRIPT_HOME=~/myscripts
mkdir -p $SCRIPT_HOME
echo "==> Download Scripts"
sleep 2
wget https://github.com/hekailiang/mybash/blob/master/mywork/update-apt-get.sh -P $SCRIPT_HOME
wget https://github.com/hekailiang/mybash/blob/master/mywork/init_sys_env.sh -P $SCRIPT_HOME
wget https://github.com/hekailiang/mybash/blob/master/mywork/setup_eclipse.sh -P $SCRIPT_HOME
wget https://github.com/hekailiang/mybash/blob/master/mywork/install_mysql.sh -P $SCRIPT_HOME
wget https://github.com/hekailiang/mybash/blob/master/mywork/install_mongodb.sh -P $SCRIPT_HOME
wget https://github.com/hekailiang/mybash/blob/master/mywork/setup_maven_repo.sh -P $SCRIPT_HOME
wget https://github.com/hekailiang/mybash/blob/master/mywork/build_deploy_project.sh -P $SCRIPT_HOME
wget https://github.com/hekailiang/mybash/blob/master/mywork/build_project.sh -P $SCRIPT_HOME
wget https://github.com/hekailiang/mybash/blob/master/mywork/deploy_project.sh -P $SCRIPT_HOME

chmod 755 /$SCRIPT_HOME/*.sh

cd $SCRIPT_HOME
echo "==> Update apt-get Source"
sleep 2
sudo ./update-apt-get.sh

echo "==> Initial System environment"
sleep 2
sudo ./init_sys_env.sh

echo "==> Setup Eclipse"
sleep 2
sudo ./setup_eclipse.sh

echo "==> Install MySql Server"
sleep 2
sudo ./install_mysql.sh

echo "==> Install MongoDB"
sleep 2
sudo ./install_mongodb.sh

echo "==> Setup Maven Repository"
sleep 2
./setup_maven_repo.sh
