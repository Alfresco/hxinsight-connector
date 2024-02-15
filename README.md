# Alfresco Connector for Hyland Experience Insight

Connector for sending ACS events to Hx Insight and updating the Repository with the predictions that it generates.

### Live Ingester configuration

#### Retry
In case of an error while trying to call external endpoint the call will be reattempted.
Retry specification default values are:
- attempts: 10
- initial delay: 500 ms
- delay multiplier: 2

Above default properties can be overwritten with custom specification, which for particular endpoints may look like, e.g.:
- authentication request:
```yaml
hyland-experience:
  authentication:
    retry:
      attempts: 5
      initialDelay: 1000
      delayMultiplier: 1.5
```

- file download from shared file store:
```yaml
alfresco:
  transform:
    shared-file-store:
      retry:
        attempts: 5
        initialDelay: 1000
        delayMultiplier: 1.5
```

- storage location request:
```yaml
hyland-experience:
  storage:
    location:
      retry:
        attempts: 5
        initialDelay: 1000
        delayMultiplier: 1.5
```

- file upload to obtained storage location:
```yaml
hyland-experience:
  storage:
    upload:
      retry:
        attempts: 5
        initialDelay: 1000
        delayMultiplier: 1.5
```

- ingest request:
```yaml
hyland-experience:
  ingester:
    retry:
      attempts: 5
      initialDelay: 1000
      delayMultiplier: 1.5
```


### Bulk Ingester configuration

#### Namespace prefixes
As namespace prefixes are not available in db you have to specify mapping between `namespace`->`prefix` in configuration
file. By default, prefixes mappings are specified in [namespace-prefixes.json](bulk-ingester/src/main/resources/namespace-prefixes.json) file -
you can change it via the `alfresco.bulk.ingest.namespace-prefixes-mapping` property

### Code Quality
This project uses `spotless` that enforces `alfresco-formatter.xml` to ensure code quality.

To check code-style violations you can use:
```bash
mvn spotless:check
```
To reformat files you can use:
```bash
mvn spotless:apply
```

### Development Environment

To set up a local developer environment then build the jar, the docker image and finally run the docker-compose environment.

```bash
mvn clean install -Pdistribution && \
./scripts/ci/buildDockerImages.sh && \
cd distribution/src/main/resources/docker-compose && \
docker compose --project-name dev up
```

It's also possible to set up local developer environment adjusted to run Live Ingester outside docker container, to do se please run bellow command:

```bash
mvn clean install -Pdistribution && \
./scripts/ci/buildDockerImages.sh && \
cd distribution/src/main/resources/docker-compose && \
docker compose --file docker-compose-ingesterless.yml --project-name dev up
```
