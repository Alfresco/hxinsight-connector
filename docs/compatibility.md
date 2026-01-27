# Compatibility

[← Documentation Index](index.md) | [Installation →](installation.md)

## Alfresco Content Services Versions

**Supported:** ACS 7.3 through 25.x (Enterprise Edition only)

> ⚠️ **Alfresco Community Edition is not supported.** The connector currently requires enterprise events and so will only run against Alfresco Enterprise Edition.

---

## Java Requirements

| Component | Java Version |
|-----------|-------------|
| Live Ingester, Bulk Ingester, Prediction Applier, Nucleus User Sync | Java 17+ |
| [Knowledge Discovery JAR Module](hxinsight-extension.md) | Java 11 or 17 (matches your ACS version) |

The Knowledge Discovery JAR Module Docker image can switch between Java 11 and 17 at runtime to match older Alfresco versions (7.3.x, 7.4.x).

---

## Infrastructure Requirements

| Component | Requirement |
|-----------|-------------|
| ActiveMQ | 5.x (tested with 5.18.3) |
| PostgreSQL | 14.x (for Bulk Ingester direct DB access) |
| Alfresco Transform Service | 4.x / 5.x |
| Shared File Store | 4.x |

---

## HX Insight API Compatibility

The connector is designed to work with the current HX Insight API. Contact Hyland for specific API version requirements and to obtain your OAuth credentials.
