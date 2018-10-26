FROM openjdk:8-jre


# sbt
ENV SBT_URL=https://dl.bintray.com/sbt/native-packages/sbt
ENV SBT_VERSION 0.13.15
ENV INSTALL_DIR /usr/local
ENV SBT_HOME /usr/local/sbt
ENV PATH ${PATH}:${SBT_HOME}/bin

COPY svc /svc

CMD /svc/bin/start -Dhttps.port=9443 -Dplay.crypto.secret=secret
#CMD sleep infinity

#FROM openjdk:8-jre
#
#RUN \
#  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
#  dpkg -i sbt-$SBT_VERSION.deb && \
#  rm sbt-$SBT_VERSION.deb && \
#  apt-get update && \
#  apt-get install sbt && \
#  sbt sbtVersion
#
#WORKDIR /HelloWorld
#ADD . /HelloWorld
#CMD sbt run

#COPY svc /svc
#EXPOSE 9000 9443
#CMD /svc/bin/start -Dhttps.port=9443 -Dplay.crypto.secret=secret

#docker build -t redisratingstream .
