# Bulk Ingester Configuration

[← Components](components.md) | [Installation →](installation.md)

The Bulk Ingester reads directly from the Alfresco database to batch-ingest existing content into Knowledge Discovery. It is responsible for the initial ingestion of documents, or recovering after a period of downtime.

## Required Configuration

### Database Connection

The Bulk Ingester requires direct read access to the Alfresco database.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/alfresco
    username: alfresco
    password: alfresco
```

| Environment Variable | Description |
|---------------------|-------------|
| `SPRING_DATASOURCE_URL` | JDBC connection URL to Alfresco database |
| `SPRING_DATASOURCE_USERNAME` | Database username (read access required) |
| `SPRING_DATASOURCE_PASSWORD` | Database password |

### ActiveMQ Connection

```yaml
spring:
  activemq:
    broker-url: nio://activemq:61616
```

| Environment Variable | Description |
|---------------------|-------------|
| `SPRING_ACTIVEMQ_BROKERURL` | ActiveMQ broker URL |

---

## Ingestion Configuration

### Node ID Range

Control which nodes to ingest by specifying database node ID ranges. This allows you to:
- Process specific batches of content
- Resume interrupted ingestion runs
- Split large ingestions across multiple instances

```yaml
alfresco:
  bulk:
    ingest:
      node-params:
        from-id: 0
        to-id: 1000000
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_BULK_INGEST_NODEPARAMS_FROMID` | Starting node ID (inclusive) | `0` |
| `ALFRESCO_BULK_INGEST_NODEPARAMS_TOID` | Ending node ID (inclusive) | Required |

> **Tip:** Query your database to find the max node ID: `SELECT MAX(id) FROM alf_node;`

### Repository Settings

```yaml
alfresco:
  bulk:
    ingest:
      repository:
        page-size: 100
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_BULK_INGEST_REPOSITORY_PAGESIZE` | Number of nodes to fetch per database query | `100` |

### Event Publisher

```yaml
alfresco:
  bulk:
    ingest:
      publisher:
        endpoint: activemq:queue:bulk-ingester-events
```

| Environment Variable | Description |
|---------------------|-------------|
| `ALFRESCO_BULK_INGEST_PUBLISHER_ENDPOINT` | ActiveMQ queue for publishing ingestion events |

---

## Node Filtering

Control which nodes are ingested using allow/deny lists. Filters are applied in order: a node must pass all allow lists (if specified) and not match any deny lists.

```yaml
alfresco:
  filter:
    aspect:
      allow: []
      deny: []
    type:
      allow: []
      deny: []
    path:
      allow: []
      deny: []
```

| Environment Variable | Description |
|---------------------|-------------|
| `ALFRESCO_FILTER_ASPECT_ALLOW` | Aspects to include (comma-separated) |
| `ALFRESCO_FILTER_ASPECT_DENY` | Aspects to exclude |
| `ALFRESCO_FILTER_TYPE_ALLOW` | Node types to include |
| `ALFRESCO_FILTER_TYPE_DENY` | Node types to exclude |
| `ALFRESCO_FILTER_PATH_ALLOW` | Paths to include |
| `ALFRESCO_FILTER_PATH_DENY` | Paths to exclude |

### Filter Examples

**By Node Type** - Use the prefixed QName format:
```yaml
alfresco:
  filter:
    type:
      allow:
        - cm:content           # Standard content nodes
        - custom:document      # Custom type from your model
      deny:
        - cm:thumbnail         # Exclude thumbnails
        - cm:failedThumbnail
```

**By Aspect** - Use the prefixed QName format:
```yaml
alfresco:
  filter:
    aspect:
      allow:
        - cm:versionable       # Only versioned content
      deny:
        - sys:hidden           # Exclude hidden nodes
        - cm:workingcopy       # Exclude working copies
```

**By Path** - Use repository paths:
```yaml
alfresco:
  filter:
    path:
      allow:
        - /app:company_home/st:sites/*/cm:documentLibrary/**  # Site document libraries
      deny:
        - /app:company_home/st:sites/*/cm:dataLists/**        # Exclude data lists
```

---

## Namespace Prefix Mapping

The Alfresco database stores QNames as full namespace URIs, not prefixed names. You must provide a mapping file so the Bulk Ingester can convert them to the prefixed format expected by HX Insight.

```yaml
alfresco:
  bulk:
    ingest:
      namespace-prefixes-mapping: classpath:namespace-prefixes.json
```

| Environment Variable | Description |
|---------------------|-------------|
| `ALFRESCO_BULK_INGEST_NAMESPACEPREFIXESMAPPING` | Path to namespace mapping file |

### Generating the Mapping File

Use the provided script to generate the mapping from your Alfresco instance:

```bash
python3 scripts/utils/namespaces-to-namespace-prefixes-file-generator.py \
  --url http://localhost:8080/alfresco \
  --username admin \
  --password admin \
  --output namespace-prefixes.json
```

### Example Mapping File

```json
{
  "http://www.alfresco.org/model/content/1.0": "cm",
  "http://www.alfresco.org/model/system/1.0": "sys",
  "http://www.alfresco.org/model/dictionary/1.0": "d",
  "http://www.alfresco.org/model/site/1.0": "st",
  "http://www.alfresco.org/model/application/1.0": "app",
  "http://www.mycompany.com/model/custom/1.0": "custom"
}
```

> **Important:** Include mappings for all custom content models used in your repository.

---

## Logging

```yaml
logging:
  level:
    org.alfresco: INFO
```

| Environment Variable | Description |
|---------------------|-------------|
| `LOGGING_LEVEL_ORG_ALFRESCO` | Log level for connector classes |

**Useful log levels:**
- `INFO` - Progress updates, summary statistics
- `DEBUG` - Individual node processing details
