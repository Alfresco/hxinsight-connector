# Alfresco Connector for Hyland Experience Insight

Connector for sending ACS events to Hx Insight and updating the Repository with the predictions that it generates.


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
mvn clean install && \
./scripts/ci/buildDockerImages.sh && \
cd distribution/src/main/resources/docker-compose && \
docker compose --project-name dev up
```
