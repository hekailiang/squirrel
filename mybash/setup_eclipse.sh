#!/bin/bash
echo "==> Setup Eclipse"
mkdir -p ~/project/downloads

echo "==> Download Eclipse"
#wget -c http://mirror.hust.edu.cn/eclipse/technology/epp/downloads/release/kepler/SR1/\
wget -c http://download.actuatechina.com/eclipse/technology/epp/downloads/release/kepler/SR1/\
eclipse-standard-kepler-SR1-linux-gtk-x86_64.tar.gz \
-O ~/project/downloads/eclipse-standard-kepler-linux.tar.gz

cd ~/project/downloads
tar -zxvf eclips*.tar.gz

mv eclipse /opt/

chown -R root:root /opt/eclipse

cat > /usr/share/applications/eclipse.desktop << "EOF" 
[Desktop Entry]
Version=1.0
Encoding=UTF-8
Name=Eclipse Platform
Comment=Eclipse IDE
Exec=eclipse
Icon=/opt/eclipse/icon.xpm
Terminal=false
Type=Application
Categories=GNOME;Application;Development;
StartupNotify=true
EOF

ln -s /opt/eclipse/eclipse /usr/local/bin

echo "==> Install Eclipse Plugins(subversion). This may takes a while, please be patient..."
eclipse \
-clean -purgeHistory \
-application org.eclipse.equinox.p2.director \
-noSplash \
-repository \
http://download.eclipse.org/releases/kepler/,\
http://download.eclipse.org/webtools/updates,\
http://download.eclipse.org/eclipse/updates/4.3,\
http://subclipse.tigris.org/update_1.6.x \
-installIUs \
Subclipse \
-vmargs -Declipse.p2.mirrors=true -Djava.net.preferIPv4Stack=true

echo "==> Install Eclipse Plugins(junit, runjettyrun, eclemma). This may takes a while, please be patient..."
eclipse \
-clean -purgeHistory \
-application org.eclipse.equinox.p2.director \
-noSplash \
-repository \
http://download.eclipse.org/releases/kepler/,\
http://download.eclipse.org/webtools/updates,\
http://download.eclipse.org/eclipse/updates/4.3,\
http://run-jetty-run.googlecode.com/svn/trunk/updatesite/,\
http://update.eclemma.org/ \
-installIUs \
org.junit,\
runjettyrun_feature.feature.group,\
com.mountainminds.eclemma.feature.feature.group \
-vmargs -Declipse.p2.mirrors=true -Djava.net.preferIPv4Stack=true

echo "==> Install Eclipse Plugins(m2e, ajdt). This may takes a while, please be patient..."
eclipse \
-clean -purgeHistory \
-application org.eclipse.equinox.p2.director \
-noSplash \
-repository \
http://download.eclipse.org/releases/kepler/,\
http://download.eclipse.org/webtools/updates,\
http://download.eclipse.org/eclipse/updates/4.3,\
http://download.eclipse.org/tools/ajdt/43/update/,\
http://download.eclipse.org/technology/m2e/releases \
-installIUs \
org.eclipse.m2e.feature.feature.group,\
org.eclipse.ajdt.feature.group \
-vmargs -Declipse.p2.mirrors=true -Djava.net.preferIPv4Stack=true

echo "==> Setup Eclipse Done!"

