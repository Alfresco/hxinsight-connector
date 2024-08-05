# ACTUAL IMAGE
# More infos about this image: https://github.com/Alfresco/alfresco-docker-base-java
# jre17-rockylinux9 (29/07/2023)
FROM quay.io/alfresco/alfresco-base-java:jre17-rockylinux9@sha256:e8b1f76172205d316fc25c7c64b517c20b69a73d8f6377c0a80b170e0aa4414c
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
