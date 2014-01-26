#!/bin/bash
mkdir ~/.m2
cd ~/.m2

cat > ~/.m2/settings.xml << "EOF"
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" 
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
  </servers>
  <mirrors>
    <mirror>
      <id>ebao</id>
      <name>eBaoTech Maven Repository</name>
      <url>http://172.25.32.35/artifactory/repo</url>
      <mirrorOf>*</mirrorOf>
     </mirror>
  </mirrors>
  <profiles>
    <profile>
      <id>ebao</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <repositories>
        <repository>
          <id>ebao</id>
          <name>eBaoTech Maven Repository</name>
          <url>http://172.25.32.35/artifactory/repo</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>ebao</id>
          <name>eBaoTech Maven Repository</name>
          <url>http://172.25.32.35/artifactory/repo</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles> 
</settings>
EOF
