# Live Ingester Configuration

[← Components](components.md) | [Installation →](installation.md)

The Live Ingester processes real-time events from Alfresco and sends documents to Hyland Experience Insight (HXI) for indexing. It is responsible for keeping the data in Knowledge Discovery up to date with the day-to-day changes made in Alfresco Content Services.

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
| `ALFRESCO_REPOSITORY_BASEURL` | Base URL for the Alfresco Repository REST API | Required |
| `ALFRESCO_REPOSITORY_EVENTSENDPOINT` | ActiveMQ topic to listen for repo events | `activemq:topic:alfresco.repo.event2` |
| `ALFRESCO_REPOSITORY_DISCOVERYENDPOINT` | Alfresco Discovery API URL (used to determine ACS version) | Derived from `base-url` |
| `ALFRESCO_REPOSITORY_VERSIONOVERRIDE` | ACS version string to use instead of calling Discovery API (e.g., `23.2.0`) | - |
| `ALFRESCO_REPOSITORY_HEALTHPROBE_ENDPOINT` | Health check endpoint for Alfresco | Derived from `base-url` |
| `ALFRESCO_REPOSITORY_HEALTHPROBE_TIMEOUTSECONDS` | Max time to wait for Alfresco to become healthy (seconds) | `1800` |
| `ALFRESCO_REPOSITORY_HEALTHPROBE_INTERVALSECONDS` | Interval between health checks (seconds) | `30` |

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
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID` | HXI OAuth client ID |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET` | HXI OAuth client secret |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI` | HXI OAuth token endpoint |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_ENVIRONMENTKEY` | HXI environment key (identifies your HXI tenant) |
| `AUTH_PROVIDERS_ALFRESCO_USERNAME` | Alfresco admin username (for API calls to ACS) |
| `AUTH_PROVIDERS_ALFRESCO_PASSWORD` | Alfresco admin password |

---

## Transform Service Configuration

The Live Ingester uses Alfresco Transform Service (ATS) to convert documents before sending to HX Insight.

```yaml
alfresco:
  transform:
    request:
      endpoint: activemq:queue:acs-repo-transform-request
      timeout: 20000
    response:
      queue-name: org.alfresco.hxinsight-connector.transform.response
      endpoint: activemq:queue:${alfresco.transform.response.queue-name}
    shared-file-store:
      base-url: http://shared-file-store:8099
      file-endpoint: ${alfresco.transform.shared-file-store.base-url}/alfresco/api/-default-/private/sfs/versions/1/file
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_TRANSFORM_REQUEST_ENDPOINT` | ActiveMQ queue to send transform requests to ATS | `activemq:queue:acs-repo-transform-request` |
| `ALFRESCO_TRANSFORM_REQUEST_TIMEOUT` | Max time to wait for transform to complete (ms) | `20000` |
| `ALFRESCO_TRANSFORM_RESPONSE_QUEUENAME` | Queue name for receiving transform responses | `org.alfresco.hxinsight-connector.transform.response` |
| `ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL` | Shared File Store base URL | - |
| `ALFRESCO_TRANSFORM_SHAREDFILESTORE_FILEENDPOINT` | Shared File Store endpoint for downloading transformed files (derived from `base-url`) | - |

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
        # Exact matches
        image/png: image/png
        image/bmp: image/png
        image/gif: image/png
        # Subtype wildcard - matches any image/* not listed above
        image/*: image/jpeg
        # Universal wildcard - catch-all for anything else
        "*": application/pdf
```

### Wildcard Matching

Wildcards allow you to handle groups of MIME types:

| Pattern | Matches |
|---------|---------|
| `image/*` | Any `image/` type (e.g., `image/png`, `image/webp`) |
| `text/*` | Any `text/` type (e.g., `text/plain`, `text/html`) |
| `application/*` | Any `application/` type |
| `*` | Everything (universal catch-all) |

### Lookup Order

**Exact matches always take precedence over wildcards:**

1. **Exact match** - e.g., `image/png` → `image/png`
2. **Subtype wildcard** - e.g., `image/*` → `image/jpeg`
3. **Universal wildcard** - e.g., `*` → `application/pdf`
4. **No match** - content is skipped (not ingested)

This means you can define specific handling for certain types while using wildcards as fallbacks.

### Skipping Content Types

To exclude a MIME type from content ingestion, map it to an empty string:

```yaml
alfresco:
  transform:
    mime-type:
      mapping:
        video/*: ""           # Skip all videos
        audio/*: ""           # Skip all audio
        application/*: application/pdf
        "*": application/pdf  # Everything else to PDF
```

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
        - st:site              # Site nodes
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
        - cm:titled            # Only content with titles
      deny:
        - sys:hidden           # Exclude hidden nodes
        - cm:workingcopy       # Exclude working copies
```

**By Path** - Use repository paths with wildcards:
```yaml
alfresco:
  filter:
    path:
      allow:
        - /app:company_home/st:sites/*/cm:documentLibrary/**  # All site document libraries
        - /app:company_home/cm:shared/**                       # Shared folder
      deny:
        - /app:company_home/st:sites/*/cm:dataLists/**        # Exclude data lists
        - /**/cm:temp/**                                       # Exclude temp folders anywhere
```

### Filter Logic

- If `allow` is empty, all nodes are allowed (unless denied)
- If `allow` has values, only matching nodes are allowed
- `deny` always takes precedence over `allow`
- Filters are combined with AND logic (node must pass type AND aspect AND path filters)

---

## Retry Configuration

All external calls support retry configuration with exponential backoff.

```yaml
hyland-experience:
  ingester:
    retry:
      attempts: 10
      initial-delay: 500
      delay-multiplier: 2
```

Available retry configurations:
- `auth.retry.*` - Authentication requests to HXI
- `alfresco.transform.shared-file-store.retry.*` - File downloads from Shared File Store
- `hyland-experience.storage.location.retry.*` - Storage location requests to HXI
- `hyland-experience.storage.upload.retry.*` - File uploads to HXI storage
- `hyland-experience.ingester.retry.*` - Ingestion event requests to HXI

| Property | Description | Default |
|----------|-------------|---------|
| `attempts` | Maximum retry attempts | `10` |
| `initial-delay` | Initial delay between retries (ms) | `500` |
| `delay-multiplier` | Multiplier for exponential backoff | `2` |

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
- `INFO` - Standard operational logging
- `DEBUG` - Full event payloads from Alfresco, detailed processing info
- `TRACE` - Very verbose, includes all HTTP requests/responses
