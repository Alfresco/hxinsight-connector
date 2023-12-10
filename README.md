# Alfresco HxInsight Connector

Connector for sending ACS events to HxInsight and updating the Repository with the predictions that it generates.


### Code Quality
This project uses `spotless` to ensure code quality.

To check code-style violations you can use:
```bash
mvn spotless:check
```
To reformat files you can use:
```bash
mvn spotless:apply
```
or load `code-formatter.xml` to IntelliJ

### Development Environment

To set up a local developer environment then build the jar, the docker image and finally run the docker-compose environment.

```bash
mvn clean install && \
./scripts/ci/buildDockerImages.sh && \
cd distribution/src/main/resources/docker-compose && \
docker compose --project-name dev up
```
