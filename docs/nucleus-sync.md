# Nucleus User Sync Configuration

[← Components](components.md) | [Installation →](installation.md)

> ⚠️ **Status: Incomplete (Work in Progress)**

The Nucleus User Sync application periodically synchronizes Alfresco users and groups with the Nucleus identity system. This enables HX Insight to understand user permissions and provide personalized search results.

## Required Configuration

### Alfresco Connection

```yaml
alfresco:
  base-url: http://alfresco:8080/alfresco
  page-size: 100
  sync-batch-size: 1000
  user-group:
    fetch-timeout: PT5M
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `ALFRESCO_BASEURL` | Alfresco REST API base URL | - |
| `ALFRESCO_PAGESIZE` | Number of users/groups to fetch per API request | `100` |
| `ALFRESCO_SYNCBATCHSIZE` | Number of records to sync in each batch | `1000` |
| `ALFRESCO_USERGROUP_FETCHTIMEOUT` | Timeout for fetching user/group data (ISO-8601 duration) | `PT5M` (5 minutes) |

### Nucleus Connection

```yaml
nucleus:
  base-url: https://nucleus.hyland.com
  idp-base-url: https://auth.hyland.com
  system-id: <your-system-id>
  delete-group-member-batch-size: 50
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `NUCLEUS_BASEURL` | Nucleus API base URL (provided by Hyland) | - |
| `NUCLEUS_IDPBASEURL` | Identity provider base URL for user lookups | - |
| `NUCLEUS_SYSTEMID` | Unique identifier for your system in Nucleus | - |
| `NUCLEUS_DELETEGROUPMEMBERBATCHSIZE` | Batch size when removing group members | `50` |

### HTTP Client

```yaml
http-client:
  timeout-minutes: 5
```

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `HTTPCLIENT_TIMEOUTMINUTES` | HTTP client timeout for API calls (minutes) | `5` |

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

| Environment Variable | Description |
|---------------------|-------------|
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID` | HXI OAuth client ID |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTSECRET` | HXI OAuth client secret |
| `AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI` | HXI OAuth token endpoint |
| `AUTH_PROVIDERS_ALFRESCO_USERNAME` | Alfresco admin username (for REST API access) |
| `AUTH_PROVIDERS_ALFRESCO_PASSWORD` | Alfresco admin password |

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

### Group Filtering

Only groups that contain at least one mapped user will be synchronized to Nucleus.
