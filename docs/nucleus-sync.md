# Nucleus User Sync Configuration

[← Components](components.md) | [Installation →](installation.md)

> ⚠️ **Status: Coming Soon**
>
> This component is under active development and not yet production-ready. It will be available in a future release.

The Nucleus User Sync application periodically synchronizes Alfresco users and groups with the Nucleus identity system.
This enables HX Insight to understand user permissions and provide personalized search results.

> **Default configuration:** See [`application.yml`](../nucleus-sync/src/main/resources/application.yml) for all defaults.

## Required Configuration

### Alfresco Connection

```yaml
alfresco:
  base-url: http://alfresco:8080/alfresco
  page-size: 1000
  user:
    skip-not-enabled: true
  user-group:
    fetch-timeout: PT45M
    max-concurrent-requests: 50
```

| Environment Variable                        | Description                                                               | Default              |
| ------------------------------------------- | ------------------------------------------------------------------------- | -------------------- |
| `ALFRESCO_BASEURL`                          | Alfresco REST API base URL                                                | -                    |
| `ALFRESCO_PAGESIZE`                         | Number of users/groups to fetch per API request                           | `1000`               |
| `ALFRESCO_USER_SKIPNOTENABLED`              | Skip users who are not enabled                                            | `true`               |
| `ALFRESCO_USERGROUP_FETCHTIMEOUT`           | Timeout for fetching group data (ISO-8601 duration)                       | `PT15M` (15 minutes) |
| `ALFRESCO_USERGROUP_MAXCONCURRENT_REQUESTS` | Maximum concurrent requests to be made to obtain user's group memberships | `50`                 |

### Nucleus Connection

```yaml
nucleus:
  base-url: https://nucleus.hyland.com
  idp-base-url: https://auth.hyland.com
  system-id: <your-system-id>
  page-size: 1000
  sync-batch-size: 1000
  delete-group-member-batch-size: 100
```

| Environment Variable                 | Description                                  | Default |
| ------------------------------------ | -------------------------------------------- | ------- |
| `NUCLEUS_BASEURL`                    | Nucleus API base URL (provided by Hyland)    | -       |
| `NUCLEUS_IDPBASEURL`                 | Identity provider base URL for user lookups  | -       |
| `NUCLEUS_SYSTEMID`                   | Unique identifier for your system in Nucleus | -       |
| `NUCLEUS_PAGESIZE`                   | Number of mappings to fetch per API request  | `1000`  |
| `NUCLEUS_SYNCBATCHSIZE`              | Number of mappings to create per API request | `1000`  |
| `NUCLEUS_DELETEGROUPMEMBERBATCHSIZE` | Batch size when removing group members       | `100`   |

### HTTP Client

```yaml
http-client:
  timeout-minutes: 5
  buffer-size-kilobytes: 10240
```

| Environment Variable             | Description                                 | Default        |
| -------------------------------- | ------------------------------------------- | -------------- |
| `HTTPCLIENT_TIMEOUTMINUTES`      | HTTP client timeout for API calls (minutes) | `5`            |
| `HTTPCLIENT_BUFFERSIZEKILOBYTES` | HTTP client buffer size in KB               | `10240` (10MB) |

### Authentication

```yaml
auth:
  providers:
    hyland-experience:
      type: oauth2
      client-id: <your-client-id>
      client-secret: <your-client-secret>
      token-uri: https://auth.hyland.com/oauth/token
    alfresco:
      type: basic
      username: admin
      password: admin
```

| Environment Variable                           | Description                                   |
| ---------------------------------------------- | --------------------------------------------- |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID`     | HXI OAuth client ID                           |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET` | HXI OAuth client secret                       |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI`     | HXI OAuth token endpoint                      |
| `AUTH_PROVIDERS_ALFRESCO_USERNAME`             | Alfresco admin username (for REST API access) |
| `AUTH_PROVIDERS_ALFRESCO_PASSWORD`             | Alfresco admin password                       |

### Synchronization

```yaml
sync:
  enabled: true
  cron.expression: "0 0 0 * * *"
```

| Environment Variable  | Description                                                                               | Default       |
|-----------------------|-------------------------------------------------------------------------------------------|---------------|
| `SYNC_ENABLED`        | Whether scheduled sync should be enabled. (If not sync is to be performed using REST API) | `true`        |
| `SYNC_CRONEXPRESSION` | Cron expression for scheduled sync                                                        | `"0 0 0 * * *"` |
---

## Synchronization using REST API

- The following URL needs to be called to perform an on-demand sync

```sh
curl -X POST "http://<base-url>:<port>/sync/trigger"
```

- We can also view the last sync status using

```sh
curl -X GET "http://<base-url>:<port>/sync/status"
```

---

## Synchronization Behavior

The application performs these steps periodically:

1. **Fetch Users** - Retrieves all users from Alfresco via REST API
2. **Map Users** - Matches Alfresco users to Nucleus users by email address
3. **Fetch Groups** - Retrieves Alfresco groups that contain mapped users
4. **Sync Groups** - Creates/updates groups in Nucleus
5. **Sync Memberships** - Updates group memberships in Nucleus

### User Mapping

Users are mapped between Alfresco and Nucleus based on their **email address**. If a user's email in Alfresco matches an email in the Nucleus identity provider, they will be linked.

> [!NOTE]
> Alfresco users with no email will be ignored.
> If multiple Alfresco users have same email - all of them will be ignored.

### Group Filtering

Only groups that contain at least one mapped user will be synchronized to Nucleus.
