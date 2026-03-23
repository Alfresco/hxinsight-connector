# Nucleus Sync APIs

## Purpose

The purpose of these APIs are to perform an on-demand sync, know the last sync
status and perform health-check.

## APIs

### Endpoint

The base url is either `localhost` or the machine-name. Port by default is 8081.

Base path for all endpoints mentioned below is `{host}:{port}/sync`

### Perform an on-demand sync

- Method: `POST`
- Path: `/trigger`
- Response:
  - Status: 200
  - Content-Type: `application/json`
  - Body:
    - `success` [boolean]
    - `message` [string]
    - `timestamp` [datetime]
  - Sample
  ```json
  {
    "success": true,
    "message": "Sync completed successfully",
    "timestamp": "2026-03-23T10:15:30"
  }
  ```
- Error Responses:
  - `409 CONFLICT` - Sync already in progress
    ```json
    {
      "errorCode": "SYNC_IN_PROGRESS",
      "message": "A sync operation is already running",
      "timestamp": "2026-03-23T10:15:30"
    }
    ```
  - `503 SERVICE_UNAVAILABLE` - Alfresco is not reachable
    ```json
    {
      "errorCode": "ALFRESCO_UNAVAILABLE",
      "message": "Could not connect to Alfresco",
      "timestamp": "2026-03-23T10:15:30"
    }
    ```
  - `503 SERVICE_UNAVAILABLE` - Nucleus is not reachable
    ```json
    {
      "errorCode": "NUCLEUS_UNAVAILABLE",
      "message": "Could not connect to Nucleus",
      "timestamp": "2026-03-23T10:15:30"
    }
    ```
  - `500 INTERNAL_SERVER_ERROR` - Sync operation failed
    ```json
    {
      "errorCode": "SYNC_FAILED",
      "message": "Sync failed due to an unexpected error",
      "timestamp": "2026-03-23T10:15:30"
    }
    ```

### Get sync status

- Method: `GET`
- Path: `/status`
- Response:
  - Status: 200
  - Content-Type: `application/json`
  - Body:
    - `syncInProgress` [boolean]
    - `lastSyncTime` [datetime] - Set to `"-999999999-01-01T00:00:00"` if sync has never run
    - `lastSyncResult` [string] - `"Never Synced"`, `"Sync completed successfully"`, or `"Failed: {reason}"`
    - `alfrescoStatus` [string] - `"HEALTHY"` or `"UNKNOWN"`
    - `nucleusStatus` [string] - `"HEALTHY"` or `"UNKNOWN"`
  - Sample
  ```json
  {
    "syncInProgress": false,
    "lastSyncTime": "2026-03-23T10:15:30",
    "lastSyncResult": "Sync completed successfully",
    "alfrescoStatus": "HEALTHY",
    "nucleusStatus": "HEALTHY"
  }
  ```

### Health Check

* Method: `GET`
* Path: `/actuator/health`
* Response:
    * Status: 200
    * Content-Type: `application/json`
    * Body:
        * `status` [string] - `"UP"` or `"DOWN"`
    * Sample
    ```json
    {
      "status": "UP"
    }
    ```
