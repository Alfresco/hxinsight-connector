FROM @dist.alfresco.docker.image@:@dist.alfresco.version@

ARG TOMCAT_DIR=/usr/local/tomcat
ARG USERNAME=@dist.alfresco.docker.user@

USER root

# Copy Dockerfile to avoid an error if no JARs exist
COPY Dockerfile *.jar $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/

# Copy Dockerfile to avoid an error if no license file exists
COPY Dockerfile licenses/*.* $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/alfresco/extension/license/

RUN chown -R -h $USERNAME $TOMCAT_DIR

# Install the required JRE version if not already present
ARG JAVA_VERSION=""
RUN CURRENT_MAJOR=$(java -version 2>&1 | head -1 | sed 's/.*"\([0-9]*\).*/\1/') && \
    if [[ "$JAVA_VERSION" == "21" && "$CURRENT_MAJOR" != "21" ]]; then \
      ARCH=$(uname -m | sed s/86_//); \
      JAVA_RELEASE=21.0.7_6; \
      curl -fsLo java.tar.gz https://github.com/adoptium/temurin${JAVA_VERSION}-binaries/releases/download/jdk-${JAVA_RELEASE/_/+}/OpenJDK${JAVA_VERSION}U-jre_${ARCH}_linux_hotspot_${JAVA_RELEASE}.tar.gz && \
      tar xvfz java.tar.gz && \
      mv jdk-* /usr/lib/jvm/temurin-21-jdk && \
      update-alternatives --install /usr/bin/java java /usr/lib/jvm/temurin-21-jdk/bin/java 1 && \
      update-alternatives --remove java $(update-alternatives --display java | head -2 | tail -1 | cut -d " " -f6); \
    elif [[ "$JAVA_VERSION" == "11" && "$CURRENT_MAJOR" != "11" ]]; then \
      ARCH=$(uname -m | sed s/86_//); \
      JAVA_RELEASE=11.0.24_8; \
      curl -fsLo java.tar.gz https://github.com/adoptium/temurin${JAVA_VERSION}-binaries/releases/download/jdk-${JAVA_RELEASE/_/+}/OpenJDK${JAVA_VERSION}U-jre_${ARCH}_linux_hotspot_${JAVA_RELEASE}.tar.gz && \
      tar xvfz java.tar.gz && \
      mv jdk-* /usr/lib/jvm/temurin-11-jdk && \
      update-alternatives --install /usr/bin/java java /usr/lib/jvm/temurin-11-jdk/bin/java 1 && \
      update-alternatives --remove java $(update-alternatives --display java | head -2 | tail -1 | cut -d " " -f6); \
    fi

USER $USERNAME

COPY java-switching-entrypoint.sh /
ENTRYPOINT ["/java-switching-entrypoint.sh"]
CMD ["catalina.sh", "run", "-security"]
