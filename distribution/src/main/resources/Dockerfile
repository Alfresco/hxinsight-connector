# ACTUAL IMAGE
# More infos about this image: https://github.com/Alfresco/alfresco-docker-base-java
# jre17-rockylinux9 (29/07/2023)
FROM quay.io/alfresco/alfresco-base-java:jre17-rockylinux9@sha256:8dca9445141dd07b9a6b7a9962f0505546b2ed041b4d5cc785cc9c2d39eeda19
ENV PORT=8080
EXPOSE 8080
ARG JAR_FILE=*-app.jar
WORKDIR /opt
COPY $JAR_FILE app.jar
COPY THIRD-PARTY.txt THIRD-PARTY.txt
COPY README.md README.md
COPY licenses licenses
SHELL ["/bin/bash", "-c"]
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
