# HxInsight Connector Configuration Documentation

## Overview

This directory contains comprehensive configuration documentation for all components of the Alfresco Connector for Hyland Experience Insight.

## Components

The HxInsight Connector consists of four main components, each with its own configuration requirements:

### 1. [Live Ingester](live-ingester-configuration.md)

Real-time event processing component that:
- Listens to Alfresco Repository events
- Transforms content for ingestion
- Sends data to Hyland Experience Insight
- Handles content filtering and routing

**Key Configuration Areas:**
- Repository connection and event streaming
- Content transformation settings
- HxI ingestion endpoints
- Storage and upload configuration
- Content filtering (aspect, type, path)
- Authentication (OAuth2 and Basic)
- Retry and resilience settings

### 2. [Bulk Ingester](bulk-ingester-configuration.md)

Batch processing component that:
- Reads existing content from Alfresco database
- Performs bulk ingestion of repository content
- Publishes events for Live Ingester processing
- Supports incremental and full reindexing

**Key Configuration Areas:**
- Database connection settings
- Node range and pagination
- Namespace prefix mappings
- Content filtering
- Event publishing
- Performance tuning

### 3. [Prediction Applier](prediction-applier-configuration.md)

AI prediction integration component that:
- Polls HxI for generated predictions
- Applies predictions to repository content
- Manages prediction lifecycle
- Handles prediction buffering

**Key Configuration Areas:**
- Polling configuration
- Repository API settings
- Prediction buffering
- Authentication
- Retry mechanisms
- Health monitoring

### 4. [HxInsight Extension](hxinsight-extension-configuration.md)

Repository extension (AMP) that:
- Provides REST APIs for prediction management
- Integrates agent discovery
- Handles question answering
- Manages prediction state in repository

**Key Configuration Areas:**
- Service account configuration
- Authentication settings
- Discovery endpoints
- Question/answer configuration
- Knowledge retrieval
- Security settings

## Quick Start

### Minimal Configuration

Each component requires a minimal set of configuration to function. Here's what you need at minimum:

#### Live Ingester
```yaml
alfresco.repository.base-url: http://alfresco:8080/alfresco
hyland-experience.insight.ingestion.base-url: https://hxi.example.com
auth.providers.hyland-experience.client-id: your-client-id
auth.providers.hyland-experience.client-secret: your-client-secret
auth.providers.hyland-experience.token-uri: https://auth.example.com/token
auth.providers.hyland-experience.environment-key: your-env-key
auth.providers.alfresco.username: admin
auth.providers.alfresco.password: admin
```

#### Bulk Ingester
```yaml
spring.datasource.url: jdbc:postgresql://postgres:5432/alfresco
spring.datasource.username: alfresco
spring.datasource.password: alfresco
```

#### Prediction Applier
```yaml
alfresco.repository.base-url: http://alfresco:8080/alfresco
hyland-experience.insight.predictions.base-url: https://hxi-predictions.example.com
hyland-experience.insight.predictions.poll-period-millis: 300000
auth.providers.hyland-experience.client-id: your-client-id
auth.providers.hyland-experience.client-secret: your-client-secret
auth.providers.hyland-experience.token-uri: https://auth.example.com/token
auth.providers.hyland-experience.environment-key: your-env-key
auth.providers.alfresco.username: admin
auth.providers.alfresco.password: admin
```

#### HxInsight Extension
```properties
hxi.auth.providers.hyland-experience.client-id=your-client-id
hxi.auth.providers.hyland-experience.client-secret=your-client-secret
hxi.auth.providers.hyland-experience.token-uri=https://auth.example.com/token
hxi.auth.providers.hyland-experience.environment-key=your-env-key
hxi.discovery.base-url=https://hxi.example.com
```

## Configuration Formats

### Spring Boot Components (YAML)

Live Ingester, Bulk Ingester, and Prediction Applier use Spring Boot configuration in YAML format:
- Default location: `src/main/resources/application.yml`
- Override with: `--spring.config.location=file:/path/to/application.yml`
- Or use environment variables

### Repository Extension (Properties)

HxInsight Extension uses Java properties format:
- Module defaults: `alfresco/module/alfresco-hxinsight-connector-hxinsight-extension/alfresco-global.properties`
- System overrides: `tomcat/shared/classes/alfresco-global.properties`
- Or use environment variables with `${VARIABLE_NAME}` syntax

## Common Configuration Patterns

### Authentication

All components share similar authentication configuration patterns:

**OAuth2 for Hyland Experience:**
```yaml
auth.providers.hyland-experience:
  type: oauth2
  client-id: ${HXI_CLIENT_ID}
  client-secret: ${HXI_CLIENT_SECRET}
  token-uri: https://auth.example.com/token
  grant-type: client_credentials
  scope: [hxp.integrations]
  environment-key: ${HXI_ENV_KEY}
  client-name: acs-insight-connector
```

**Basic Auth for Alfresco:**
```yaml
auth.providers.alfresco:
  type: basic
  username: ${ALFRESCO_USERNAME}
  password: ${ALFRESCO_PASSWORD}
```

### Retry Configuration

All components use exponential backoff retry:
```yaml
retry:
  attempts: 10          # -1 for infinite
  initial-delay: 500    # milliseconds
  delay-multiplier: 2   # exponential factor
```

### Content Filtering

Live Ingester and Bulk Ingester support identical filtering:
```yaml
alfresco.filter:
  aspect:
    allow: []           # empty = allow all
    deny: [cm:thumbnail]
  type:
    allow: []
    deny: [cm:failedThumbnail]
  path:
    allow: []
    deny: [/sys:*]
```

## Environment Variables

All configuration properties can be overridden with environment variables:

**Conversion Rule:**
1. Convert to uppercase
2. Replace `.` with `_`
3. Remove `-` (hyphens are deleted)

**Examples:**
```bash
# YAML: alfresco.repository.base-url
ALFRESCO_REPOSITORY_BASEURL=http://alfresco:8080/alfresco

# YAML: auth.providers.hyland-experience.client-id
AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID=your-client-id

# Properties: hxi.auth.retry.attempts
HXI_AUTH_RETRY_ATTEMPTS=5
```

## Deployment Architectures

### Standalone Development

All components running on localhost:
```
┌─────────────────┐
│   Alfresco      │←──┐
│   Repository    │   │
└─────────────────┘   │
         ↓            │
┌─────────────────┐   │
│ Live Ingester   │───┤
└─────────────────┘   │
         ↓            │
┌─────────────────┐   │
│ Bulk Ingester   │───┤
└─────────────────┘   │
         ↓            │
┌─────────────────┐   │
│ Prediction      │───┘
│ Applier         │
└─────────────────┘
         ↓
┌─────────────────┐
│   Hyland        │
│   Experience    │
│   Insight       │
└─────────────────┘
```

### Docker Compose

Components in containers with service discovery:
- Use service names for URLs (e.g., `http://alfresco:8080`)
- Share ActiveMQ instance
- External HxI endpoints

#### Available Docker Compose Examples

The project includes several Docker Compose configurations in `distribution/src/main/resources/docker-compose/`:

| File | Description | Use Case |
|------|-------------|----------|
| `docker-compose.yml` | Full stack with all components | Complete local development environment (uses WireMock for HxI services; update endpoints for production) |
| `docker-compose-ingesterless.yml` | Stack without Live Ingester | For running Live Ingester in IDE for debugging |
| `docker-compose-minimal.yml` | Minimal configuration | Quick testing with essential services only |
| `docker-compose-w-keycloak.yml` | Stack with Keycloak | OAuth2/OIDC authentication testing |
| `.env` | Environment variables | Version tags and configuration values |

#### Running Docker Compose

```bash
# Navigate to docker-compose directory
cd distribution/src/main/resources/docker-compose

# Start all services
docker compose up -d

# View logs for all services
docker compose logs -f

# View logs for specific service
docker compose logs -f live-ingester

# Check service health
docker compose ps

# Stop all services
docker compose down

# Stop and remove volumes
docker compose down -v
```

#### Docker Compose Service Configuration

Each component in Docker Compose uses environment variables for configuration:

**Live Ingester:**
```yaml
environment:
  ALFRESCO_REPOSITORY_BASEURL: http://alfresco:8080/alfresco
  HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL: http://hxinsight-mock:8080
  AUTH_PROVIDERS_ALFRESCO_USERNAME: admin
  AUTH_PROVIDERS_ALFRESCO_PASSWORD: admin
```

**Bulk Ingester:**
```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/alfresco
  SPRING_DATASOURCE_USERNAME: alfresco
  SPRING_DATASOURCE_PASSWORD: alfresco
```

**Prediction Applier:**
```yaml
environment:
  ALFRESCO_REPOSITORY_BASEURL: http://alfresco:8080/alfresco
  HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_BASEURL: http://hxinsight-mock:8080
  HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_POLLPERIODMILLIS: 300000
```

See component-specific documentation for complete Docker Compose examples:
- [Live Ingester - Docker Compose Configuration](live-ingester-configuration.md#docker-compose-configuration)
- [Bulk Ingester - Docker Compose Configuration](bulk-ingester-configuration.md#docker-compose-configuration)
- [Prediction Applier - Docker Compose Configuration](prediction-applier-configuration.md#docker-compose-configuration)

#### Available Docker Compose Examples

The project includes several Docker Compose configurations in `distribution/src/main/resources/docker-compose/`:

| File | Description | Use Case |
|------|-------------|----------|
| `docker-compose.yml` | Full stack with all components | Complete local development environment with mock HxI services |
| `docker-compose-ingesterless.yml` | Stack without Live Ingester | For running Live Ingester in IDE for debugging |
| `docker-compose-minimal.yml` | Minimal configuration | Quick testing with essential services only |
| `docker-compose-w-keycloak.yml` | Stack with Keycloak | OAuth2/OIDC authentication testing |
| `.env` | Environment variables | Version tags and configuration values |

#### Running Docker Compose

```bash
# Navigate to docker-compose directory
cd distribution/src/main/resources/docker-compose

# Start all services
docker compose up -d

# View logs for all services
docker compose logs -f

# View logs for specific service
docker compose logs -f live-ingester

# Check service health
docker compose ps

# Stop all services
docker compose down

# Stop and remove volumes
docker compose down -v
```

#### Docker Compose Service Configuration

Each component in Docker Compose uses environment variables for configuration:

**Live Ingester:**
```yaml
environment:
  ALFRESCO_REPOSITORY_BASEURL: http://alfresco:8080/alfresco
  HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL: https://insight.hyland.com
  AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID: your-client-id
  AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET: your-client-secret
  AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI: https://auth.hyland.com/token
  AUTH_PROVIDERS_ALFRESCO_USERNAME: admin
  AUTH_PROVIDERS_ALFRESCO_PASSWORD: admin
```

**Bulk Ingester:**
```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/alfresco
  SPRING_DATASOURCE_USERNAME: alfresco
  SPRING_DATASOURCE_PASSWORD: alfresco
```

**Prediction Applier:**
```yaml
environment:
  ALFRESCO_REPOSITORY_BASEURL: http://alfresco:8080/alfresco
  HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_BASEURL: https://predictions.hyland.com
  HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_POLLPERIODMILLIS: 300000
  AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID: your-client-id
  AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET: your-client-secret
```

**Note:** The provided Docker Compose example files use mock services for local development. Replace the HxI URLs with your actual Hyland Experience Insight endpoints for production use.

See component-specific documentation for complete Docker Compose examples:
- [Live Ingester - Docker Compose Configuration](live-ingester-configuration.md#docker-compose-configuration)
- [Bulk Ingester - Docker Compose Configuration](bulk-ingester-configuration.md#docker-compose-configuration)
- [Prediction Applier - Docker Compose Configuration](prediction-applier-configuration.md#docker-compose-configuration)

### Kubernetes

Distributed deployment with:
- ConfigMaps for configuration
- Secrets for credentials
- Service mesh for communication
- Health probes enabled

## Security Best Practices

1. **Never commit secrets**: Use environment variables or secret managers
2. **Use HTTPS**: Configure SSL/TLS for all external endpoints
3. **Rotate credentials**: Regularly update passwords and client secrets
4. **Limit permissions**: Use service accounts with minimal required permissions
5. **Network isolation**: Restrict network access between components
6. **Audit logging**: Enable and monitor authentication logs
7. **Encrypt at rest**: Encrypt configuration files containing secrets

## Performance Tuning

### High Throughput

For high-volume repositories:
- Increase database pool size (Bulk Ingester)
- Increase page size (Bulk Ingester)
- Reduce polling interval (Prediction Applier)
- Increase transform timeout (Live Ingester)

### Low Latency

For real-time requirements:
- Decrease polling interval
- Optimize retry configuration
- Enable connection pooling
- Use SSD storage for queues

### Resource Constrained

For limited resources:
- Decrease database pool size
- Reduce page size
- Increase polling interval
- Enable content filtering

## Monitoring & Observability

All Spring Boot components expose:
- Health endpoints: `/actuator/health`
- Liveness probes: `/actuator/health/liveness`
- Readiness probes: `/actuator/health/readiness`

**Kubernetes Health Checks:**
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

### Connection Issues

1. Verify network connectivity between components
2. Check firewall rules
3. Validate DNS resolution
4. Review authentication credentials
5. Check service availability

### Performance Issues

1. Monitor ActiveMQ queue depth
2. Check database connection pool metrics
3. Review retry configuration
4. Analyze application logs
5. Profile resource usage (CPU, memory, I/O)

### Data Issues

1. Verify content filtering configuration
2. Check namespace prefix mappings (Bulk Ingester)
3. Review transformation settings
4. Validate prediction format
5. Inspect event payloads

## Migration & Upgrades

When upgrading components:

1. **Backup configuration**: Save current configuration files
2. **Review changes**: Check release notes for configuration changes
3. **Test in non-prod**: Validate configuration in test environment
4. **Plan downtime**: Some upgrades may require service restart
5. **Monitor closely**: Watch logs after upgrade

## Additional Resources

- [ACS Private API Documentation](acs-private-apis.md)
- [Main Project README](../README.md)
- Hyland Experience Insight Documentation
- Alfresco Content Services Documentation

## Support

For issues or questions:
1. Check component-specific documentation
2. Review application logs
3. Verify configuration against examples
4. Consult Hyland support resources
5. Report bugs via project issue tracker

---

**Last Updated:** November 2025
**Version:** 2.0.3-A.5
