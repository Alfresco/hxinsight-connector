FROM @dist.alfresco.docker.image@:@dist.alfresco.version@

ARG TOMCAT_DIR=/usr/local/tomcat
ARG USERNAME=@dist.alfresco.docker.user@

USER root

# Copy Dockerfile to avoid an error if no JARs exist
COPY Dockerfile *.jar $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/

# Copy Dockerfile to avoid an error if no AMPs exist
COPY Dockerfile *.amp $TOMCAT_DIR/amps/
RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
              $TOMCAT_DIR/amps $TOMCAT_DIR/webapps/alfresco -directory -nobackup -force

# Copy Dockerfile to avoid an error if no license file exists
COPY Dockerfile licenses/*.* $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/alfresco/extension/license/

USER ${USERNAME}
