#wget -qO- https://get.docker.com/ | sh
#sudo usermod -aG docker samuel
#sudo apt-get install apparmor
#sudo docker build -t server .
#sudo docker run -p 80:9000 -i -t server
#sudo docker ps
#detach shortcut: ctrl+p+q
#sudo docker attach 3998ab616c0f
#sudo docker stop 399
FROM ubuntu:14.04
RUN apt-get -y update
RUN apt-get -y install unzip python-software-properties software-properties-common git
RUN echo ttf-mscorefonts-installer msttcorefonts/accepted-mscorefonts-eula select true | debconf-set-selections
RUN apt-get install ttf-mscorefonts-installer --quiet
RUN add-apt-repository -y ppa:webupd8team/java
RUN apt-get -y update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
RUN apt-get -y install oracle-java8-installer
RUN wget http://downloads.typesafe.com/typesafe-activator/1.3.2/typesafe-activator-1.3.2.zip
RUN unzip typesafe-activator-1.3.2.zip -d / && rm typesafe-activator-1.3.2.zip && chmod a+x /activator-1.3.2/activator
ENV PATH $PATH:/activator-1.3.2
RUN git clone https://github.com/godchaser/PlaySong.git
WORKDIR "/PlaySong"
EXPOSE 9000 8888
CMD ["activator", "run"]
