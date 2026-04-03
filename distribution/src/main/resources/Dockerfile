# More infos about this image: https://github.com/Alfresco/alfresco-docker-base-java
FROM alfresco/alfresco-base-java:jre21-rockylinux9@sha256:f577509bc07fe51ed87dd6e75ef14f41c78671b13f549b3d287311428e629bc3
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
