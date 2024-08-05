# ACTUAL IMAGE
# More infos about this image: https://github.com/Alfresco/alfresco-docker-base-java
# jre17-rockylinux9 (19/12/2023)
FROM alfresco/alfresco-base-java:jre17-rockylinux9-202312191347@sha256:45801565eb6fc065870d4deb33fadf8845f18878c3196032c753883ccca64f09
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
