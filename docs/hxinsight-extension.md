# Knowledge Discovery JAR Module Configuration

[← Components](components.md) | [Installation →](installation.md#knowledge-discovery-jar-module-installation) | [API Reference →](acs-private-apis.md)

The Knowledge Discovery JAR Module is an Alfresco JAR module (or JAMP) that enables Discovery integration in Alfresco Digital Workspace (ADW). It provides the ability for Digital Workspace to interact with Knowledge Discovery, including knowledge retrieval and question answering functionality.

## Installation

**This component can only be installed as a JAR module in the Alfresco Repository.** It cannot be deployed as a standalone service.

### Pre-built Docker Image (Recommended)

A Docker image is available that bundles the JAMP with the Alfresco Enterprise repository:

```
quay.io/alfresco/alfresco-content-repository-hxinsight-extension:<version>
```

This image is built from the standard Alfresco Enterprise image with the JAMP added to `WEB-INF/lib/`.

### Manual JAR Installation

Copy the JAR to the modules directory of your Alfresco installation:

```bash
cp alfresco-hxinsight-connector-hxinsight-extension-*.jar \
   $ALFRESCO_HOME/modules/platform/
```

Or mount it as a volume in Docker:

```yaml
volumes:
  - ./alfresco-hxinsight-connector-hxinsight-extension.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/hxinsight-extension.jar
```

---

## Configuration

Configure via `alfresco-global.properties` or Java system properties (`-D` flags).

### Authentication

OAuth2 credentials for authenticating with HX Insight APIs.

```properties
hxi.auth.providers.hyland-experience.type=oauth2
hxi.auth.providers.hyland-experience.grant-type=client_credentials
hxi.auth.providers.hyland-experience.client-id=<your-client-id>
hxi.auth.providers.hyland-experience.client-secret=<your-client-secret>
hxi.auth.providers.hyland-experience.token-uri=https://auth.hyland.com/oauth/token
hxi.auth.providers.hyland-experience.environment-key=<your-env-key>
hxi.auth.providers.hyland-experience.scope=hxp.integrations hxp iam.jti-capture
```

| Property | Description |
|----------|-------------|
| `hxi.auth.providers.hyland-experience.client-id` | HXI OAuth client ID (provided by Hyland) |
| `hxi.auth.providers.hyland-experience.client-secret` | HXI OAuth client secret (provided by Hyland) |
| `hxi.auth.providers.hyland-experience.token-uri` | HXI OAuth token endpoint |
| `hxi.auth.providers.hyland-experience.environment-key` | HXI environment key (identifies your HXI tenant) |
| `hxi.auth.providers.hyland-experience.scope` | OAuth scopes required for API access |

### Auth Retry

Configure retry behavior for authentication requests.

```properties
hxi.auth.retry.attempts=3
hxi.auth.retry.initial-delay=500
hxi.auth.retry.delay-multiplier=2
```

| Property | Description | Default |
|----------|-------------|---------|
| `hxi.auth.retry.attempts` | Maximum retry attempts | `3` |
| `hxi.auth.retry.initial-delay` | Initial delay between retries (ms) | `500` |
| `hxi.auth.retry.delay-multiplier` | Multiplier for exponential backoff | `2` |

---

### Discovery API

Endpoints for the HX Insight Discovery service.

```properties
hxi.discovery.base-url=https://hxinsight.hyland.com
hxi.discovery.agents-endpoint=${hxi.discovery.base-url}/agent/integrations
hxi.discovery.questions-endpoint=${hxi.discovery.base-url}/qna/integrations
```

| Property | Description |
|----------|-------------|
| `hxi.discovery.base-url` | HX Insight base URL (provided by Hyland) |
| `hxi.discovery.agents-endpoint` | API endpoint for Discovery agents |
| `hxi.discovery.questions-endpoint` | API endpoint for question/answer functionality |

---

### Knowledge Retrieval

Endpoint for the knowledge retrieval (AI search) service.

```properties
hxi.knowledge-retrieval.url=https://hxinsight.hyland.com/knowledge-retrieval/bots
```

| Property | Description |
|----------|-------------|
| `hxi.knowledge-retrieval.url` | Knowledge retrieval API endpoint |

---

### Question Settings

```properties
hxi.question.max-context-size-for-question=100
```

| Property | Description | Default |
|----------|-------------|---------|
| `hxi.question.max-context-size-for-question` | Maximum number of context items to include when answering questions | `100` |

---

### Connector Identity

These properties are sent to Knowledge Discovery to enable tracking the original source of documents.

```properties
hxi.connector.version=2.0.0
hxi.connector.source-id=a1f3e7c0-d193-7023-ce1d-0a63de491876
```

| Property | Description |
|----------|-------------|
| `hxi.connector.version` | Connector version. This is automatically set during the build process (e.g., `2.0.0`). You typically don't need to set this manually. |
| `hxi.connector.source-id` | Unique identifier for this Alfresco source in HXI |

---

### Service Account

Optional service account configuration for internal operations.

```properties
serviceaccount.role.service-account-hxi-connector=
```

| Property | Description |
|----------|-------------|
| `serviceaccount.role.service-account-hxi-connector` | Role to assign to the HXI service account |

---

## ADW Configuration

To enable the Knowledge Retrieval UI in Alfresco Digital Workspace, set:

```yaml
# In digital-workspace environment configuration
APP_CONFIG_PLUGIN_KNOWLEDGE_RETRIEVAL: true
```

This enables the Discovery search panel in ADW, which uses the APIs provided by the Knowledge Discovery JAR Module.
