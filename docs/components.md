# Component Overview

## Live Ingester

**Status:** ✅ Complete | [Configuration →](live-ingester.md)

A Spring Boot application that listens to Alfresco repository events via ActiveMQ and sends document metadata and content to Knowledge Discovery for indexing. It is responsible for keeping the data in Knowledge Discovery up to date with the day-to-day changes made in Content Services.

**Key Features:**
- Real-time event processing as documents are created, updated, or deleted
- Content transformation via Alfresco Transform Service (ATS)
- Filtering by node type, aspect, and path
- ACL synchronization (read/deny permissions)

**Required For:**
- Knowledge Discovery - AI-powered document search
- Knowledge Enrichment - automated metadata extraction

**Dependencies:**
- ActiveMQ (for receiving Alfresco events)
- Alfresco Transform Service (for content transformation)
- Shared File Store (for transformed content)

**Deployment:** [Docker, Kubernetes, or standalone JAR](installation.md)

---

## Bulk Ingester

**Status:** ✅ Complete | [Configuration →](bulk-ingester.md)

A batch processing application that reads directly from the Alfresco database to ingest large volumes of existing content into Knowledge Discovery. It is responsible for the initial ingestion of documents, or recovering after a period of downtime.

**Key Features:**
- Direct database access for high-performance batch processing
- Configurable node ID ranges for incremental or parallel ingestion
- Filtering by node type, aspect, and path
- Namespace prefix mapping for custom content models

**Required For:**
- Initial system bootstrapping (ingesting existing content)
- Large-scale migrations
- Recovery after downtime or configuration changes

**Dependencies:**
- PostgreSQL (direct database access)
- ActiveMQ (for publishing events)

**Deployment:** [Docker, Kubernetes, or standalone JAR](installation.md)

---

## Knowledge Discovery JAR Module

**Status:** ✅ Complete | [Configuration →](hxinsight-extension.md) | [API Reference →](acs-private-apis.md)

An Alfresco JAR module that extends the Alfresco repository with Knowledge Discovery integration capabilities. It provides the ability for Digital Workspace to interact with Knowledge Discovery.

**Key Features:**
- REST API endpoints for Discovery queries (Agents, Questions, Answers, Predictions)
- Question answering integration
- Knowledge retrieval for ADW

**Required For:**
- Discovery features in Alfresco Digital Workspace (ADW)
- AI-powered search directly from the Alfresco UI

**Dependencies:**
- Alfresco Content Services (installed as a module)

**Deployment:** [JAR module installed in Alfresco Repository](installation.md#knowledge-discovery-jar-module-installation) **only** (cannot run standalone)

---

## Nucleus User Sync

**Status:** ⚠️ Incomplete (Work in Progress) | [Configuration →](nucleus-sync.md)

A Spring Boot application that periodically synchronizes Alfresco users and groups with the Nucleus identity system.

**Key Features:**
- Periodic user/group synchronization via REST API
- Email-based user mapping between Alfresco and Nucleus
- Group membership sync

**Deployment:** [Docker, Kubernetes, or standalone JAR](installation.md)

---

## Prediction Applier

**Status:** 🧪 Experimental | [Configuration →](prediction-applier.md)

A Spring Boot application that polls HX Insight for AI-generated predictions and applies them back to Alfresco documents as metadata.

**Key Features:**
- Configurable polling interval
- Prediction buffering via ActiveMQ
- Automated metadata updates on Alfresco nodes

**Dependencies:**
- ActiveMQ (for prediction buffering)
- Alfresco Repository (for applying predictions)

**Deployment:** [Docker, Kubernetes, or standalone JAR](installation.md)
