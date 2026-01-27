# Live Ingester Configuration

[← Components](components.md) | [Installation →](installation.md)

The Live Ingester processes real-time events from Alfresco and sends documents to Hyland Experience Insight (HXI) for indexing. It is responsible for keeping the data in Knowledge Discovery up to date with the day-to-day changes made in Alfresco Content Services.

> **Default configuration:** See [`application.yml`](../live-ingester/src/main/resources/application.yml) for all defaults.

## Required Configuration

### ActiveMQ Connection

```yaml
spring:
  activemq:
    broker-url: nio://activemq:61616
```

| Environment Variable | Description |
|---------------------|-------------|
| `SPRING_ACTIVEMQ_BROKERURL` | ActiveMQ broker URL |

### Alfresco Repository

```yaml
alfresco:
  repository:
    base-url: http://alfresco:8080/alfresco
    events-endpoint: activemq:topic:alfresco.repo.event2
    discovery-endpoint: ${alfresco.repository.base-url}/api/discovery
    health-probe:
      endpoint: ${alfresco.repository.base-url}
      timeout-seconds: 1800
      interval-seconds: 30
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_REPOSITORY_BASEURL` | Base URL for the Alfresco Repository REST API (URL) | Required |
| `ALFRESCO_REPOSITORY_EVENTSENDPOINT` | ActiveMQ topic to listen for repo events (string) | `activemq:topic:alfresco.repo.event2` |
| `ALFRESCO_REPOSITORY_DISCOVERYENDPOINT` | Alfresco Discovery API URL (URL) | Derived from `base-url` |
| `ALFRESCO_REPOSITORY_VERSIONOVERRIDE` | ACS version string to use instead of calling Discovery API (string, e.g., `23.2.0`) | - |
| `ALFRESCO_REPOSITORY_HEALTHPROBE_ENDPOINT` | Health check endpoint for Alfresco (URL) | Derived from `base-url` |
| `ALFRESCO_REPOSITORY_HEALTHPROBE_TIMEOUTSECONDS` | Max time to wait for Alfresco to become healthy in seconds (integer) | `1800` |
| `ALFRESCO_REPOSITORY_HEALTHPROBE_INTERVALSECONDS` | Interval between health checks in seconds (integer) | `30` |

> **Note:** The connector needs to know the ACS version. Choose one approach:
> - **Automatic detection (recommended):** Set `base-url` and leave `version-override` empty. The connector will call the Alfresco Discovery API at startup to determine the ACS version.
> - **Manual override:** Set `version-override` to skip the Discovery API call (e.g., `23.2.0`). Useful if the Alfresco Discovery API is inaccessible.

### HX Insight Ingestion

The most common configuration is to set the `base-url` for HX Insight. The storage and ingestion endpoints are automatically derived from this base URL.

```yaml
hyland-experience:
  insight:
    ingestion:
      base-url: https://hxinsight.hyland.com
```

| Environment Variable | Description |
|---------------------|-------------|
| `HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL` | HXI base URL (provided by Hyland). Endpoints for storage and ingestion are derived from this. |

**Advanced:** If you need to override the derived endpoints individually:

| Environment Variable | Description | Derived From |
|---------------------|-------------|--------------|
| `HYLANDEXPERIENCE_STORAGE_LOCATION_ENDPOINT` | HXI API endpoint to request pre-signed URLs for uploading binary content | `${base-url}/presigned-urls` |
| `HYLANDEXPERIENCE_INGESTER_ENDPOINT` | HXI API endpoint for sending ingestion events (metadata and content references) | `${base-url}/ingestion-events` |

### Authentication

```yaml
auth:
  providers:
    hyland-experience:
      type: oauth2
      grant-type: client_credentials
      client-id: <your-client-id>
      client-secret: <your-client-secret>
      token-uri: https://auth.hyland.com/oauth/token
      environment-key: <your-env-key>
    alfresco:
      type: basic
      username: admin
      password: admin
```

| Environment Variable | Description |
|---------------------|-------------|
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID` | HXI OAuth client ID (string) |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET` | HXI OAuth client secret (string) |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI` | HXI OAuth token endpoint (URL) |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_GRANTTYPE` | OAuth grant type (string, default: `client_credentials`) |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_ENVIRONMENTKEY` | HXI environment key (string, identifies your HXI tenant) |
| `AUTH_PROVIDERS_ALFRESCO_USERNAME` | Alfresco admin username (string, for API calls to ACS) |
| `AUTH_PROVIDERS_ALFRESCO_PASSWORD` | Alfresco admin password (string) |

### Application Identity

| Environment Variable | Description |
|---------------------|-------------|
| `APPLICATION_SOURCEID` | Unique identifier for the Alfresco instance (UUID). Used to identify the source of ingested content in HX Insight. Generate a new UUID for each deployment. |

---

## Bulk Ingester Integration

The Live Ingester receives events from the Bulk Ingester via an ActiveMQ queue. This allows the Bulk Ingester to discover documents from the database while the Live Ingester handles the actual ingestion to HX Insight.

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_BULKINGESTER_ENDPOINT` | ActiveMQ queue to receive bulk ingestion events (string) | `activemq:queue:bulk-ingester-events` |

> **Important:** This endpoint must match the `ALFRESCO_BULK_INGEST_PUBLISHER_ENDPOINT` configured in the [Bulk Ingester](bulk-ingester.md).

---

## Transform Service Configuration

The Live Ingester uses Alfresco Transform Service (ATS) to convert documents before sending to HX Insight.

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_TRANSFORM_REQUEST_ENDPOINT` | ActiveMQ queue to send transform requests to ATS (string) | `activemq:queue:acs-repo-transform-request` |
| `ALFRESCO_TRANSFORM_REQUEST_TIMEOUT` | Max time to wait for transform to complete in ms (integer) | `20000` |
| `ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL` | Shared File Store base URL (URL) | Required |
| `ALFRESCO_TRANSFORM_RESPONSE_QUEUENAME` | Queue name for receiving transform responses (string) | `org.alfresco.hxinsight-connector.transform.response` |
| `ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_ATTEMPTS` | Retry attempts for failed ingestion after transform (integer, `-1` = unlimited) | `-1` |

---

## MIME Type Mapping

The Live Ingester transforms content before sending it to HX Insight. The `mime-type.mapping` configuration controls which source MIME types are transformed and what target format they become.

### Default Mappings

By default, content is transformed to **PDF** or **image** formats:

| Source MIME Type | Target MIME Type |
|-----------------|------------------|
| `image/png` | `image/png` (unchanged) |
| `image/bmp` | `image/png` |
| `image/tiff` | `image/png` |
| `image/gif` | `image/png` |
| `image/raw` | `image/png` |
| `image/*` (other images) | `image/jpeg` |
| `application/*` (all applications) | `application/pdf` |
| `text/*` (all text) | `application/pdf` |

### Custom Mappings

Override the defaults by providing your own mapping:

```yaml
alfresco:
  transform:
    mime-type:
      mapping:
        image/png: image/png    # Exact match (highest priority)
        image/*: image/jpeg     # Subtype wildcard
        video/*: ""             # Empty string = skip this type
        "*": application/pdf    # Universal catch-all (lowest priority)
```

**Lookup order:** Exact match → Subtype wildcard (`type/*`) → Universal wildcard (`*`) → No match (skipped)

### Transform with CIC Document Filters

Hyland's [Content Innovation Cloud](https://www.hyland.com/en/platform) includes [Document Filters](https://www.hyland.com/en/solutions/products/document-filters), which can transform many file types. If Alfresco Transform Services cannot convert a particular file type to PDF, you can map the file type to itself to upload it without transformation. After upload, Document Filters will attempt to convert it to a readable format.

### Transform Options

Additional options can be passed to ATS for specific target types:

```yaml
alfresco:
  transform:
    request:
      timeout: 20000
      options:
        application/pdf:
          # Options passed to ATS when transforming TO PDF
          someOption: value
```

### Setting MIME Type Mappings in Docker Compose

MIME type mappings **cannot be set using simple environment variables** because MIME type keys contain slashes. Use one of these approaches:

- **`SPRING_APPLICATION_JSON`** - Pass mappings as JSON (see [Spring Boot docs](https://docs.spring.io/spring-boot/reference/features/external-config.html))
- **Mount a config file** - Mount a volume pointing to a custom `application.yml` and set `SPRING_CONFIG_LOCATION=file:/path/to/application.yml`

---

## Node Filtering

Control which nodes are ingested using allow/deny lists. Filters are applied in order: a node must pass all allow lists (if specified) and not match any deny lists.

| Environment Variable | Description |
|---------------------|-------------|
| `ALFRESCO_FILTER_ASPECT_ALLOW` | Aspects to include (comma-separated) |
| `ALFRESCO_FILTER_ASPECT_DENY` | Aspects to exclude |
| `ALFRESCO_FILTER_TYPE_ALLOW` | Node types to include |
| `ALFRESCO_FILTER_TYPE_DENY` | Node types to exclude |
| `ALFRESCO_FILTER_PATH_ALLOW` | Paths to include |
| `ALFRESCO_FILTER_PATH_DENY` | Paths to exclude |

### Example

```yaml
alfresco:
  filter:
    type:
      allow: [cm:content, st:site, custom:document]
      deny: [cm:thumbnail, cm:failedThumbnail]
    aspect:
      allow: [cm:versionable, cm:titled]
      deny: [sys:hidden, cm:workingcopy]
    path:
      allow:
        - /app:company_home/st:sites/*/cm:documentLibrary/**
        - /app:company_home/cm:shared/**
      deny:
        - /app:company_home/st:sites/*/cm:dataLists/**
        - /**/cm:temp/**
```

Use prefixed QName format for types and aspects. Paths support wildcards (`*` for single segment, `**` for multiple).

### Filter Logic

- If `allow` is empty, all nodes are allowed (unless denied)
- If `allow` has values, only matching nodes are allowed
- `deny` always takes precedence over `allow`
- Filters are combined with AND logic (node must pass type AND aspect AND path filters)

---

## Retry Configuration

All external calls support retry configuration with exponential backoff. Configure via `*.retry.attempts`, `*.retry.initial-delay`, and `*.retry.delay-multiplier` for these prefixes:

- `auth` - Authentication requests
- `alfresco.transform.shared-file-store` - File downloads from Shared File Store
- `hyland-experience.storage.location` - Storage location requests
- `hyland-experience.storage.upload` - File uploads to HXI storage
- `hyland-experience.ingester` - Ingestion event requests

Default: 10 attempts, 500ms initial delay, 2x multiplier.

---

## Logging

The Live Ingester uses [Spring Boot's logging configuration](https://docs.spring.io/spring-boot/reference/features/logging.html). Log levels can be set via environment variables or command-line arguments.

### Setting Log Levels

**Environment variables** (Docker/Kubernetes):
```bash
LOGGING_LEVEL_ORG_ALFRESCO=DEBUG
LOGGING_LEVEL_ORG_APACHE_CAMEL=WARN
```

**Command line** (JAR deployment):
```bash
java -jar alfresco-hxinsight-connector-live-ingester-*.jar \
  --logging.level.org.alfresco=DEBUG
```

### Common Packages

| Environment Variable | Description |
|---------------------|-------------|
| `LOGGING_LEVEL_ORG_ALFRESCO` | All connector classes |
| `LOGGING_LEVEL_ORG_ALFRESCO_HXI_CONNECTOR_LIVE_INGESTER` | Live Ingester specific |
| `LOGGING_LEVEL_ORG_APACHE_CAMEL` | Apache Camel routing |
| `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK` | Spring Framework |
