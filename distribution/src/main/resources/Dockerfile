# ACTUAL IMAGE
# More infos about this image: https://github.com/Alfresco/alfresco-docker-base-java
# jre17-rockylinux8 (29/07/2024)
FROM alfresco/alfresco-base-java@sha256:18ac53e9a267dc4389eaff80ad1f5b8deaf53da09c234de5fcc6e05ebcd4a77c
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
