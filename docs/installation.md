# Installation Guide

This guide covers the different ways to deploy the Content Intelligence Connector components.

See [Compatibility](compatibility.md) for supported Alfresco versions and requirements.

## Component Deployment Matrix

| Component | JAR | Docker | Kubernetes | Config |
|-----------|-----|--------|------------|--------|
| Live Ingester | ✅ | ✅ | ✅ | [→](live-ingester.md) |
| Bulk Ingester | ✅ | ✅ | ✅ | [→](bulk-ingester.md) |
| Knowledge Discovery JAR Module | ✅ | ✅* | N/A | [→](hxinsight-extension.md) |
| Nucleus User Sync | ✅ | ✅ | ✅ | [→](nucleus-sync.md) |
| Prediction Applier | ✅ | ✅ | ✅ | [→](prediction-applier.md) |

*The Docker image bundles the module with Alfresco—it's not a standalone container.*

---

## Standalone JAR Deployment

### Prerequisites
- Java 17 or later
- Access to Alfresco Repository
- Access to ActiveMQ
- Access to HX Insight APIs

### Running the JAR

```bash
# Live Ingester
java -jar alfresco-hxinsight-connector-live-ingester-*.jar \
  --spring.activemq.broker-url=nio://activemq:61616 \
  --alfresco.repository.discovery-endpoint=http://alfresco:8080/alfresco/api/discovery \
  --auth.providers.hyland-experience.client-id=<client-id> \
  --auth.providers.hyland-experience.client-secret=<client-secret>

# Bulk Ingester
java -jar alfresco-hxinsight-connector-bulk-ingester-*.jar \
  --spring.datasource.url=jdbc:postgresql://postgres:5432/alfresco \
  --spring.activemq.broker-url=nio://activemq:61616

# Prediction Applier
java -jar alfresco-hxinsight-connector-prediction-applier-*.jar \
  --spring.activemq.broker-url=nio://activemq:61616 \
  --alfresco.repository.base-url=http://alfresco:8080/alfresco
```

### Using Environment Variables

All configuration properties can be set via environment variables:

```bash
export SPRING_ACTIVEMQ_BROKERURL=nio://activemq:61616
export AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID=my-client-id
export AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET=my-secret

java -jar alfresco-hxinsight-connector-live-ingester-*.jar
```

### Using an External Config File

```bash
java -jar alfresco-hxinsight-connector-live-ingester-*.jar \
  --spring.config.location=file:/path/to/application.yml
```

---

## Docker Deployment

### Available Images

```
quay.io/alfresco/alfresco-hxinsight-connector-live-ingester:<version>
quay.io/alfresco/alfresco-hxinsight-connector-bulk-ingester:<version>
quay.io/alfresco/alfresco-hxinsight-connector-prediction-applier:<version>
quay.io/alfresco/alfresco-hxinsight-connector-nucleus-sync:<version>
```

### Running with Docker

```bash
docker run -d \
  --name live-ingester \
  -e APPLICATION_SOURCEID=<source-id> \
  -e SPRING_ACTIVEMQ_BROKERURL=nio://activemq:61616 \
  -e ALFRESCO_REPOSITORY_BASEURL=http://alfresco:8080/alfresco \
  -e ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL=http://shared-file-store:8099 \
  -e HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL=https://hxinsight.hyland.com \
  -e AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID=<client-id> \
  -e AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET=<client-secret> \
  -e AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI=https://auth.hyland.com/oauth/token \
  -e AUTH_PROVIDERS_HYLANDEXPERIENCE_ENVIRONMENTKEY=<env-key> \
  quay.io/alfresco/alfresco-hxinsight-connector-live-ingester:<version>
```

The `APPLICATION_SOURCEID` is generated when you register your Alfresco instance in CIC for Knowledge Discovery. In some systems (Nucleus) then it is referred to as "system id", where as Knowledge Discovery calls it a "source id."

### Docker Compose

A complete docker-compose environment for testing purposes is provided in the repository at `distribution/src/main/resources/docker-compose/`:

```bash
cd distribution/src/main/resources/docker-compose
docker compose up -d
```

See the `docker-compose.yml` in that directory for a full example configuration.

---

## Kubernetes Deployment

### Resource Requirements

| Component | Memory Request | Memory Limit |
|-----------|---------------|--------------|
| Live Ingester | 256Mi | 512Mi |
| Bulk Ingester | 256Mi | 512Mi |
| Prediction Applier | 128Mi | 256Mi |
| Nucleus User Sync | 128Mi | 256Mi |

### Example Deployment

A minimal Kubernetes deployment consists of:
1. **Secret** for HXI credentials (`client-id`, `client-secret`, `environment-key`)
2. **Deployment** referencing the connector image with environment variables

See each component's configuration page for the full list of required environment variables.

---

## Knowledge Discovery JAR Module Installation

The Knowledge Discovery JAR Module **must be installed directly in the Alfresco Repository**. It cannot be deployed as a standalone service.

### Option 1: Use the Pre-built Docker Image (Recommended)

A Docker image is provided that layers the module on top of the standard Alfresco Enterprise image:

```yaml
services:
  alfresco:
    image: quay.io/alfresco/alfresco-content-repository-hxinsight-extension:<version>
```

This image is built from the official `quay.io/alfresco/alfresco-content-repository` image with the JAMP JAR added to `WEB-INF/lib/`.

### Option 2: Mount the JAR as a Volume

Add the JAR to an existing Alfresco container:

```yaml
services:
  alfresco:
    image: quay.io/alfresco/alfresco-content-repository:23.2
    volumes:
      - ./alfresco-hxinsight-connector-hxinsight-extension.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/hxinsight-extension.jar
```

### Option 3: Manual Installation

For non-Docker deployments, copy the JAR to the modules directory:

```bash
cp alfresco-hxinsight-connector-hxinsight-extension-*.jar \
   $ALFRESCO_HOME/modules/platform/
```

Then restart Alfresco.

### Configuration

Configure via `alfresco-global.properties` or Java system properties (`-D` flags):

```properties
hxi.discovery.base-url=https://hxinsight.hyland.com
hxi.auth.providers.hyland-experience.client-id=<your-client-id>
hxi.auth.providers.hyland-experience.client-secret=<your-client-secret>
hxi.auth.providers.hyland-experience.token-uri=https://auth.hyland.com/oauth/token
```

---

## Building from Source

```bash
# Build all modules
mvn clean install -DskipTests

# Build Docker images
./scripts/ci/buildDockerImages.sh

# Or build individual images
mvn spring-boot:build-image -pl live-ingester -DskipTests
```
