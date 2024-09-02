# Alfresco Connector for Hyland Experience Insight

Connector for sending ACS events to Hx Insight and updating the Repository with the predictions that it generates.

## Additional Documentation

* [ACS Private API Documentation](docs/acs-private-apis.md)

### Development Environment

To set up a local developer environment then build the jar, the docker image and finally run the docker-compose environment:

```bash
mvn clean install -DskipTests -Pdistribution && \
./scripts/ci/buildDockerImages.sh && \
cd distribution/src/main/resources/docker-compose && \
docker compose --project-name dev up
```

It's also possible to set up a local developer environment adjusted to run Live Ingester outside docker container, to do so please run the following command:

```bash
mvn clean install -DskipTests -Pdistribution && \
./scripts/ci/buildDockerImages.sh && \
cd distribution/src/main/resources/docker-compose && \
docker compose --file docker-compose-ingesterless.yml --project-name dev up
```

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

### Secret Detection

We are using [detect-secrets](https://github.com/Yelp/detect-secrets) to try to avoid accidentally publishing secret keys.
If you have pre-commit installed then this should run automatically when making a commit. Usually there should be no issues,
but if it finds a potential issue (e.g. a high entropy string) then you will see the following:

```shell
Detect secrets...........................................................Failed
- hook id: detect-secrets
- exit code: 1

ERROR: Potential secrets about to be committed to git repo!

Secret Type: Secret Keyword
Location:    test.txt:1
```

If this is a false positive and you actually want to commit the string then run these two commands:

```shell
detect-secrets scan --baseline .secrets.baseline
detect-secrets audit .secrets.baseline
```

This will update the baseline file to include your new code and then allow you to review the detected secret and mark it as a false positive.
Once you are finished then you can add `.secrets.baseline` to the staged changes and you should be able to create a commit.


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
      initial-delay: 1000
      delay-multiplier: 1.5
```

- file download from shared file store:
```yaml
alfresco:
  transform:
    shared-file-store:
      retry:
        attempts: 5
        initial-delay: 1000
        delay-multiplier: 1.5
```

- storage location request:
```yaml
hyland-experience:
  storage:
    location:
      retry:
        attempts: 5
        initial-delay: 1000
        delay-multiplier: 1.5
```

- file upload to obtained storage location:
```yaml
hyland-experience:
  storage:
    upload:
      retry:
        attempts: 5
        initial-delay: 1000
        delay-multiplier: 1.5
```

- ingest request:
```yaml
hyland-experience:
  ingester:
    retry:
      attempts: 5
      initial-delay: 1000
      delay-multiplier: 1.5
```


### Bulk Ingester configuration

#### Namespace prefixes
As namespace prefixes are not available in db you have to specify mapping between `namespace`->`prefix` in configuration
file. By default, prefixes mappings are specified in [namespace-prefixes.json](bulk-ingester/src/main/resources/namespace-prefixes.json) file -
you can change it via the `alfresco.bulk.ingest.namespace-prefixes-mapping` property

With use of [namespaces-to-namespace-prefixes-file-generator.py](scripts/utils/namespaces-to-namespace-prefixes-file-generator.py)
you can automatically generate `namespace-prefixes.json` with all types in your repository

```bash
python3 scripts/utils/namespaces-to-namespace-prefixes-file-generator.py --help
```
