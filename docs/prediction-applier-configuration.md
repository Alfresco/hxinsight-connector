# Prediction Applier Configuration

## Overview

The Prediction Applier is a Spring Boot application that polls Hyland Experience Insight for predictions and applies them back to the Alfresco Repository. It acts as a bridge between HxI's AI-generated predictions and the repository content.

## Configuration Parameters

Configuration is provided via `application.yml` or environment variables. Below are all available configuration parameters with their default values.

### Server Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | HTTP port for the Prediction Applier application |

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
| `camel.main.auto-startup` | `false` | Auto-start Camel routes on application startup |

### Application Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `application.source-id` | `a1f3e7c0-d193-7023-ce1d-0a63de491876` | Unique identifier for this connector instance |
| `application.version` | `@project.version@` | Application version (injected during build) |

### Hyland Experience Insight Configuration

#### Predictions Settings

| Property | Default | Description | Required |
|----------|---------|-------------|----------|
| `hyland-experience.insight.predictions.base-url` | - | Base URL for HxI Predictions API | Yes |
| `hyland-experience.insight.predictions.poll-period-millis` | `300000` | Polling interval in milliseconds (5 minutes) | Conditional* |
| `hyland-experience.insight.predictions.collector-timer-endpoint` | Auto-generated** | Quartz timer endpoint for polling | Conditional* |
| `hyland-experience.insight.predictions.buffer-endpoint` | `activemq:queue:predictions-buffer` | ActiveMQ queue for buffering predictions | No |

\* Either `poll-period-millis` or `collector-timer-endpoint` must be set.

\*\* If `poll-period-millis` is set, the endpoint is automatically generated as:
```
quartz:predictions-collector-timer?autoStartScheduler=true&trigger.repeatInterval={poll-period-millis}
```

### Alfresco Repository Configuration

#### Basic Settings

| Property | Default | Description | Required |
|----------|---------|-------------|----------|
| `alfresco.repository.base-url` | - | Base URL of the Alfresco Repository | Yes |
| `alfresco.repository.discovery-endpoint` | `${alfresco.repository.base-url}/api/discovery` | Discovery API endpoint for version detection | Yes |

#### Repository Retry Settings

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.repository.retry.attempts` | `10` | Number of retry attempts for repository API calls |
| `alfresco.repository.retry.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `alfresco.repository.retry.delay-multiplier` | `2` | Multiplier for delay between retries |

#### Health Probe Settings

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.repository.health-probe.endpoint` | `${alfresco.repository.base-url}` | Health check endpoint for repository |
| `alfresco.repository.health-probe.timeout-seconds` | `1800` | Maximum time to wait for repository to become available (seconds) |
| `alfresco.repository.health-probe.interval-seconds` | `30` | Interval between health check attempts (seconds) |

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
| `auth.providers.alfresco.client-id` | - | Client identifier (optional) | No |
| `auth.providers.alfresco.username` | - | Basic auth username | Yes |
| `auth.providers.alfresco.password` | - | Basic auth password | Yes |

#### Authentication Retry Settings

| Property | Default | Description |
|----------|---------|-------------|
| `auth.retry.attempts` | `10` | Number of retry attempts for authentication |
| `auth.retry.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `auth.retry.delay-multiplier` | `2` | Multiplier for delay between retries |

## Prediction Polling

The Prediction Applier polls HxI for new predictions at regular intervals. The polling mechanism can be configured in two ways:

### 1. Simple Polling (Recommended)

Set the polling period in milliseconds:

```yaml
hyland-experience:
  insight:
    predictions:
      poll-period-millis: 300000  # 5 minutes
```

### 2. Custom Quartz Configuration

Provide a custom Quartz timer endpoint for advanced scheduling:

```yaml
hyland-experience:
  insight:
    predictions:
      collector-timer-endpoint: "quartz:predictions-collector-timer?cron=0+0/5+*+*+*+?"
```

## Retry Mechanism

The Prediction Applier uses the same exponential backoff retry mechanism as other components:

- **Default attempts**: 10
- **Default initial delay**: 500ms
- **Default delay multiplier**: 2

This applies to:
- Prediction API calls to HxI
- Repository API calls to Alfresco
- Authentication requests

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

## Prediction Processing Flow

1. **Poll**: The application polls HxI at configured intervals
2. **Buffer**: Retrieved predictions are placed in an ActiveMQ queue
3. **Process**: Predictions are read from the queue and applied to repository nodes
4. **Retry**: Failed predictions are retried according to retry configuration
5. **Acknowledge**: Successfully applied predictions are acknowledged

## Example Configuration

```yaml
server:
  port: 8080

management:
  endpoints.web.exposure.include: health
  endpoint.health.probes.enabled: true

camel:
  springboot:
    main-run-controller: true
  main:
    auto-startup: false

application:
  source-id: a1f3e7c0-d193-7023-ce1d-0a63de491876
  version: "@project.version@"

hyland-experience:
  insight:
    predictions:
      base-url: https://hxi-predictions.example.com
      poll-period-millis: 300000  # 5 minutes
      buffer-endpoint: activemq:queue:predictions-buffer

alfresco:
  repository:
    base-url: http://alfresco:8080/alfresco
    discovery-endpoint: ${alfresco.repository.base-url}/api/discovery
    retry:
      attempts: 10
      initial-delay: 500
      delay-multiplier: 2
    health-probe:
      endpoint: ${alfresco.repository.base-url}
      timeout-seconds: 1800
      interval-seconds: 30

auth:
  providers:
    hyland-experience:
      type: oauth2
      client-id: your-client-id
      client-secret: your-client-secret
      token-uri: https://auth.example.com/token
      grant-type: client_credentials
      scope:
        - hxp.integrations
      environment-key: your-env-key
      client-name: acs-insight-connector
    alfresco:
      type: basic
      username: admin
      password: admin
  retry:
    attempts: 10
    initial-delay: 500
    delay-multiplier: 2
```

## Environment Variables

All configuration properties can be overridden using environment variables. Convert property names to uppercase, replace dots with underscores, and remove hyphens:

```bash
# HxI Predictions
HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_BASEURL=https://hxi-predictions.example.com
HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_POLLPERIODMILLIS=300000

# Alfresco Repository
ALFRESCO_REPOSITORY_BASEURL=http://alfresco:8080/alfresco

# Authentication
AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID=your-client-id
AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET=your-client-secret
AUTH_PROVIDERS_ALFRESCO_USERNAME=admin
AUTH_PROVIDERS_ALFRESCO_PASSWORD=admin
```

## Docker Compose Configuration

When running the Prediction Applier in Docker Compose, use environment variables to configure the service.

### Example Docker Compose Service

```yaml
prediction-applier:
  image: quay.io/alfresco/alfresco-hxinsight-connector-prediction-applier:${HXINSIGHT_CONNECTOR_TAG}
  deploy:
    resources:
      limits:
        memory: 256m
  depends_on:
    alfresco:
      condition: service_healthy
    activemq:
      condition: service_started
  environment:
    JAVA_TOOL_OPTIONS: -agentlib:jdwp=transport=dt_socket,address=*:5009,server=y,suspend=n
    LOGGING_LEVEL_ORG_ALFRESCO: INFO
    SPRING_ACTIVEMQ_BROKERURL: nio://activemq:61616
    ALFRESCO_REPOSITORY_BASEURL: http://alfresco:8080/alfresco
    HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_BASEURL: https://predictions.hyland.com
    HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_POLLPERIODMILLIS: 300000
    AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID: your-client-id
    AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET: your-client-secret
    AUTH_PROVIDERS_HYLANDEXPERIENCE_ENVIRONMENTKEY: your-env-key
    AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI: https://auth.hyland.com/token
    AUTH_PROVIDERS_ALFRESCO_USERNAME: admin
    AUTH_PROVIDERS_ALFRESCO_PASSWORD: admin
  ports:
    - "5009:5009"  # Debug port (optional)
  healthcheck:
    test: curl --fail http://localhost:8080/actuator/health || exit 1
    interval: 30s
    timeout: 3s
    retries: 3
    start_period: 30s
```

### Key Configuration Points for Docker Compose

1. **Service Discovery**: Use Docker service names instead of localhost
   - `http://alfresco:8080` instead of `http://localhost:8080`
   - `nio://activemq:61616` for ActiveMQ broker URL

2. **Dependencies**: Ensure Alfresco is healthy before starting
   - Use `depends_on` with `condition: service_healthy`

3. **Resource Limits**: Prediction Applier is lightweight (256m recommended)

4. **Debug Port**: Expose port 5009 for remote debugging (optional)

5. **Health Check**: Configure health probes for Kubernetes/Docker Swarm

6. **Polling Interval**: Adjust based on prediction frequency needs
   - Default: 300000ms (5 minutes)
   - For faster updates: reduce to 60000ms (1 minute)

### Production Configuration Example

For production environments:

```yaml
prediction-applier:
  image: quay.io/alfresco/alfresco-hxinsight-connector-prediction-applier:${HXINSIGHT_CONNECTOR_TAG}
  deploy:
    resources:
      limits:
        memory: 512m
      reservations:
        memory: 256m
    restart_policy:
      condition: on-failure
      delay: 5s
      max_attempts: 3
  environment:
    LOGGING_LEVEL_ORG_ALFRESCO: WARN
    SPRING_ACTIVEMQ_BROKERURL: failover:(nio://activemq:61616)
    ALFRESCO_REPOSITORY_BASEURL: http://alfresco:8080/alfresco
    ALFRESCO_REPOSITORY_RETRY_ATTEMPTS: 20
    HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_BASEURL: https://predictions.hyland.com
    HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_POLLPERIODMILLIS: 180000  # 3 minutes
    AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID: ${HXI_CLIENT_ID}
    AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET: ${HXI_CLIENT_SECRET}
    AUTH_PROVIDERS_HYLANDEXPERIENCE_ENVIRONMENTKEY: ${HXI_ENV_KEY}
    AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI: https://auth.hyland.com/token
    AUTH_PROVIDERS_ALFRESCO_USERNAME: ${ACS_USERNAME}
    AUTH_PROVIDERS_ALFRESCO_PASSWORD: ${ACS_PASSWORD}
  healthcheck:
    test: curl --fail http://localhost:8080/actuator/health/readiness || exit 1
    interval: 30s
    timeout: 5s
    retries: 3
    start_period: 60s
```

### Complete Example Files

Complete Docker Compose examples are available in the project repository:

- **Full Stack**: `distribution/src/main/resources/docker-compose/docker-compose.yml`
  - Includes Prediction Applier with mock HxI services
  - Suitable for local development and testing

- **Minimal Setup**: `distribution/src/main/resources/docker-compose/docker-compose-minimal.yml`
  - Minimal configuration for quick testing

- **With Keycloak**: `distribution/src/main/resources/docker-compose/docker-compose-w-keycloak.yml`
  - Includes Keycloak for OAuth2/OIDC authentication

### Environment File

Use a `.env` file to manage versions and secrets:

```dotenv
HXINSIGHT_CONNECTOR_TAG=2.0.3-SNAPSHOT

# HxI Configuration
HXI_CLIENT_ID=your-client-id
HXI_CLIENT_SECRET=your-client-secret
HXI_ENV_KEY=your-env-key

# Alfresco Credentials
ACS_USERNAME=admin
ACS_PASSWORD=admin
```

See `distribution/src/main/resources/docker-compose/.env` for the complete example.

### Running with Docker Compose

```bash
# Navigate to docker-compose directory
cd distribution/src/main/resources/docker-compose

# Start the full stack
docker compose up -d

# View Prediction Applier logs
docker compose logs -f prediction-applier

# Check health status
docker compose ps prediction-applier

# Stop services
docker compose down
```

## Running the Prediction Applier

### Prerequisites

1. Alfresco Repository must be running and accessible
2. Hyland Experience Insight must be configured and accessible
3. ActiveMQ must be running for prediction buffering
4. Valid authentication credentials for both systems

### Starting the Application

```bash
java -jar alfresco-hxinsight-connector-prediction-applier-{version}.jar
```

### With Custom Configuration

```bash
java -jar alfresco-hxinsight-connector-prediction-applier-{version}.jar \
  --spring.config.location=file:/path/to/application.yml
```

## Performance Tuning

### Polling Frequency

Adjust polling frequency based on your needs:

```yaml
# More frequent polling (1 minute)
hyland-experience.insight.predictions.poll-period-millis: 60000

# Less frequent polling (15 minutes)
hyland-experience.insight.predictions.poll-period-millis: 900000
```

**Considerations:**
- More frequent polling increases API calls but reduces prediction lag
- Less frequent polling reduces load but increases time to apply predictions

### Retry Configuration

For unstable network connections, increase retry attempts:

```yaml
alfresco.repository.retry.attempts: 20
auth.retry.attempts: 15
```

For faster failure detection, reduce retry delays:

```yaml
alfresco.repository.retry.initial-delay: 250
alfresco.repository.retry.delay-multiplier: 1.5
```

## Monitoring

The Prediction Applier exposes health endpoints for monitoring:

- **Health**: `http://localhost:8080/actuator/health`
- **Liveness**: `http://localhost:8080/actuator/health/liveness`
- **Readiness**: `http://localhost:8080/actuator/health/readiness`

### Kubernetes Integration

The health probes are designed for Kubernetes:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

## Troubleshooting

### Predictions Not Being Applied

1. Check authentication credentials
2. Verify Alfresco Repository is accessible
3. Check HxI Predictions API endpoint
4. Review application logs for errors
5. Verify ActiveMQ connectivity

### High Prediction Lag

1. Reduce `poll-period-millis` for more frequent polling
2. Check network latency between services
3. Review retry configuration
4. Monitor ActiveMQ queue depth

### Connection Timeouts

1. Increase retry attempts and delays
2. Verify network connectivity
3. Check firewall rules
4. Review health probe timeout settings
