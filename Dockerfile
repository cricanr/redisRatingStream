FROM openjdk:8-jre

# sbt
ENV SBT_URL=https://dl.bintray.com/sbt/native-packages/sbt
ENV SBT_VERSION 0.13.15
ENV INSTALL_DIR /usr/local
ENV SBT_HOME /usr/local/sbt
ENV PATH ${PATH}:${SBT_HOME}/bin

COPY svc /svc

CMD /svc/bin/start -Dhttps.port=9443 -Dplay.crypto.secret=secret