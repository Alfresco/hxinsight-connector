# Alfresco Connector for Content Intelligence

> **📘 Note:**
> - Official documentation is available at [Hyland Support](https://support.hyland.com/r/Alfresco/Alfresco-Connector-for-Content-Intelligence)
> - This documentation applies to the version of the connector at this commit. For documentation matching other versions, see the [tags in GitHub](https://github.com/Alfresco/hxi-connector/tags).

The Alfresco Connector for Content Intelligence (also known as the HX Insight Connector) provides knowledge retrieval capabilities by connecting your content repository, Alfresco Content Services (ACS), to Knowledge Discovery. Knowledge Discovery allows you to apply machine learning to your content repository.

Using Alfresco Digital Workspace as the user interface, you can select one or more documents, and ask an AI Agent questions to gain knowledge about the content stored in your repository.

## Components

| Component | Status | Description |
|-----------|--------|-------------|
| [Live Ingester](live-ingester.md) | ✅ Complete | Keeps data in Knowledge Discovery up to date with day-to-day changes |
| [Bulk Ingester](bulk-ingester.md) | ✅ Complete | Initial ingestion of documents, or recovering after downtime |
| [Knowledge Discovery JAR Module](hxinsight-extension.md) | ✅ Complete | Enables Digital Workspace to interact with Knowledge Discovery |
| [Nucleus User Sync](nucleus-sync.md) | ⚠️ Incomplete | User/group synchronization with Nucleus |
| [Prediction Applier](prediction-applier.md) | 🧪 Experimental | Applies HXI predictions back to Alfresco |

## Use Cases

### Knowledge Discovery & Enrichment

Use these components to index Alfresco content in HX Insight for AI-powered search and metadata enrichment:

| Component | When to Use |
|-----------|-------------|
| **Live Ingester** | Required for real-time document processing. Listens to Alfresco events and sends new/updated content to HXI. |
| **Bulk Ingester** | Essential for initial system bootstrapping. Ingests existing content directly from the database. Also useful for re-indexing or catching up after outages. |

### Discovery from ADW UI

To enable the HX Insight Discovery search panel in Alfresco Digital Workspace:

- **HxInsight Extension (JAMP)** - Must be installed in the Alfresco Repository. Provides the server-side APIs that ADW uses to query HX Insight.

## Quick Links

- [Compatibility](compatibility.md) - Supported Alfresco versions and requirements
- [Installation Guide](installation.md) - JAR, Docker, and Kubernetes deployment options
- [Component Details](components.md) - Detailed description of each component
- [ACS Private APIs](acs-private-apis.md) - REST API reference for the Knowledge Discovery JAR Module

## Prerequisites

- **Alfresco Content Services Enterprise Edition** 7.3.x or later (Community Edition not supported)
- ActiveMQ 5.x
- HX Insight subscription (credentials provided by Hyland)
- Java 17 or later (for JAR deployments)
