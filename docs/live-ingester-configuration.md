# Live Ingester Configuration

## Overview

The Live Ingester is a Spring Boot application that listens to Alfresco Repository events in real-time and ingests them into Hyland Experience Insight. It handles content transformation, file storage, and event processing.

## Configuration Parameters

Configuration is provided via `application.yml` or environment variables. Below are all available configuration parameters with their default values.

### Server Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | HTTP port for the Live Ingester application |

### Management & Health

| Property | Default | Description |
|----------|---------|-------------|
| `management.endpoints.web.exposure.include` | `health` | Actuator endpoints to expose |
| `management.endpoint.health.probes.enabled` | `true` | Enable Kubernetes-style health probes |
| `management.health.liveness-state.enabled` | `true` | Enable liveness probe |
| `management.health.readiness-state.enabled` | `true` | Enable readiness probe |

### Apache Camel Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `camel.springboot.main-run-controller` | `true` | Enable Camel main run controller |
| `camel.dataformat.jackson.autoDiscoverObjectMapper` | `true` | Auto-discover Jackson ObjectMapper |
| `camel.main.auto-startup` | `false` | Auto-start Camel routes on application startup |

### Application Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `application.source-id` | `a1f3e7c0-d193-7023-ce1d-0a63de491876` | Unique identifier for this connector instance |
| `application.version` | `@project.version@` | Application version (injected during build) |

### Alfresco Repository Configuration

#### Basic Settings

| Property | Default | Description | Required |
|----------|---------|-------------|----------|
| `alfresco.repository.events-endpoint` | `activemq:topic:alfresco.repo.event2` | ActiveMQ endpoint for Alfresco events | Yes |
| `alfresco.repository.base-url` | - | Base URL of the Alfresco Repository | No |
| `alfresco.repository.discovery-endpoint` | `${alfresco.repository.base-url}/api/discovery` | Discovery API endpoint | Conditional* |
| `alfresco.repository.version-override` | - | Override auto-detected repository version | Conditional* |

*Either `discovery-endpoint` or `version-override` must be set.

#### Health Probe Settings

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.repository.health-probe.endpoint` | `${alfresco.repository.base-url}` | Health check endpoint for repository |
| `alfresco.repository.health-probe.timeout-seconds` | `1800` | Maximum time to wait for repository to become available (seconds) |
| `alfresco.repository.health-probe.interval-seconds` | `30` | Interval between health check attempts (seconds) |

### Bulk Ingester Integration

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.bulk-ingester.endpoint` | `activemq:queue:bulk-ingester-events` | ActiveMQ endpoint for bulk ingester events |

### Content Transformation Configuration

#### Transform Request Settings

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.transform.request.endpoint` | `activemq:queue:acs-repo-transform-request?jmsMessageType=Text` | Transform service request queue |
| `alfresco.transform.request.timeout` | `20000` | Transform request timeout (milliseconds) |
| `alfresco.transform.request.options.[mime-type].*` | - | Transform options per MIME type |

**Default Transform Options:**

For `image/png` and `image/jpeg`:
- `resizeWidth`: `3840`
- `resizeHeight`: `3840`
- `allowEnlargement`: `false`

#### Transform Response Settings

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.transform.response.queue-name` | `org.alfresco.hxinsight-connector.transform.response` | Response queue name |
| `alfresco.transform.response.endpoint` | `activemq:queue:${alfresco.transform.response.queue-name}` | Transform response queue endpoint |

#### Transform Response Retry Settings

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.transform.response.retry-ingestion.attempts` | `-1` | Number of retry attempts for ingestion (-1 = infinite) |
| `alfresco.transform.response.retry-ingestion.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `alfresco.transform.response.retry-ingestion.delay-multiplier` | `2` | Multiplier for delay between retries |
| `alfresco.transform.response.retry-transformation.attempts` | `10` | Number of retry attempts for transformation |
| `alfresco.transform.response.retry-transformation.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `alfresco.transform.response.retry-transformation.delay-multiplier` | `2` | Multiplier for delay between retries |

#### Shared File Store Settings

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.transform.shared-file-store.base-url` | - | Base URL for Shared File Store |
| `alfresco.transform.shared-file-store.file-endpoint` | `${alfresco.transform.shared-file-store.base-url}/alfresco/api/-default-/private/sfs/versions/1/file` | File endpoint |

#### Shared File Store Retry Settings

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.transform.shared-file-store.retry.attempts` | `10` | Number of retry attempts |
| `alfresco.transform.shared-file-store.retry.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `alfresco.transform.shared-file-store.retry.delay-multiplier` | `2` | Multiplier for delay between retries |

### Content Filtering Configuration

The Live Ingester supports filtering nodes by aspect, type, and path.

#### Aspect Filtering

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.filter.aspect.allow` | `[]` | List of aspects to allow (empty = allow all) |
| `alfresco.filter.aspect.deny` | `[]` | List of aspects to deny |

#### Type Filtering

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.filter.type.allow` | `[]` | List of types to allow (empty = allow all) |
| `alfresco.filter.type.deny` | `[]` | List of types to deny |

#### Path Filtering

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.filter.path.allow` | `[]` | List of path patterns to allow (empty = allow all) |
| `alfresco.filter.path.deny` | `[]` | List of path patterns to deny |

### Hyland Experience Insight Configuration

#### Ingestion Settings

| Property | Default | Description |
|----------|---------|-------------|
| `hyland-experience.insight.ingestion.base-url` | - | Base URL for HxI Ingestion API |
| `hyland-experience.ingester.endpoint` | `${hyland-experience.insight.ingestion.base-url}/ingestion-events?httpMethod=POST&throwExceptionOnFailure=false` | Ingestion events endpoint |

#### Ingester Retry Settings

| Property | Default | Description |
|----------|---------|-------------|
| `hyland-experience.ingester.retry.attempts` | `10` | Number of retry attempts |
| `hyland-experience.ingester.retry.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `hyland-experience.ingester.retry.delay-multiplier` | `2` | Multiplier for delay between retries |

#### Storage Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `hyland-experience.storage.digest-algorithm` | `SHA-256` | Hash algorithm for content digests |
| `hyland-experience.storage.location.endpoint` | `${hyland-experience.insight.ingestion.base-url}/presigned-urls?httpMethod=POST&throwExceptionOnFailure=false` | Presigned URL endpoint |

#### Storage Location Retry Settings

| Property | Default | Description |
|----------|---------|-------------|
| `hyland-experience.storage.location.retry.attempts` | `10` | Number of retry attempts |
| `hyland-experience.storage.location.retry.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `hyland-experience.storage.location.retry.delay-multiplier` | `2` | Multiplier for delay between retries |

#### Storage Upload Retry Settings

| Property | Default | Description |
|----------|---------|-------------|
| `hyland-experience.storage.upload.retry.attempts` | `10` | Number of retry attempts |
| `hyland-experience.storage.upload.retry.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `hyland-experience.storage.upload.retry.delay-multiplier` | `2` | Multiplier for delay between retries |

### Authentication Configuration

#### Hyland Experience Authentication (OAuth2)

| Property | Default | Description | Required |
|----------|---------|-------------|----------|
| `auth.providers.hyland-experience.type` | `oauth2` | Authentication type | Yes |
| `auth.providers.hyland-experience.client-id` | - | OAuth2 client ID | Yes |
| `auth.providers.hyland-experience.client-secret` | - | OAuth2 client secret | Yes |
| `auth.providers.hyland-experience.token-uri` | - | OAuth2 token endpoint | Yes |
| `auth.providers.hyland-experience.grant-type` | `client_credentials` | OAuth2 grant type | Yes |
| `auth.providers.hyland-experience.scope` | `[hxp.integrations]` | OAuth2 scopes | Yes |
| `auth.providers.hyland-experience.environment-key` | - | Environment key for HxI | Yes |
| `auth.providers.hyland-experience.client-name` | `acs-insight-connector` | Client name | Yes |

#### Alfresco Authentication (Basic)

| Property | Default | Description | Required |
|----------|---------|-------------|----------|
| `auth.providers.alfresco.type` | `basic` | Authentication type | Yes |
| `auth.providers.alfresco.username` | - | Basic auth username | Yes |
| `auth.providers.alfresco.password` | - | Basic auth password | Yes |

#### Authentication Retry Settings

| Property | Default | Description |
|----------|---------|-------------|
| `auth.retry.attempts` | `10` | Number of retry attempts for authentication |
| `auth.retry.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `auth.retry.delay-multiplier` | `2` | Multiplier for delay between retries |

## Retry Mechanism

The Live Ingester implements an exponential backoff retry mechanism with the following default behavior:

- **Default attempts**: 10
- **Default initial delay**: 500ms
- **Default delay multiplier**: 2

The retry mechanism is applied to:
- External API calls (HxI Ingestion API)
- Authentication requests
- File downloads from Shared File Store
- File uploads to storage locations
- Content transformation requests

### Retryable Exceptions

By default, the following exceptions trigger a retry:
- `EndpointServerErrorException`
- `UnknownHostException`
- `MalformedURLException`
- `JsonEOFException`
- `MismatchedInputException`
- `HttpHostConnectException`
- `NoHttpResponseException`
- `MalformedChunkCodingException`

## Example Configuration

```yaml
server:
  port: 8080

alfresco:
  repository:
    base-url: http://alfresco:8080/alfresco
    events-endpoint: activemq:topic:alfresco.repo.event2
    discovery-endpoint: ${alfresco.repository.base-url}/api/discovery
    health-probe:
      endpoint: ${alfresco.repository.base-url}
      timeout-seconds: 1800
      interval-seconds: 30
  filter:
    type:
      deny:
        - cm:thumbnail
        - cm:failedThumbnail
  transform:
    shared-file-store:
      base-url: http://shared-file-store:8099

hyland-experience:
  insight:
    ingestion:
      base-url: https://hxi-ingestion.example.com
  storage:
    digest-algorithm: SHA-256

auth:
  providers:
    hyland-experience:
      type: oauth2
      client-id: your-client-id
      client-secret: your-client-secret
      token-uri: https://auth.example.com/token
      environment-key: your-env-key
    alfresco:
      type: basic
      username: admin
      password: admin
```

## Environment Variables

All configuration properties can be overridden using environment variables. Convert property names to uppercase, replace dots with underscores, and remove hyphens:

```bash
# Example
ALFRESCO_REPOSITORY_BASEURL=http://alfresco:8080/alfresco
AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID=your-client-id
HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL=https://hxi-ingestion.example.com
```

## Docker Compose Configuration

When running the Live Ingester in Docker Compose, use environment variables to configure the service.

### Example Docker Compose Service

```yaml
live-ingester:
  image: quay.io/alfresco/alfresco-hxinsight-connector-live-ingester:${HXINSIGHT_CONNECTOR_TAG}
  deploy:
    resources:
      limits:
        memory: 512m
  depends_on:
    activemq:
      condition: service_started
    transform-router:
      condition: service_healthy
    alfresco:
      condition: service_healthy
  environment:
    JAVA_TOOL_OPTIONS: -agentlib:jdwp=transport=dt_socket,address=*:5007,server=y,suspend=n
    LOGGING_LEVEL_ORG_ALFRESCO: DEBUG
    SPRING_ACTIVEMQ_BROKERURL: nio://activemq:61616
    ALFRESCO_REPOSITORY_BASEURL: http://alfresco:8080/alfresco
    ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL: http://shared-file-store:8099
    HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL: https://insight.hyland.com
    AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID: your-client-id
    AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET: your-client-secret
    AUTH_PROVIDERS_HYLANDEXPERIENCE_ENVIRONMENTKEY: your-env-key
    AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI: https://auth.hyland.com/token
    AUTH_PROVIDERS_ALFRESCO_USERNAME: admin
    AUTH_PROVIDERS_ALFRESCO_PASSWORD: admin
  ports:
    - "5007:5007"  # Debug port (optional)
```

### Key Configuration Points for Docker Compose

1. **Service Discovery**: Use Docker service names instead of localhost
   - `http://alfresco:8080` instead of `http://localhost:8080`
   - `nio://activemq:61616` for ActiveMQ broker URL

2. **Dependencies**: Ensure proper service startup order using `depends_on`

3. **Resource Limits**: Set appropriate memory limits (recommended: 512m minimum)

4. **Debug Port**: Expose port 5007 for remote debugging (optional)

5. **Logging**: Set `LOGGING_LEVEL_ORG_ALFRESCO` to control log verbosity

### Complete Example Files

Complete Docker Compose examples are available in the project repository:

- **Full Stack**: `distribution/src/main/resources/docker-compose/docker-compose.yml`
  - Includes all components (for local development, uses WireMock for HxI services)
  - Update HxI endpoints to point to your actual HxI environment

- **Without Live Ingester**: `distribution/src/main/resources/docker-compose/docker-compose-ingesterless.yml`
  - For running Live Ingester outside Docker (e.g., in IDE)
  - Useful for debugging and development

- **Minimal Setup**: `distribution/src/main/resources/docker-compose/docker-compose-minimal.yml`
  - Minimal configuration for quick testing

- **With Keycloak**: `distribution/src/main/resources/docker-compose/docker-compose-w-keycloak.yml`
  - Includes Keycloak for OAuth2/OIDC authentication

**Note:** The example files include mock services for local development. For production deployments, configure the HxI endpoints to point to your actual Hyland Experience Insight environment.

### Environment File

Use a `.env` file to manage versions and common variables:

```dotenv
HXINSIGHT_CONNECTOR_TAG=2.0.3-SNAPSHOT
ALFRESCO_TAG=25.2.0-A.12
ACTIVE_MQ_TAG=5.18.5-jre17-rockylinux8
```

See `distribution/src/main/resources/docker-compose/.env` for the complete example.
