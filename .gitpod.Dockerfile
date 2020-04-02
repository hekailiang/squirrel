FROM gitpod/workspace-mysql
                    
USER gitpod

# Install custom tools, runtime, etc. using apt-get
# For example, the command below would install "bastet" - a command line tetris clone:
#
RUN sudo apt-get -q update
RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh"
RUN bash -c "echo y | sdk install java 8.0.242-zulu"
#
# More information: https://www.gitpod.io/docs/config-docker/
