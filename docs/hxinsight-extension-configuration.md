# HxInsight Extension Configuration

## Overview

The HxInsight Extension is an Alfresco Repository AMP (Alfresco Module Package) that extends the repository with HxI-specific functionality. It provides REST APIs for managing predictions, integrates with Hyland Experience Insight for agent discovery and question answering, and handles prediction state management within the repository.

## Configuration Parameters

Configuration is provided via `alfresco-global.properties` file. Below are all available configuration parameters with their default values.

## Installation

The extension is deployed as an AMP to the Alfresco Repository:

```bash
java -jar alfresco-mmt.jar install \
  alfresco-hxinsight-connector-hxinsight-extension-{version}.amp \
  alfresco.war
```

## Service Account Configuration

| Property | Default | Description | Required |
|----------|---------|-------------|----------|
| `serviceaccount.role.service-account-hxi-connector` | `(empty)` | Service account role for HxI connector | No |

This property defines the role assigned to the HxI connector service account for accessing repository APIs.

## Authentication Configuration

### Hyland Experience OAuth2 Settings

| Property | Default | Description | Required |
|----------|---------|-------------|----------|
| `hxi.auth.providers.hyland-experience.type` | `oauth2` | Authentication provider type | Yes |
| `hxi.auth.providers.hyland-experience.grant-type` | `client_credentials` | OAuth2 grant type | Yes |
| `hxi.auth.providers.hyland-experience.client-name` | `acs-insight-connector` | OAuth2 client name | Yes |
| `hxi.auth.providers.hyland-experience.client-id` | - | OAuth2 client ID | Yes |
| `hxi.auth.providers.hyland-experience.client-secret` | - | OAuth2 client secret | Yes |
| `hxi.auth.providers.hyland-experience.scope` | `hxp.integrations` | OAuth2 scope | Yes |
| `hxi.auth.providers.hyland-experience.token-uri` | `http://localhost:8001/token` | OAuth2 token endpoint URL | Yes |
| `hxi.auth.providers.hyland-experience.environment-key` | `hxi-env-key` | HxI environment key | Yes |

### Authentication Retry Settings

| Property | Default | Description |
|----------|---------|-------------|
| `hxi.auth.retry.attempts` | `3` | Number of retry attempts for authentication failures |
| `hxi.auth.retry.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `hxi.auth.retry.delay-multiplier` | `2` | Multiplier for delay between subsequent retries |

**Note:** The extension uses fewer retry attempts (3) compared to other components (10) as it runs within the repository and should fail fast.

## Discovery Configuration

The extension integrates with HxI's discovery service for agent and question management.

| Property | Default | Description | Required |
|----------|---------|-------------|----------|
| `hxi.discovery.base-url` | `http://localhost:8001` | Base URL for HxI discovery service | Yes |
| `hxi.discovery.agents-endpoint` | `${hxi.discovery.base-url}/agent/integrations` | Endpoint for agent discovery | Yes |
| `hxi.discovery.questions-endpoint` | `${hxi.discovery.base-url}/qna/integrations` | Endpoint for question answering | Yes |

These endpoints are used to:
- Register and discover HxI agents
- Submit questions to HxI
- Retrieve answers and agent capabilities

## Question & Answer Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `hxi.question.max-context-size-for-question` | `100` | Maximum number of context items to include with a question |

This controls how much context (related documents, metadata, etc.) is sent to HxI when asking a question.

## Connector Metadata

| Property | Default | Description |
|----------|---------|-------------|
| `hxi.connector.version` | `@project.version@` | Connector version (injected during build) |
| `hxi.connector.source-id` | `a1f3e7c0-d193-7023-ce1d-0a63de491876` | Unique identifier for this connector instance |

These properties identify the connector to HxI services.

## Knowledge Retrieval Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `hxi.knowledge-retrieval.url` | `http://dummy-host.xyz/knowledge-retrieval/bots` | URL for knowledge retrieval service |

**Note:** This appears to be a placeholder/dummy URL in the default configuration and should be configured for production use.

## REST API Endpoints

The extension provides the following REST API endpoints:

### Prediction Management

- **Apply Predictions to Node**
  - Endpoint: `/api/-default-/private/hxi/versions/1/nodes/{nodeId}/predictions`
  - Method: `POST`
  - Description: Apply AI predictions to a specific node

- **Get Node Predictions**
  - Endpoint: `/api/-default-/private/hxi/versions/1/nodes/{nodeId}/predictions`
  - Method: `GET`
  - Description: Retrieve predictions for a node

- **List All Predictions**
  - Endpoint: `/api/-default-/private/hxi/versions/1/predictions`
  - Method: `GET`
  - Description: List all predictions in the system

### Node Entity Extensions

- **Enhanced Node Information**
  - Endpoint: `/api/-default-/private/hxi/versions/1/nodes/{nodeId}`
  - Method: `GET`
  - Description: Get node information with HxI-specific extensions

## Example Configuration

### Development Configuration

```properties
# Service Account
serviceaccount.role.service-account-hxi-connector=

# Authentication
hxi.auth.providers.hyland-experience.type=oauth2
hxi.auth.providers.hyland-experience.grant-type=client_credentials
hxi.auth.providers.hyland-experience.client-name=acs-insight-connector
hxi.auth.providers.hyland-experience.client-id=dev-client-id
hxi.auth.providers.hyland-experience.client-secret=dev-client-secret
hxi.auth.providers.hyland-experience.scope=hxp.integrations
hxi.auth.providers.hyland-experience.token-uri=http://localhost:8001/token
hxi.auth.providers.hyland-experience.environment-key=dev-env-key

# Authentication Retry
hxi.auth.retry.attempts=3
hxi.auth.retry.initial-delay=500
hxi.auth.retry.delay-multiplier=2

# Discovery
hxi.discovery.base-url=http://localhost:8001
hxi.discovery.agents-endpoint=${hxi.discovery.base-url}/agent/integrations
hxi.discovery.questions-endpoint=${hxi.discovery.base-url}/qna/integrations

# Question Configuration
hxi.question.max-context-size-for-question=100

# Connector Metadata
hxi.connector.version=@project.version@
hxi.connector.source-id=a1f3e7c0-d193-7023-ce1d-0a63de491876

# Knowledge Retrieval
hxi.knowledge-retrieval.url=http://dummy-host.xyz/knowledge-retrieval/bots
```

### Production Configuration

```properties
# Service Account
serviceaccount.role.service-account-hxi-connector=ROLE_HXI_CONNECTOR

# Authentication
hxi.auth.providers.hyland-experience.type=oauth2
hxi.auth.providers.hyland-experience.grant-type=client_credentials
hxi.auth.providers.hyland-experience.client-name=acs-insight-connector
hxi.auth.providers.hyland-experience.client-id=${HXI_CLIENT_ID}
hxi.auth.providers.hyland-experience.client-secret=${HXI_CLIENT_SECRET}
hxi.auth.providers.hyland-experience.scope=hxp.integrations
hxi.auth.providers.hyland-experience.token-uri=https://auth.hyland.com/token
hxi.auth.providers.hyland-experience.environment-key=${HXI_ENVIRONMENT_KEY}

# Authentication Retry
hxi.auth.retry.attempts=5
hxi.auth.retry.initial-delay=1000
hxi.auth.retry.delay-multiplier=2

# Discovery
hxi.discovery.base-url=https://insight.hyland.com
hxi.discovery.agents-endpoint=${hxi.discovery.base-url}/agent/integrations
hxi.discovery.questions-endpoint=${hxi.discovery.base-url}/qna/integrations

# Question Configuration
hxi.question.max-context-size-for-question=150

# Connector Metadata
hxi.connector.version=@project.version@
hxi.connector.source-id=a1f3e7c0-d193-7023-ce1d-0a63de491876

# Knowledge Retrieval
hxi.knowledge-retrieval.url=https://knowledge.hyland.com/knowledge-retrieval/bots
```

## Environment Variables

In containerized deployments, sensitive configuration can be externalized using environment variables:

```bash
# Authentication
export HXI_CLIENT_ID="your-client-id"
export HXI_CLIENT_SECRET="your-client-secret"
export HXI_ENVIRONMENT_KEY="your-env-key"

# Then reference in alfresco-global.properties:
# hxi.auth.providers.hyland-experience.client-id=${HXI_CLIENT_ID}
# hxi.auth.providers.hyland-experience.client-secret=${HXI_CLIENT_SECRET}
# hxi.auth.providers.hyland-experience.environment-key=${HXI_ENVIRONMENT_KEY}
```

## Property Override Locations

Configuration properties can be placed in:

1. **AMP Module**: `alfresco/module/alfresco-hxinsight-connector-hxinsight-extension/alfresco-global.properties`
   - Default values provided by the extension

2. **Shared Configuration**: `tomcat/shared/classes/alfresco-global.properties`
   - System administrator overrides (recommended)

3. **Environment Variables**: As `${VARIABLE_NAME}` placeholders
   - For sensitive data in containerized environments

## Retry Mechanism

The extension uses a conservative retry mechanism for authentication:

- **Default attempts**: 3 (fewer than other components)
- **Default initial delay**: 500ms
- **Default delay multiplier**: 2

This is intentional as the extension runs within the repository and should fail fast to avoid blocking repository operations.

### Retryable Exceptions

- `EndpointServerErrorException`
- `UnknownHostException`
- `MalformedURLException`
- `JsonEOFException`
- `MismatchedInputException`

## Spring Bean Configuration

The extension registers several Spring beans for integration:

- `predictionService`: Core service for prediction management
- `PredictionService`: Proxied service with transaction and security interceptors
- `predictionMapper`: Maps predictions to repository structures
- `hxInsightClient`: HTTP client for HxI communication
- `hxInsightAuthClient`: Authentication client
- `hxInsightAuthService`: Authentication service

## Security Considerations

### Service Account

Configure a dedicated service account role for HxI operations:

```properties
serviceaccount.role.service-account-hxi-connector=ROLE_HXI_CONNECTOR
```

Then assign appropriate permissions to this role in your repository security configuration.

### Authentication

**Production Best Practices:**

1. **Never commit credentials**: Use environment variables or secret management
2. **Rotate credentials**: Regularly update client secrets
3. **Limit scope**: Only request necessary OAuth2 scopes
4. **Secure token endpoint**: Use HTTPS for `token-uri`
5. **Monitor access**: Review authentication logs regularly

### API Access

The REST APIs are exposed under the private API namespace:
- `/api/-default-/private/hxi/versions/1/*`

These should be:
- Protected by Alfresco's security layer
- Only accessible to authenticated users with appropriate permissions
- Not exposed directly to public networks

## Troubleshooting

### Authentication Failures

1. Verify `client-id` and `client-secret`
2. Check `token-uri` is accessible from the repository
3. Ensure `environment-key` matches HxI configuration
4. Review retry settings if network is unstable

### Discovery Endpoint Issues

1. Verify `hxi.discovery.base-url` is correct
2. Check network connectivity from repository to HxI
3. Ensure authentication is working
4. Review firewall rules

### Prediction API Errors

1. Check node permissions for the authenticated user
2. Verify prediction format matches expected schema
3. Review repository logs for detailed error messages
4. Ensure HxI services are operational

## Performance Tuning

### Question Context Size

Reduce context size for better performance:

```properties
hxi.question.max-context-size-for-question=50
```

Or increase for better accuracy:

```properties
hxi.question.max-context-size-for-question=200
```

**Trade-offs:**
- Larger context provides better answers but increases payload size
- Smaller context reduces network traffic but may reduce answer quality

### Retry Configuration

For production environments with reliable networks:

```properties
hxi.auth.retry.attempts=2
hxi.auth.retry.initial-delay=250
```

For unstable networks:

```properties
hxi.auth.retry.attempts=5
hxi.auth.retry.initial-delay=1000
hxi.auth.retry.delay-multiplier=1.5
```

## Monitoring

### Repository Logs

Monitor the following log categories:

```
org.alfresco.hxi_connector.hxi_extension
org.alfresco.hxi_connector.common.adapters.auth
```

### Metrics to Track

- Authentication success/failure rate
- Prediction API response times
- Discovery endpoint availability
- Question processing duration

## Integration Testing

The extension includes integration tests that can be run against a live repository:

```bash
mvn verify -pl hxinsight-extension
```

## Version Compatibility

The extension is compatible with:
- Alfresco Content Services 7.x and later
- Hyland Experience Insight (as per compatibility matrix)

Check the main project README for specific version requirements.
