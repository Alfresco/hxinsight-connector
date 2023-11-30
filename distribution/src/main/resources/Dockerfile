FROM alfresco/alfresco-base-java:jre17-rockylinux8-202302221525
ENV PORT 8080
EXPOSE 8080
ARG JAR_FILE=*-app.jar
WORKDIR /opt
COPY ${JAR_FILE} app.jar
COPY THIRD-PARTY.txt THIRD-PARTY.txt
COPY README.md README.md
COPY licenses licenses
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
