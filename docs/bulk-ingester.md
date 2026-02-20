# Bulk Ingester Configuration

[← Components](components.md) | [Installation →](installation.md)

The Bulk Ingester reads directly from the Alfresco database to batch-ingest existing content into Knowledge Discovery. It is responsible for the initial ingestion of documents, or recovering after a period of downtime.

> **Default configuration:** See [`application.yml`](../bulk-ingester/src/main/resources/application.yml) for all defaults.

## Required Configuration

### Database Connection

The Bulk Ingester requires direct read access to the Alfresco database.

| Environment Variable | Description |
|---------------------|-------------|
| `SPRING_DATASOURCE_URL` | JDBC connection URL (string, e.g., `jdbc:postgresql://postgres:5432/alfresco`) |
| `SPRING_DATASOURCE_USERNAME` | Database username (string, read access required) |
| `SPRING_DATASOURCE_PASSWORD` | Database password (string) |
| `SPRING_ACTIVEMQ_BROKERURL` | ActiveMQ broker URL (string) |

---

## How Bulk Ingestion Works

The Bulk Ingester does not send documents directly to HX Insight. Instead, it:

1. Reads node metadata from the Alfresco database
2. Publishes events to an ActiveMQ queue
3. The **Live Ingester** consumes these events and handles the actual ingestion (transforms, uploads, etc.)

> **Important:** The Live Ingester must be running and configured with a matching `ALFRESCO_BULKINGESTER_ENDPOINT` to process bulk ingestion events.

Additionally it is possible to configure filters in both the Bulk and Live Ingester, and discrepancies in these may result in data being loaded from the database, but not sent on to HX Insight.

---

## Ingestion Configuration

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_BULK_INGEST_NODEPARAMS_FROMID` | Starting node ID, inclusive (integer) | `0` |
| `ALFRESCO_BULK_INGEST_NODEPARAMS_TOID` | Ending node ID, inclusive (integer) | Required |
| `ALFRESCO_BULK_INGEST_REPOSITORY_PAGESIZE` | Nodes per database query (integer) | `2000` |
| `ALFRESCO_BULK_INGEST_PUBLISHER_ENDPOINT` | ActiveMQ queue for ingestion events (string) | `activemq:queue:bulk-ingester-events` |
| `ALFRESCO_BULK_INGEST_PUBLISHER_RETRY_ATTEMPTS` | Retry attempts for publishing events (integer) | `10` |
| `ALFRESCO_BULK_INGEST_PUBLISHER_RETRY_INITIALDELAY` | Initial delay between retries in ms (integer) | `500` |
| `ALFRESCO_BULK_INGEST_PUBLISHER_RETRY_DELAYMULTIPLIER` | Multiplier for exponential backoff (integer) | `2` |
| `ALFRESCO_REINDEX_PATHCACHESIZE` | Cache size for path lookups (integer) | `10000` |
| `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE` | Database connection pool size (integer) | `20` |

Use node ID ranges to process specific batches, resume interrupted runs, or split ingestion across instances.

> **Tip:** Find max node ID: `SELECT MAX(id) FROM alf_node;`

---

## Node Filtering

Control which nodes are ingested using allow/deny lists. A node must pass all allow lists (if specified) and not match any deny lists.

| Environment Variable | Description |
|---------------------|-------------|
| `ALFRESCO_FILTER_ASPECT_ALLOW` | Aspects to include (comma-separated) |
| `ALFRESCO_FILTER_ASPECT_DENY` | Aspects to exclude |
| `ALFRESCO_FILTER_TYPE_ALLOW` | Node types to include |
| `ALFRESCO_FILTER_TYPE_DENY` | Node types to exclude |
| `ALFRESCO_FILTER_PATH_ALLOW` | Paths to include |
| `ALFRESCO_FILTER_PATH_DENY` | Paths to exclude |

### Filter Examples

```yaml
alfresco:
  filter:
    type:
      allow: [cm:content, custom:document]
      deny: [cm:thumbnail, cm:failedThumbnail]
    aspect:
      allow: [cm:versionable]
      deny: [sys:hidden, cm:workingcopy]
    path:
      allow: [/app:company_home/st:sites/*/cm:documentLibrary/**]
      deny: [/app:company_home/st:sites/*/cm:dataLists/**]
```

Use prefixed QName format for types and aspects. Paths support wildcards (`*` for single segment, `**` for multiple).

---

## Namespace Prefix Mapping

The Alfresco database stores QNames as full namespace URIs. You must provide a mapping file to convert them to the prefixed format expected by HX Insight.

| Environment Variable | Description |
|---------------------|-------------|
| `ALFRESCO_BULK_INGEST_NAMESPACEPREFIXESMAPPING` | Path to namespace mapping file |

Generate the mapping from your Alfresco instance:

```bash
python3 scripts/utils/namespaces-to-namespace-prefixes-file-generator.py \
  --url http://localhost:8080/alfresco --username admin --password admin --output namespace-prefixes.json
```

The mapping file is JSON: `{"http://www.alfresco.org/model/content/1.0": "cm", ...}`. Include mappings for all custom content models.

---

## Logging

The Bulk Ingester uses [Spring Boot's logging configuration](https://docs.spring.io/spring-boot/reference/features/logging.html). Log levels can be set via environment variables or command-line arguments.

### Setting Log Levels

**Environment variables** (Docker/Kubernetes):
```bash
LOGGING_LEVEL_ORG_ALFRESCO=DEBUG
```

**Command line** (JAR deployment):
```bash
java -jar alfresco-hxinsight-connector-bulk-ingester-*.jar \
  --logging.level.org.alfresco=DEBUG
```

### Common Packages

| Environment Variable | Description |
|---------------------|-------------|
| `LOGGING_LEVEL_ORG_ALFRESCO` | All connector classes |
| `LOGGING_LEVEL_ORG_ALFRESCO_HXI_CONNECTOR_BULK_INGESTER` | Bulk Ingester specific |
| `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK` | Spring Framework |

### Recommended Levels

- **Production:** `INFO` - Shows progress updates and summary statistics
- **Troubleshooting:** `DEBUG` - Shows individual node processing details
