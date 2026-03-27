# Compatibility

[← Documentation Index](index.md) | [Installation →](installation.md)

## Alfresco Content Services Versions

**Supported:** ACS 7.4 through 26.x (Enterprise Edition only)

> ⚠️ **Alfresco Community Edition is not supported.** The connector currently requires enterprise events and so will only run against Alfresco Enterprise Edition.

---

## Java Requirements

| Component | Java Version |
|-----------|-------------|
| Live Ingester, Bulk Ingester, Prediction Applier, Nucleus User Sync | Java 21+ |
| [Knowledge Discovery JAR Module](hxinsight-extension.md) | Java 11, 17, or 21 (matches your ACS version) |

The Knowledge Discovery JAR Module Docker image can switch between Java 11, 17, and 21 at runtime to match your Alfresco version (7.4.x uses Java 11/17, 26.x uses Java 21).

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
