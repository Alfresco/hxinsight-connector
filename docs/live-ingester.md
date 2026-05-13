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
| `ALFRESCO_REPOSITORY_RESPONSETIMEOUTMS` | Per-request response timeout (ms) for ACS REST content downloads. `0` (default) leaves the underlying Camel HTTP client at its built-in timeout — a slow or unresponsive ACS can pin a route worker thread until the JVM is restarted. Set a positive value (e.g., `30000`) in production to bound per-request wait and surface a slow ACS as a timeout that can be retried / dead-lettered. (integer ms, `>= 0`) | `0` |

> **Note:** The connector needs to know the ACS version. Choose one approach:
> - **Automatic detection (recommended):** Set `base-url` and leave `version-override` empty. The connector will call the Alfresco Discovery API at startup to determine the ACS version.
> - **Manual override:** Set `version-override` to skip the Discovery API call (e.g., `23.2.0`). Useful if the Alfresco Discovery API is inaccessible.

#### Durable event subscription (recommended)

By default, the Live Ingester subscribes to `alfresco.repo.event2` as a **non-durable** topic consumer. If the broker connection drops (network partition, broker restart, ingester restart), any repo events published while the consumer is detached are silently dropped by ActiveMQ. This is acceptable for a development setup but causes data drift in production.

Opt in to a **durable** subscription so that the broker retains events for this consumer across disconnects and replays them on reconnect:

```yaml
alfresco:
  repository:
    events-subscription:
      durable: true
      name: LiveIngesterSubscription   # any unique identifier; keep stable across restarts
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DURABLE` | Set to `true` to use a durable topic subscription so events published while the Live Ingester is disconnected are retained by the broker and replayed on reconnect. (boolean) | `false` |
| `ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_NAME` | Subscription identifier and JMS connection clientId. Must be unique per Live Ingester deployment and **must remain stable across restarts** — changing it abandons the existing subscription on the broker (which keeps accumulating events until cleaned up). (string) | `LiveIngesterSubscription` |

**Broker requirements:** ActiveMQ Classic 5.15+ (tested on 5.18.x and 6.1.x). Both `kahadb` and `jdbc` persistence stores work; the broker must be configured with `persistent="true"` (the default).

**Operational notes:**
- Durable subscriptions persist on the broker until explicitly unsubscribed. If you decommission a Live Ingester instance, run `Unsubscribe` on the broker (Web Console → Subscribers → Offline Durable Topic Subscribers → Delete) or events will accumulate forever for the abandoned subscription.
- The configured `name` is also set as the JMS connection `clientId` on **all** of this Live Ingester's JMS connections (the `bulk-ingester-events` and transform-response queues too), because durable topic subscriptions require a unique `clientId` per the JMS specification. The broker will refuse a second simultaneous connection from the same `clientId`. **Run only one Live Ingester instance** with the durable opt-in enabled. Multi-instance HA is tracked under `RB-003` in the reliability bug log and requires a separate fix.
- The `events-endpoint` URI itself remains unchanged (`activemq:topic:alfresco.repo.event2` by default). The Live Ingester appends `?subscriptionDurable=true&durableSubscriptionName=<name>` internally when the opt-in is on.

**Rollback:** Set `ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DURABLE=false` (or unset) and restart. The Live Ingester reverts to a non-durable subscription. Remember to unsubscribe the orphaned durable subscription on the broker.

#### Repo events: dead-letter channel

Exceptions thrown while processing a repo event (malformed payload, unexpected NPE, transient downstream failure) are handled by an explicit Camel `DeadLetterChannel` on the route. Each failure is retried with exponential backoff up to a configurable maximum; if all retries are exhausted the **original** message is moved to a dead-letter queue and a Micrometer counter is incremented so operators can alert on it.

```yaml
alfresco:
  repository:
    events-subscription:
      dead-letter-enabled: true                      # opt-in; default is false (broker-side redelivery)
      dead-letter-uri: activemq:queue:ActiveMQ.DLQ   # default
      maximum-redeliveries: 6                        # default
      redelivery-delay-ms: 1000                      # default; exponential backoff: 1s, 2s, 4s, 8s, 16s, 32s
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERENABLED` | When `true`, install the route-level Camel `errorHandler(deadLetterChannel(...))` described above. When `false` (default), the route falls back to Camel's `DefaultErrorHandler` plus the broker's redelivery semantics; per-failure logs and the `live_ingester_repo_events_dlq_total` counter are not produced. Opt in for the structured DLQ inventory + retry budget; leave off for master-equivalent behaviour. (boolean) | `false` |
| `ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERURI` | Camel endpoint URI used as the dead-letter destination for repo events. Only takes effect when `dead-letter-enabled=true`. (string) | `activemq:queue:ActiveMQ.DLQ` |
| `ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_MAXIMUMREDELIVERIES` | Maximum number of redelivery attempts before the message is moved to the dead-letter URI. Only takes effect when `dead-letter-enabled=true`. (integer; `0` disables retries) | `6` |
| `ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_REDELIVERYDELAYMS` | Initial delay between redelivery attempts in milliseconds. Exponential backoff doubles the delay on each subsequent retry. Only takes effect when `dead-letter-enabled=true`. (long; `0` for no delay) | `1000` |

**Observability:**
- Counter `live_ingester_repo_events_dlq_total{exception="<short-class-name>"}` is exposed via the existing Micrometer registry. It is incremented exactly once per message routed to the DLQ.
- Operators should alert on a non-zero rate over a 5-minute window. A non-zero rate indicates either malformed events on the topic or a downstream system flapping persistently enough to exhaust the bounded redelivery policy.
- Each DLQ event is also logged at `ERROR` with the masked exchange state (no payload bodies) for triage.

**Operational notes:**
- The default DLQ (`ActiveMQ.DLQ`) is the broker-level catch-all queue. Most ActiveMQ-based deployments already have monitoring on it; if yours does not, add a queue-depth alert.
- Set a service-specific DLQ (e.g. `activemq:queue:LiveIngester.RepoEvents.DLQ`) if you need to differentiate repo-event failures from other DLQ traffic.
- Setting `maximum-redeliveries=0` skips retries entirely and dead-letters on the first exception. Useful for testing the alert path without waiting for backoff.

#### Repo events: unrecognised `eventType`

The connector dispatches on a fixed set of repo event types (`org.alfresco.event.node.Created` / `Updated` / `PermissionUpdated` / `Deleted`, plus the prediction-event variants). When a syntactically-valid CloudEvent arrives with an `eventType` outside that set:

- **Default behaviour:** an `INFO` log line names the unsupported type, the `live_ingester_repo_events_unhandled_total{type="<the.unknown.type>"}` Micrometer counter is incremented, and the JMS message is ACK'd. The route is preserved (no DLQ flood) so a future ACS release adding a new event type does not pile up on `ActiveMQ.DLQ` until the connector adds explicit handling.
- **Opt-in dead-letter:** set `ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERUNSUPPORTEDTYPES=true` to re-throw the event as `UnsupportedEventTypeException` so the existing repo-events `DeadLetterChannel` (above) routes it to `ActiveMQ.DLQ` with a `live_ingester_repo_events_dlq_total{exception="UnsupportedEventTypeException"}` increment. Pick this if your alerting story needs structured DLQ inventory rather than counter / log inventory.

```yaml
alfresco:
  repository:
    events-subscription:
      dead-letter-unsupported-types: false  # default; flip to true for hard-fail inventory
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERUNSUPPORTEDTYPES` | When `true`, an unrecognised `eventType` is re-thrown as `UnsupportedEventTypeException` and routed to the DLQ via the repo-events dead-letter channel. When `false`, only the `live_ingester_repo_events_unhandled_total` counter is incremented and the message is ACK'd. (boolean) | `false` |

**Observability (always on regardless of the opt-in):**
- Counter `live_ingester_repo_events_unhandled_total{type="<the.unknown.type>"}` is incremented exactly once per unrecognised event. The `type` tag carries the offending `eventType` so operators can break out e.g. classification events from retention events on the same alert.
- One `INFO` log line is emitted per unrecognised event, naming the type.

**Operational notes:**
- Cardinality of the `type` tag is bounded by ACS' actual event taxonomy; the counter does not sanitise the tag value, so a malformed publisher (or a deliberate fuzz on the topic) can in principle inflate the cardinality. ACS-side topics are not externally writable so this is a low-risk concern in supported deployments.
- The opt-in default is `false` because flipping it changes operator-visible queue depth on the broker (`ActiveMQ.DLQ`) and risks new ACS event types flooding the DLQ until the connector adds dispatch for them. The default-off path is the right shape for forward-compatibility; opt in only when your alerting story demands per-event DLQ inventory.

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

#### Replay handling and ordering

Repo events flow through ActiveMQ to the Live Ingester at-least-once. Every retry path inside the Live Ingester (the Camel JMS redelivery configured above, plus the HTTP retry on a 5xx response from `/ingestion-events` configured under [Retry Configuration](#retry-configuration)) leans on the assumption that HX Insight handles a re-POST of the same event safely. Two observed facts shape what operators can rely on, and one risk:

**Exact-replay is safe.** HX Insight silently accepts byte-identical re-POSTs of the same `/ingestion-events` body. There is no externally observable error, no response header signal, and no body-field difference between a first POST and any subsequent identical replay. This is what makes the at-least-once redelivery from the broker sound — a transient HX Insight 5xx, a JMS reconnect, or a Camel redelivery cannot produce visible duplicate state on the HX Insight side as long as the body the Live Ingester re-sends is unchanged.

**Stale-replay can shadow newer content.** Within a single `(sourceId, objectId)` HX Insight applies last-arrival-wins on the `content-metadata.digest` field, *regardless of the producer-supplied `sourceTimestamp`*. If a delayed redelivery of an older event reaches HX Insight after a newer event for the same node has already been ingested, the older event's content claim takes over and the newer one disappears from `GET /v1/check-digest`. The Live Ingester does not (and cannot) detect this from the producer side — every `POST` returns `202 Accepted` either way.

The risk window is bounded in single-instance deployments by the broker's per-queue FIFO ordering plus the bounded retry budget configured above (default ~63 s before parking to the DLQ on the JMS path, plus the HTTP retry budget on the HX Insight path). Operators should:

- Keep the Live Ingester to a single running instance. The [durable subscription opt-in](#durable-event-subscription-recommended) already enforces this; the same constraint protects ordering.
- Treat DLQ replays as deliberate, manual operations — do not blindly resend old DLQ contents into a queue that may have moved on to newer events for the same nodes.
- Alert on the DLQ counter (`live_ingester_repo_events_dlq_total`, see the [Observability section above](#repo-events-dead-letter-channel)). A non-zero rate is the externally visible signal that something is being retried persistently enough to risk falling out of order with later events for the same node.

**Note on `GET /v1/check-digest`.** The HX Insight `check-digest` endpoint is *not* a useful health probe for content emitted by this connector. HX Insight only indexes digests that were declared explicitly in the event payload's `content-metadata.digest` field, and the Live Ingester does not currently populate that field. Any `check-digest` query against connector-emitted content will return `exists: false` regardless of what is actually stored.

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

### Bulk-ingester events: dead-letter channel

Exceptions thrown while processing a bulk-ingester event (transient downstream failure, unexpected NPE, producer/consumer schema skew between same-repo services) are handled by an explicit Camel `DeadLetterChannel` on the route. Each failure is retried with exponential backoff up to a configurable maximum; if all retries are exhausted the **original** message is moved to a dead-letter queue and a Micrometer counter is incremented so operators can alert on it.

Mirror of the [repo-events DLC](#repo-events-dead-letter-channel) above — both JMS ingress routes share one operational pattern, one default retry budget, and the same DLQ destination by default. The realistic real-world trigger on this route is a transient downstream failure (HX Insight 5xx, network blip) persistent enough to exhaust the bounded redelivery policy. Producer/consumer schema skew is in principle possible but unlikely in practice, since the producer is the `bulk-ingester` service shipped from this same repository.

```yaml
alfresco:
  bulk-ingester:
    dead-letter-enabled: true                      # opt-in; default is false (broker-side redelivery)
    dead-letter-uri: activemq:queue:ActiveMQ.DLQ   # default
    maximum-redeliveries: 6                        # default; matches the repo-events route
    redelivery-delay-ms: 1000                      # default; exponential backoff: 1s, 2s, 4s, 8s, 16s, 32s
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_BULKINGESTER_DEADLETTERENABLED` | When `true`, install the route-level Camel `errorHandler(deadLetterChannel(...))` described above. When `false` (default), the route falls back to Camel's `DefaultErrorHandler` plus the broker's redelivery semantics; per-failure logs and the `live_ingester_bulk_events_dlq_total` counter are not produced. Opt in for the structured DLQ inventory + retry budget; leave off for master-equivalent behaviour. (boolean) | `false` |
| `ALFRESCO_BULKINGESTER_DEADLETTERURI` | Camel endpoint URI used as the dead-letter destination for bulk-ingester events. Only takes effect when `dead-letter-enabled=true`. (string) | `activemq:queue:ActiveMQ.DLQ` |
| `ALFRESCO_BULKINGESTER_MAXIMUMREDELIVERIES` | Maximum number of redelivery attempts before the message is moved to the dead-letter URI. Only takes effect when `dead-letter-enabled=true`. (integer; `0` disables retries) | `6` |
| `ALFRESCO_BULKINGESTER_REDELIVERYDELAYMS` | Initial delay between redelivery attempts in milliseconds. Exponential backoff doubles the delay on each subsequent retry. Only takes effect when `dead-letter-enabled=true`. (long; `0` for no delay) | `1000` |

**Observability:**
- Counter `live_ingester_bulk_events_dlq_total{exception="<short-class-name>"}` is exposed via the existing Micrometer registry. It is incremented exactly once per message routed to the DLQ.
- Operators should alert on a non-zero rate over a 5-minute window. A non-zero rate indicates a downstream system flapping persistently enough to exhaust the bounded redelivery policy, or — far less likely on this queue — a producer/consumer schema skew between the `bulk-ingester` service and the `live-ingester` consumer.
- Each DLQ event is also logged at `ERROR` with the masked exchange state (no payload bodies) for triage.

**Operational notes:**
- The default DLQ (`ActiveMQ.DLQ`) is the broker-level catch-all queue. Most ActiveMQ-based deployments already have monitoring on it; if yours does not, add a queue-depth alert.
- Set a service-specific DLQ (e.g. `activemq:queue:LiveIngester.BulkEvents.DLQ`) if you need to differentiate bulk-ingester failures from other DLQ traffic.
- Setting `maximum-redeliveries=0` skips retries entirely and dead-letters on the first exception. Useful for testing the alert path without waiting for backoff.
- Both JMS ingress routes (repo events and bulk-ingester) default to `maximumRedeliveries=6` so a single retry budget covers them. Tune them independently if one route's downstream behaves very differently from the other's.

---

## Transform Service Configuration

By default, the Live Ingester downloads content directly from the Alfresco repository and uploads it to HX Insight without transformation (passthrough). If you need to convert documents to a different format before ingestion, you can configure [MIME Type Mappings](#mime-type-mapping) to route specific content types through the Alfresco Transform Service (ATS).

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_TRANSFORM_REQUEST_ENDPOINT` | ActiveMQ queue to send transform requests to ATS (string) | `activemq:queue:acs-repo-transform-request` |
| `ALFRESCO_TRANSFORM_REQUEST_TIMEOUT` | Max time to wait for transform to complete in ms (integer) | `20000` |
| `ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL` | Shared File Store base URL (URL) | Required |
| `ALFRESCO_TRANSFORM_RESPONSE_QUEUENAME` | Queue name for receiving transform responses (string) | `org.alfresco.hxinsight-connector.transform.response` |
| `ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_ATTEMPTS` | Retry attempts for failed ingestion after transform (integer, `-1` = unlimited) | `-1` |

#### Transform-response: dead-letter channel (recommended)

By default, post-201 failures while processing a transform-response (e.g. transient Shared File Store unavailability when downloading the rendition, S3 PUT errors after rendition download but before upload-to-HXI, or any other exception that bubbles past the response handler's retry budget) are silently ACK'd by Camel's `DefaultErrorHandler`. The transform itself succeeded on the ATS side, but the rendition is silently abandoned: HX Insight ends up with only the metadata-only ingestion-event for the affected node, no DLQ entry exists, no metric is incremented, and no log line names the dropped node reference. This is acceptable for a development setup but constitutes silent data loss in production whenever SFS or downstream systems flap.

Opt in to a route-level `DeadLetterChannel` so failures land on a dead-letter queue with bounded redelivery and a Micrometer counter for observability. **Pair the DLC opt-in with a finite `retry-ingestion.attempts` value** — the production default is `-1` (unbounded) for legacy reasons, and the route's broad-`Exception` retry handler will swallow exceptions in an infinite loop and never reach the dead-letter destination unless this is set:

```yaml
alfresco:
  transform:
    response:
      dead-letter-enabled: true
      dead-letter-uri: activemq:queue:ActiveMQ.DLQ   # default
      maximum-redeliveries: 6                        # default
      redelivery-delay-ms: 1000                      # default; exponential backoff: 1s, 2s, 4s, 8s, 16s, 32s
      retry-ingestion:
        attempts: 6                                  # required; production default is -1 (unbounded) and would render the DLC inert
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_TRANSFORM_RESPONSE_DEADLETTERENABLED` | Set to `true` to install a Camel `errorHandler(deadLetterChannel(...))` on the `transform-events-consumer` route so post-201 failures during rendition processing land on the dead-letter queue instead of being silently ACK'd. (boolean) | `false` |
| `ALFRESCO_TRANSFORM_RESPONSE_DEADLETTERURI` | Camel endpoint URI used as the dead-letter destination for transform-response messages. Only takes effect when `dead-letter-enabled=true`. (string) | `activemq:queue:ActiveMQ.DLQ` |
| `ALFRESCO_TRANSFORM_RESPONSE_MAXIMUMREDELIVERIES` | Maximum number of redelivery attempts before the message is moved to the dead-letter URI. Only takes effect when `dead-letter-enabled=true`. (integer; `0` disables retries) | `6` |
| `ALFRESCO_TRANSFORM_RESPONSE_REDELIVERYDELAYMS` | Initial delay between redelivery attempts in milliseconds. Exponential backoff doubles the delay on each subsequent retry. Only takes effect when `dead-letter-enabled=true`. (long; `0` for no delay) | `1000` |
| `ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_ATTEMPTS` | Maximum redelivery attempts on the route's broad-`Exception` `onException` handler. Production default is `-1` (unbounded) and predates the DLC; **must be set to a finite value when `dead-letter-enabled=true`**, otherwise the broad handler retries forever and the DLC is never reached. The connector logs a `WARN` at startup if it detects this misconfiguration. (integer; `-1` = unbounded, recommended `6` to match the DLC redelivery budget) | `-1` |

**Observability:**
- Counter `live_ingester_transform_response_dlq_total{exception="<short-class-name>"}` is exposed via the existing Micrometer registry. It is incremented exactly once per message routed to the DLQ.
- Operators should alert on a non-zero rate over a 5-minute window. A non-zero rate indicates SFS unavailability persistent enough to exhaust the route's bounded redelivery policy, or any other downstream system flapping during the post-201 phase of the transform-response handler.
- Each DLQ entry preserves the original transform-response payload (`useOriginalMessage()`) so operators can trace the abandoned `clientData.nodeRef` and replay if appropriate. The exchange state is also logged at `ERROR` (with sensitive headers masked) for triage.
- A startup `WARN` is logged when `dead-letter-enabled=true` is detected together with `retry-ingestion.attempts < 0`, calling out that the DLC will be inert until the property is bounded.

**Operational notes:**
- Default off because turning it on adds a new queue depth (`ActiveMQ.DLQ`) for operators to monitor; the DLQ shape and tunables match the repo-events and bulk-ingester DLCs already documented in this file, so monitoring tooling that already alerts on those counters trivially covers this one.
- Setting `maximum-redeliveries=0` skips retries entirely and dead-letters on the first exception. Useful for testing the alert path without waiting for backoff.
- The unbounded default of `retry-ingestion.attempts` predates the DLC opt-in and is preserved for backward compatibility with deployments that rely on the legacy "retry forever" behaviour. A coordinated default-flip to a bounded value is on the same release roadmap as the other reliability defaults (durable subscription, DLC default-on); until then the pairing requirement above is the operator's responsibility.

**Rollback:** Set `ALFRESCO_TRANSFORM_RESPONSE_DEADLETTERENABLED=false` (or unset) and restart. The route reverts to the default — silent ACK on retry exhaustion. Drain the DLQ before rollback if non-empty.

#### Transform-response: surface ATS-reported failures (optional)

When ATS reports a transform as failed (`status=400` on the transform-response queue — typically a deterministic "I cannot produce this rendition" signal: unsupported mime mapping, transform-engine config, etc.), the connector logs a route-level `WARN` line containing the full payload (so the offending `clientData.nodeRef` and `errorDetails` are grep-friendly) and then ACKs the JMS message without further work. Retrying a deterministic ATS rejection is pointless and would just produce a flood of redeliveries with no upside, so this is the by-design behaviour and is appropriate for most deployments.

Deployments that want a structured, automation-friendly inventory of these abandonments (DLQ entry per failure, exception-tagged Micrometer counter, replayable original payload) can opt in to surfacing them as a thrown exception that flows through the route's error handler. Pair this opt-in with `dead-letter-enabled` above so the failed message lands on the DLQ rather than just exhausting the retry budget:

```yaml
alfresco:
  transform:
    response:
      throw-failed-transforms: true
      dead-letter-enabled: true                       # required to land the failure on the DLQ
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_TRANSFORM_RESPONSE_THROWFAILEDTRANSFORMS` | When `true`, an ATS-reported transform failure (`status=400` on the transform-response queue) is surfaced as a `FailedTransformResponseException` instead of the default silent ACK. The exception flows through the route's error handler — pair with `dead-letter-enabled=true` for the failure to land on the DLQ + counter. (boolean) | `false` |

**Observability:**
- Increments the same `live_ingester_transform_response_dlq_total{exception="FailedTransformResponseException"}` counter exposed by the dead-letter-channel opt-in. Operators alerting on a non-zero rate over a 5-minute window will catch a sudden spike in ATS-reported failures the same way they'd catch SFS-related drops.
- The DLQ entry preserves the original transform-response payload so operators can inspect `clientData.nodeRef`, `targetMimeType`, and ATS' `errorDetails` and replay against a corrected ATS configuration if appropriate.
- The default-deployment `WARN` log line (`Transform :: Transformation failed. Body: ${body}`) still fires whether the opt-in is on or off — the opt-in adds the structured signal alongside the existing log.

**Operational notes:**
- Default off because retrying a deterministic ATS rejection is wasted work, and most deployments are happy with the existing `WARN`-log signal. Turn the opt-in on if your alerting story needs metric/DLQ inventory rather than log-line inventory.
- `throw-failed-transforms=true` without `dead-letter-enabled=true` causes the exception to exhaust the route's retry budget and then ACK the message anyway (with extra `ERROR` logs along the way) — strictly noisier than the default unless paired with the DLC.
- Intended as a permanent operator-visibility opt-in, not as a transitional workaround for a bug. The default-off behaviour is a deliberate design choice in the connector and is expected to remain the default for new deployments; turn the opt-in on if and only if your alerting story needs structured failure inventory.

**Rollback:** Set `ALFRESCO_TRANSFORM_RESPONSE_THROWFAILEDTRANSFORMS=false` (or unset) and restart. The route reverts to the default `WARN`-log + silent-ACK behaviour. Drain any pending DLQ entries before rollback if non-empty.

---

## MIME Type Mapping

The `mime-type.mapping` configuration controls which source MIME types are accepted and what target format they are transformed to before being sent to HX Insight. When the source and target MIME types are the same, content is uploaded directly without transformation (passthrough).

### Default Mappings

By default, **all content is passed through without transformation** — content is downloaded directly from the Alfresco repository and uploaded to HX Insight as-is. This relies on [CIC Document Filters](#passthrough-with-cic-document-filters) to handle any server-side conversion. To route specific content types through ATS instead, provide a custom mapping (see below).

### Custom Mappings

Override the default passthrough behaviour by providing your own mapping. When custom mappings are configured, they **replace** the defaults entirely — any MIME types not covered by an explicit entry will have their content upload skipped. To ensure uncovered types still pass through, include a universal wildcard entry (`"*": "*"`) as a catch-all. The mapping value determines how content is handled:

| Mapping | Behaviour |
|---------|-----------|
| Source → different target (e.g. `text/csv: application/pdf`) | Content is transformed via ATS and the result is uploaded to HX Insight |
| Source → itself (e.g. `text/csv: text/csv`) | Content is downloaded directly from Alfresco and uploaded to HX Insight without transformation (passthrough). Useful when relying on [CIC Document Filters](#passthrough-with-cic-document-filters) for server-side conversion. |
| Wildcard → itself (e.g. `image/*: image/*` or `"*": "*"`) | Passthrough for all matching types. Each matched source type is uploaded as-is without transformation. |
| Source → empty string (e.g. `text/csv: ""`) | Content upload is skipped entirely. Node metadata is still ingested. |

> **Note:** Wildcards in the target value are only valid when they match the source pattern exactly (e.g. `image/*: image/*` or `"*": "*"`). Any other use of wildcards on the right-hand side (e.g. `image/*: text/*`) is a configuration error and will prevent the application from starting.

```yaml
alfresco:
  transform:
    mime-type:
      mapping:
        "[text/csv]": application/pdf  # Transform via ATS
        "[image/png]": image/png       # Passthrough for a specific type
        "[video/*]": video/*           # Passthrough for all video types
        "[audio/*]": ""                # Skip content upload for all audio
        "[*]": application/pdf       # Universal catch-all (lowest priority)
```

**Lookup order:** Exact match → Subtype wildcard (`type/*`) → Universal wildcard (`*`) → No match (skipped)

### Passthrough with CIC Document Filters

Hyland's [Content Innovation Cloud](https://www.hyland.com/en/platform) includes [Document Filters](https://www.hyland.com/en/solutions/products/document-filters), which can transform many file types. The default passthrough behaviour sends all content directly to HX Insight without ATS transformation, relying on Document Filters for any server-side conversion. If you need to transform specific content types via ATS before ingestion, override the defaults with a custom mapping (e.g. `text/csv: application/pdf`). When source and target MIME types match, the connector downloads the content directly from the Alfresco repository and uploads it to HX Insight without sending it through ATS.

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

### HX Insight HTTP response timeout

The two HX Insight HTTP paths (`/presigned-urls`, `/ingestion-events`) accept a per-path HTTP response timeout that bounds how long the Live Ingester waits for HX Insight to start sending response bytes before aborting the request. Without this timeout configured, the underlying Apache HttpClient5 used by Camel defaults to an **infinite** wait, so a slow HX Insight (e.g. a stuck upstream) would block the route's worker thread until the JVM is restarted.

| Environment variable | Description | Default |
|---------------------|-------------|---------|
| `HYLANDEXPERIENCE_INGESTER_RESPONSETIMEOUTMS` | Maximum time (ms) to wait for an HX Insight `/ingestion-events` response before aborting with `SocketTimeoutException`. The aborted attempt counts against the JMS-level redelivery budget; exhaustion sends the message to `ActiveMQ.DLQ` (see [Repo events dead-letter channel](#repo-events-dead-letter-channel)). | `30000` (30 s) |
| `HYLANDEXPERIENCE_STORAGE_LOCATION_RESPONSETIMEOUTMS` | Same, for the `/presigned-urls` path. | `30000` (30 s) |
| `HYLANDEXPERIENCE_STORAGE_UPLOAD_RESPONSETIMEOUTMS` | Per-request response timeout (ms) for the actual content `PUT` against the pre-signed S3 URL returned by HX Insight. `0` (default) leaves no per-request response timeout on the underlying Apache HttpClient5 (`RequestConfig.responseTimeout` unset, falling through to the connection's default `SocketConfig.soTimeout` of `0` / disabled), so the socket read blocks indefinitely on a slow or unresponsive S3 endpoint and the route worker thread stays pinned until the JVM is restarted. Set a positive value (e.g., `30000`) in production to bound per-request wait and surface a slow upload as a `SocketTimeoutException` that can be retried / dead-lettered. (integer ms, `>= 0`) | `0` |

The 30 s default on the two HX Insight paths is chosen to be conservative: long enough to cover real HX Insight cold-start spikes (cold S3 client, distant region) without being so long that a single hung response can stall the route for minutes. Tune lower if your HX Insight tenant has tight SLOs and you'd rather fail fast (the failure surfaces as a counter increment on `live_ingester_repo_events_dlq_total{exception="SocketTimeoutException"}` and a parked message on the DLQ).

The S3 upload path (`HYLANDEXPERIENCE_STORAGE_UPLOAD_RESPONSETIMEOUTMS`) defaults to `0` rather than a positive value because the right value depends on operator-side tuning (large content uploads against a distant S3 region legitimately take many seconds), so no single safe number works across deployments.

> Note on `4xx` responses: any HX Insight `4xx` response (other than `404`, which surfaces as `ResourceNotFoundException`) is treated as a fatal client error and is **not retried**. This applies to `400`, `409`, `412`, `429`, etc. uniformly — the connector does not implement bespoke handling for individual `4xx` semantics. If you observe `live_ingester_repo_events_dlq_total{exception="EndpointClientErrorException"}` increasing, inspect the corresponding HX Insight response codes and tighten producer pacing (e.g. cap the bulk-ingester batch rate) if the upstream is signalling overload.

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
