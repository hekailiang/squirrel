#!/bin/bash
echo "==> Deploy Project"
export PROJECT_BASE=~/project/sp20/SP20_Main
export PROJECT_HOME=$PROJECT_BASE/trunk/salesplatform

echo "==> cp $PROJECT_HOME/dashboard-demo-master/target/sp20_web.war ~/mywebapps/"
sudo /bin/cp $PROJECT_HOME/dashboard-demo-master/target/sp20_web.war ~/mywebapps/

if [ -d ~/mywebapps/sp20_web ]; then
sudo rm -rf ~/mywebapps/sp20_web
fi

echo "==> cp $PROJECT_HOME/pa_web/target/pa_web.war ~/mywebapps/"
sudo /bin/cp $PROJECT_HOME/pa_web/target/pa_web.war ~/mywebapps/

if [ -d ~/mywebapps/pa_web ]; then
sudo rm -rf ~/mywebapps/pa_web
fi

echo "==> tomcat7 restart"
sudo /etc/init.d/tomcat7 restart

firefox localhost:8080/sp20_web/product &