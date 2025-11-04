# Bulk Ingester Configuration

## Overview

The Bulk Ingester is a Spring Boot application that performs batch ingestion of existing Alfresco Repository content into Hyland Experience Insight. It reads node data directly from the Alfresco database and publishes events for processing by the Live Ingester.

## Configuration Parameters

Configuration is provided via `application.yml` or environment variables. Below are all available configuration parameters with their default values.

### Database Configuration

The Bulk Ingester connects directly to the Alfresco database using a JDBC connection.

| Property | Default | Description | Required |
|----------|---------|-------------|----------|
| `spring.datasource.url` | - | JDBC connection URL (e.g., `jdbc:postgresql://localhost:5432/alfresco`) | Yes |
| `spring.datasource.username` | - | Database username | Yes |
| `spring.datasource.password` | - | Database password | Yes |
| `spring.datasource.hikari.maximumPoolSize` | `20` | Maximum number of connections in the pool | No |

### Bulk Ingestion Configuration

#### Node Parameters

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.bulk.ingest.node-params.fromId` | `0` | Starting node ID for ingestion (inclusive) |
| `alfresco.bulk.ingest.node-params.toId` | `20000000000` | Ending node ID for ingestion (exclusive) |

These parameters control the range of nodes to process. The ingestion processes all nodes with IDs in the range `[fromId, toId)`.

#### Repository Settings

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.bulk.ingest.repository.pageSize` | `2000` | Number of nodes to fetch per database query |

This controls batch size for database queries. Larger values may improve performance but increase memory usage.

#### Publisher Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.bulk.ingest.publisher.endpoint` | `activemq:queue:bulk-ingester-events` | ActiveMQ endpoint for publishing bulk ingester events |

#### Publisher Retry Settings

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.bulk.ingest.publisher.retry.attempts` | `10` | Number of retry attempts for event publishing |
| `alfresco.bulk.ingest.publisher.retry.initial-delay` | `500` | Initial delay before first retry (milliseconds) |
| `alfresco.bulk.ingest.publisher.retry.delay-multiplier` | `2` | Multiplier for delay between retries |

### Namespace Prefixes Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.bulk.ingest.namespace-prefixes-mapping` | `classpath:namespace-prefixes.json` | Location of namespace to prefix mapping file |

The Bulk Ingester requires a mapping file because namespace prefixes are not stored in the database. The default file includes common Alfresco namespaces. You can generate a custom mapping file using the provided Python script.

**Default namespace mappings include:**
- `http://www.alfresco.org/model/content/1.0` → `cm`
- `http://www.alfresco.org/model/system/1.0` → `sys`
- `http://www.alfresco.org/model/application/1.0` → `app`
- And many more...

### Reindexing Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `alfresco.reindex.pathCacheSize` | `10000` | Size of the LRU cache for node paths |

The path cache improves performance by caching frequently accessed node paths.

### Content Filtering Configuration

The Bulk Ingester supports the same filtering options as the Live Ingester.

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

## Retry Mechanism

The Bulk Ingester uses the same exponential backoff retry mechanism as other components:

- **Default attempts**: 10
- **Default initial delay**: 500ms
- **Default delay multiplier**: 2

### Retryable Exceptions

The retry mechanism is triggered for:
- `EndpointServerErrorException`
- `UnknownHostException`
- `MalformedURLException`
- `JsonEOFException`
- `MismatchedInputException`
- `HttpHostConnectException`
- `NoHttpResponseException`
- `MalformedChunkCodingException`

## Generating Namespace Prefixes Mapping

The Bulk Ingester includes a Python utility script to automatically generate the namespace prefixes mapping file from your Alfresco repository.

### Usage

```bash
python3 scripts/utils/namespaces-to-namespace-prefixes-file-generator.py \
  --alfresco-url http://localhost:8080/alfresco \
  --username admin \
  --password admin \
  --output bulk-ingester/src/main/resources/namespace-prefixes.json
```

### Command Options

| Option | Description | Required |
|--------|-------------|----------|
| `--alfresco-url` | Base URL of Alfresco Repository | Yes |
| `--username` | Admin username | Yes |
| `--password` | Admin password | Yes |
| `--output` | Output file path | No (default: `namespace-prefixes.json`) |

The script will:
1. Connect to the Alfresco Repository
2. Retrieve all registered namespaces
3. Generate a JSON mapping file
4. Save it to the specified location

## Example Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/alfresco
    username: alfresco
    password: alfresco
    hikari:
      maximumPoolSize: 20

alfresco:
  reindex:
    pathCacheSize: 10000
  bulk:
    ingest:
      node-params:
        fromId: 0
        toId: 20000000000
      repository:
        pageSize: 2000
      publisher:
        endpoint: activemq:queue:bulk-ingester-events
        retry:
          attempts: 10
          initial-delay: 500
          delay-multiplier: 2
      namespace-prefixes-mapping: classpath:namespace-prefixes.json
  filter:
    type:
      deny:
        - cm:thumbnail
        - cm:failedThumbnail
    path:
      deny:
        - /sys:*
```

## Environment Variables

All configuration properties can be overridden using environment variables. Convert property names to uppercase, replace dots with underscores, and remove hyphens:

```bash
# Database connection
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/alfresco
SPRING_DATASOURCE_USERNAME=alfresco
SPRING_DATASOURCE_PASSWORD=alfresco

# Node range
ALFRESCO_BULK_INGEST_NODEPARAMS_FROMID=0
ALFRESCO_BULK_INGEST_NODEPARAMS_TOID=20000000000

# Repository settings
ALFRESCO_BULK_INGEST_REPOSITORY_PAGESIZE=2000
```

## Docker Compose Configuration

When running the Bulk Ingester in Docker Compose, use environment variables to configure the service.

### Example Docker Compose Service

```yaml
bulk-ingester:
  image: quay.io/alfresco/alfresco-hxinsight-connector-bulk-ingester:${HXINSIGHT_CONNECTOR_TAG}
  deploy:
    resources:
      limits:
        memory: 512m
  depends_on:
    alfresco:
      condition: service_healthy
    postgres:
      condition: service_started
    activemq:
      condition: service_started
  environment:
    JAVA_TOOL_OPTIONS: -agentlib:jdwp=transport=dt_socket,address=*:5008,server=y,suspend=n
    LOGGING_LEVEL_ORG_ALFRESCO: INFO
    SPRING_ACTIVEMQ_BROKERURL: nio://activemq:61616
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/alfresco
    SPRING_DATASOURCE_USERNAME: alfresco
    SPRING_DATASOURCE_PASSWORD: alfresco
    ALFRESCO_BULK_INGEST_NODEPARAMS_FROMID: 0
    ALFRESCO_BULK_INGEST_NODEPARAMS_TOID: 20000000000
    ALFRESCO_BULK_INGEST_REPOSITORY_PAGESIZE: 2000
  ports:
    - "5008:5008"  # Debug port (optional)
```

### Key Configuration Points for Docker Compose

1. **Database Connection**: Use Docker service name for PostgreSQL
   - `jdbc:postgresql://postgres:5432/alfresco`

2. **ActiveMQ Broker**: Use service name with NIO protocol
   - `nio://activemq:61616`

3. **Dependencies**: Wait for Alfresco and database to be ready
   - Use `depends_on` with health checks

4. **Resource Limits**: Set appropriate memory (512m recommended for most cases)

5. **Debug Port**: Expose port 5008 for remote debugging (optional)

6. **Node Range**: Configure via environment variables
   - Set `FROMID` and `TOID` based on your repository size

### Production Configuration Example

For production environments with larger repositories:

```yaml
bulk-ingester:
  image: quay.io/alfresco/alfresco-hxinsight-connector-bulk-ingester:${HXINSIGHT_CONNECTOR_TAG}
  deploy:
    resources:
      limits:
        memory: 1g
      reservations:
        memory: 512m
  environment:
    LOGGING_LEVEL_ORG_ALFRESCO: WARN
    SPRING_ACTIVEMQ_BROKERURL: nio://activemq:61616
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/alfresco
    SPRING_DATASOURCE_USERNAME: alfresco
    SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}  # From secrets
    SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE: 50
    ALFRESCO_BULK_INGEST_REPOSITORY_PAGESIZE: 5000
    ALFRESCO_REINDEX_PATHCACHESIZE: 50000
```

### Complete Example Files

Complete Docker Compose examples are available in the project repository:

- **Full Stack**: `distribution/src/main/resources/docker-compose/docker-compose.yml`
  - Includes Bulk Ingester with all dependencies
  - Suitable for local development and testing

- **Minimal Setup**: `distribution/src/main/resources/docker-compose/docker-compose-minimal.yml`
  - Minimal configuration for quick testing

### Environment File

Use a `.env` file to manage versions:

```dotenv
HXINSIGHT_CONNECTOR_TAG=2.0.3-SNAPSHOT
POSTGRES_TAG=14.4
ACTIVE_MQ_TAG=5.18.5-jre17-rockylinux8
```

See `distribution/src/main/resources/docker-compose/.env` for the complete example.

### Running with Docker Compose

```bash
# Navigate to docker-compose directory
cd distribution/src/main/resources/docker-compose

# Start the full stack
docker compose up -d

# View Bulk Ingester logs
docker compose logs -f bulk-ingester

# Stop when complete
docker compose down
```

## Running the Bulk Ingester

### Prerequisites

1. Alfresco Repository must be running
2. ActiveMQ must be running and accessible
3. Live Ingester should be running to process the events
4. Database credentials must be configured

### Starting the Ingester

```bash
java -jar alfresco-hxinsight-connector-bulk-ingester-{version}-app.jar
```

### With Custom Configuration

```bash
java -jar alfresco-hxinsight-connector-bulk-ingester-{version}-app.jar \
  --spring.config.location=file:/path/to/application.yml
```

### Monitoring Progress

The Bulk Ingester processes nodes in batches and logs progress. Monitor the application logs to track:
- Number of nodes processed
- Current node ID range
- Any errors or skipped nodes
- Completion status

### Performance Tuning

To optimize performance:

1. **Increase page size**: For better throughput (at the cost of memory)
   ```yaml
   alfresco.bulk.ingest.repository.pageSize: 5000
   ```

2. **Adjust database pool**: For better database connection management
   ```yaml
   spring.datasource.hikari.maximumPoolSize: 50
   ```

3. **Increase path cache**: For repositories with deep folder structures
   ```yaml
   alfresco.reindex.pathCacheSize: 50000
   ```

4. **Filter unnecessary content**: Reduce processing time by filtering thumbnails and system nodes
   ```yaml
   alfresco.filter.type.deny:
     - cm:thumbnail
     - cm:failedThumbnail
   alfresco.filter.path.deny:
     - /sys:*
   ```

## Incremental Ingestion

To perform incremental ingestion (only new nodes since last run), adjust the `fromId` parameter:

```yaml
alfresco.bulk.ingest.node-params.fromId: 1000000  # Start from last processed ID
```

## Limitations

- Direct database access is required
- The ingester processes nodes sequentially by ID
- Deleted nodes may still be in the database and will be processed
- Content transformations are handled asynchronously by the Live Ingester
