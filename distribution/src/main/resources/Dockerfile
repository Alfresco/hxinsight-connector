# More infos about this image: https://github.com/Alfresco/alfresco-docker-base-java
FROM alfresco/alfresco-base-java:jre17-rockylinux9@sha256:c83b4a36e57158fd04a7d3ad41b38e8e340402430711f189e47266c12de351d3
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
